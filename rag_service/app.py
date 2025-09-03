# app.py
from typing import List, Dict, Any
import json
import os

from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware

from langchain_community.embeddings import HuggingFaceEmbeddings
from langchain_community.vectorstores import FAISS
from langchain_community.llms import Ollama
from langchain.chains import RetrievalQA
from langchain.prompts import PromptTemplate
from langchain_core.documents import Document


# =========================
# FastAPI
# =========================
app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# =========================
# Globales
# =========================
qa_reglamento = None
vector_all = None
vector_enfasis = None
vector_electivas = None
vector_complementarias = None
llm = None
embeddings = None

def _env_float(name: str, default: float) -> float:
    try:
        v = float(os.getenv(name, default))
        return max(0.0, min(1.0, v))  # acotar por seguridad
    except Exception:
        return default

SCORE_MIN = _env_float("RAG_SCORE_MIN", 0.45)


# =========================
# Startup
# =========================
@app.on_event("startup")
def load_rag():
    global embeddings, qa_reglamento, vector_all, vector_enfasis, vector_electivas, vector_complementarias, llm

    embeddings = HuggingFaceEmbeddings(model_name="all-MiniLM-L6-v2")

    # ========= REGLAMENTO =========
    try:
        vectorstore_reglamento = FAISS.load_local(
            "faiss_index", embeddings, allow_dangerous_deserialization=True
        )
    except Exception:
        vectorstore_reglamento = None

    prompt_reglamento = PromptTemplate.from_template(
        """Responde la siguiente pregunta en español de forma clara y precisa usando la información disponible:

{context}

Pregunta: {question}

Respuesta:"""
    )

    llm = Ollama(model="llama3", temperature=0.0)

    if vectorstore_reglamento is not None:
        qa_reglamento = RetrievalQA.from_chain_type(
            llm=llm,
            retriever=vectorstore_reglamento.as_retriever(),
            chain_type_kwargs={"prompt": prompt_reglamento},
            input_key="question",
        )

    # ========= MATERIAS (4 índices) =========
    def load_index(path: str):
        return FAISS.load_local(path, embeddings, allow_dangerous_deserialization=True)

    try:
        vector_all = load_index("faiss_materias")
    except Exception:
        vector_all = None
    try:
        vector_enfasis = load_index("faiss_enfasis")
    except Exception:
        vector_enfasis = None
    try:
        vector_electivas = load_index("faiss_electivas")
    except Exception:
        vector_electivas = None
    try:
        vector_complementarias = load_index("faiss_complementarias")
    except Exception:
        vector_complementarias = None

    print(f"✅ Servicios RAG cargados correctamente (RAG_SCORE_MIN={SCORE_MIN})")


# =========================
# Utilidades materias
# =========================
def get_store(tipo: str):
    t = (tipo or "cualquiera").strip().lower()
    return {
        "cualquiera": vector_all,
        "énfasis": vector_enfasis,
        "enfasis": vector_enfasis,
        "electivas": vector_electivas,
        "complementarias": vector_complementarias,
    }.get(t, vector_all)

def recuperar_candidatos(intereses: str, store, k=30) -> List[Document]:
    """
    Recupera por similitud y descarta resultados por debajo de SCORE_MIN.
    Usa similarity_search_with_score para convertir distancia -> score (1/(1+dist)).
    """
    if store is None:
        return []
    try:
        raw = store.similarity_search_with_score(intereses, k=k)
    except Exception:
        return []

    scored = []
    for doc, dist in raw:
        try:
            score = 1.0 / (1.0 + float(dist))  # [0..1], mayor es mejor
        except Exception:
            score = 0.0
        if score >= SCORE_MIN:
            scored.append((doc, score))

    scored.sort(key=lambda x: x[1], reverse=True)
    return [d for d, _ in scored]

def filtrar_por_creditos(docs: List[Document], creditos_usuario) -> List[Document]:
    if creditos_usuario is None or str(creditos_usuario).lower() == "cualquiera":
        return docs
    try:
        objetivo = float(creditos_usuario)
    except Exception:
        return []
    out = []
    for d in docs:
        c = d.metadata.get("creditos", None)
        try:
            if c is not None and float(c) == objetivo:
                out.append(d)
        except Exception:
            continue
    return out

def dedupe_por_id(docs: List[Document]) -> List[Document]:
    visto = set()
    out = []
    for d in docs:
        _id = d.metadata.get("id")
        if _id and _id not in visto:
            visto.add(_id)
            out.append(d)
    return out

def pedir_explicacion_global(intereses: str, tipo: str, creditos, cursos: List[Dict[str, str]]) -> str:
    """
    Genera un párrafo corto (2–3 frases) explicando POR QUÉ se devolvieron esos cursos en conjunto.
    No menciona números específicos salvo el filtro de créditos y tipo si existen.
    """
    payload = {
        "intereses": intereses,
        "tipo": tipo,
        "creditos": str(creditos),
        "cursos": [{"nombre": c["nombre"], "descripcion": (c.get("descripcion","") or "")[:300]} for c in cursos]
    }
    prompt = (
        "Eres un asistente que resume POR QUÉ se recomendaron en conjunto los cursos listados.\n"
        "- Escribe 2–3 frases claras en español.\n"
        "- Menciona si se aplicó un filtro de créditos y/o tipo de materia.\n"
        "- Conecta los intereses del estudiante con temas comunes en las descripciones.\n"
        "- No inventes números ni datos fuera de lo dado.\n"
        "- Responde SOLO con texto plano (sin JSON).\n\n"
        f"ENTRADA:\n{json.dumps(payload, ensure_ascii=False)}"
    )
    try:
        texto = llm.invoke(prompt).strip()
        return texto[:600]
    except Exception:
        filtros = []
        if str(creditos).lower() != "cualquiera":
            filtros.append(f"{creditos} créditos")
        if tipo and tipo != "cualquiera":
            filtros.append(f"tipo {tipo}")
        filtros_txt = (" con " + " y ".join(filtros)) if filtros else ""
        return f"Se recomendaron cursos relacionados con tus intereses{filtros_txt}, priorizando descripciones que abordan temas afines y competencias asociadas."


# =========================
# Endpoints
# =========================
@app.post("/query")
async def query(request: Request):
    data = await request.json()
    question = data.get("question", "")
    if not question:
        return {"answer": "Debes enviar una pregunta en el campo 'question'."}
    if qa_reglamento is None:
        return {"answer": "El índice de reglamento no está disponible en este servidor."}
    result = qa_reglamento.invoke({"question": question})
    return {"answer": result.get("result", "")}

@app.post("/recomendar-materias")
async def recomendar_materias(request: Request):
    data = await request.json()
    intereses = (data.get("intereses") or "").strip()
    creditos = data.get("creditos", None)
    tipo = (data.get("tipo") or "cualquiera").strip().lower()

    store = get_store(tipo)
    candidatos = recuperar_candidatos(intereses, store, k=30)
    candidatos = dedupe_por_id(candidatos)

    # Si la recuperación no alcanzó el umbral, corta aquí con explicación clara
    if not candidatos:
        return {
            "materias": [],
            "explicacion": "No se encontraron materias con suficiente afinidad para los intereses proporcionados. Intenta ser más específico o prueba con otros términos.",
            "observaciones": {
                "filtros_aplicados": {"intereses": intereses, "creditos": str(creditos), "tipo": tipo},
                "fuente": "faiss",
                "advertencias": ["filtro_por_umbral_semantico"]
            }
        }

    # Filtrado determinista por créditos (si corresponde)
    candidatos_filtrados = filtrar_por_creditos(candidatos, creditos)

    # Caso sin coincidencias exactas con filtro de créditos
    if str(creditos).lower() != "cualquiera" and len(candidatos_filtrados) == 0:
        return {
            "materias": [],
            "explicacion": f"No hubo coincidencias exactas con {creditos} créditos para los intereses dados.",
            "observaciones": {
                "filtros_aplicados": {"intereses": intereses, "creditos": str(creditos), "tipo": tipo},
                "fuente": "faiss",
                "advertencias": []
            }
        }

    # Selección final
    seleccion = candidatos_filtrados if str(creditos).lower() != "cualquiera" else candidatos[:8]

    items = []
    for d in seleccion:
        md = d.metadata
        items.append({
            "id": md.get("id", ""),
            "nombre": md.get("nombre", ""),
            "grado": md.get("grado", "Pregrado"),
            "creditos": md.get("creditos", None),
            "numero_catalogo": md.get("numero_catalogo", None),
            "numero_oferta": md.get("numero_oferta", None),
            "descripcion": d.page_content or "",
        })

    explicacion_global = pedir_explicacion_global(
        intereses,
        tipo,
        creditos,
        [{"nombre": it["nombre"], "descripcion": it["descripcion"]} for it in items]
    )

    materias = [{
        "nombre": it["nombre"],
        "grado": it["grado"],
        "id": it["id"],
        "creditos": it["creditos"],
        "numero_catalogo": it["numero_catalogo"],
        "numero_oferta": it["numero_oferta"],
    } for it in items]

    return {
        "materias": materias,
        "explicacion": explicacion_global,
        "observaciones": {
            "filtros_aplicados": {"intereses": intereses, "creditos": str(creditos), "tipo": tipo},
            "fuente": "faiss",
            "advertencias": []
        }
    }
