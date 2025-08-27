# app.py
from typing import List, Dict, Any
import json

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
embeddings = None

# Reglamentos
qa_reglamento = None

# Índices por tipo (materias)
vector_all = None
vector_enfasis = None
vector_electivas = None
vector_complementarias = None

# LLM único (usado para /query y para explicaciones)
llm = None


# =========================
# Startup: carga de modelos e índices
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
        """Responde la siguiente pregunta en español de forma clara y precisa usando únicamente la información del contexto:

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
        vector_all = load_index("faiss_materias")  # TODAS (por defecto)
    except Exception:
        vector_all = None
    try:
        vector_enfasis = load_index("faiss_enfasis")  # Énfasis
    except Exception:
        vector_enfasis = None
    try:
        vector_electivas = load_index("faiss_electivas")  # Electivas
    except Exception:
        vector_electivas = None
    try:
        vector_complementarias = load_index("faiss_complementarias")  # Complementarias
    except Exception:
        vector_complementarias = None

    print("✅ Servicios RAG cargados correctamente")


# =========================
# Utilidades de recuperación/filtrado (materias)
# =========================
def get_store(tipo: str):
    t = (tipo or "cualquiera").strip().lower()
    mapping = {
        "cualquiera": vector_all or vector_all,  # fallback a vector_all
        "énfasis": vector_enfasis or vector_all,
        "enfasis": vector_enfasis or vector_all,
        "electivas": vector_electivas or vector_all,
        "complementarias": vector_complementarias or vector_all,
    }
    return mapping.get(t, vector_all)

def recuperar_candidatos(intereses: str, store: FAISS, k: int = 30) -> List[Document]:
    if store is None:
        return []
    # Recuperación semántica por descripción (page_content)
    try:
        return store.similarity_search(intereses, k=k)
    except Exception:
        return []

def filtrar_por_creditos(docs: List[Document], creditos_usuario) -> List[Document]:
    # Si el usuario no restringe créditos, no filtramos
    if creditos_usuario is None or str(creditos_usuario).strip().lower() == "cualquiera":
        return docs

    try:
        objetivo = float(creditos_usuario)
    except Exception:
        return []

    filtrados = []
    for d in docs:
        c = d.metadata.get("creditos", None)
        try:
            if c is not None and float(c) == objetivo:
                filtrados.append(d)
        except Exception:
            continue
    return filtrados

def dedupe_por_id(docs: List[Document]) -> List[Document]:
    visto = set()
    out = []
    for d in docs:
        _id = (d.metadata or {}).get("id")
        if _id and _id not in visto:
            visto.add(_id)
            out.append(d)
    return out

def pedir_explicaciones(intereses: str, items: List[Dict[str, Any]]) -> Dict[str, str]:
    """
    items: [{id, nombre, descripcion}]
    devuelve: {id: explicacion}
    """
    # Recorta descripciones largas para mantener al LLM enfocado
    payload = {
        "intereses": intereses,
        "cursos": [
            {
                "id": it["id"],
                "nombre": it.get("nombre", ""),
                "descripcion": (it.get("descripcion", "") or "")[:1200],
            }
            for it in items
            if it.get("id")
        ],
    }

    prompt = (
        "Eres un asistente que SOLO genera explicaciones breves por curso.\n"
        "Devuelve JSON VÁLIDO con la forma exacta:\n"
        "{ \"explanations\": [ {\"id\": \"...\", \"explicacion\": \"...\"}, ... ] }\n"
        "- Usa exactamente los mismos ids que llegan en la entrada.\n"
        "- Máximo 1–2 frases por curso conectando la descripción con los intereses del estudiante.\n"
        "- NO inventes ni cites números (créditos, id, catálogo, oferta). Eso lo arma otro proceso.\n"
        "- Responde SOLO con JSON, sin texto adicional.\n\n"
        "Entrada JSON:\n"
        f"{json.dumps(payload, ensure_ascii=False)}"
    )

    try:
        txt = llm.invoke(prompt).strip()
        data = json.loads(txt)
        pares = data.get("explanations", [])
        return {p.get("id"): p.get("explicacion", "") for p in pares if p.get("id")}
    except Exception:
        # Fallback simple si el LLM devuelve algo no parseable
        return {it["id"]: f"Se alinea con tus intereses: {intereses}." for it in items if it.get("id")}


# =========================
# Endpoints
# =========================

# ========= REGLAMENTO =========
@app.post("/query")
async def query(request: Request):
    data = await request.json()
    question = data.get("question", "").strip()
    if not question:
        return {"answer": "Debes enviar una pregunta en el campo 'question'."}

    if qa_reglamento is None:
        return {"answer": "El índice de reglamento no está disponible en este servidor."}

    result = qa_reglamento.invoke({"question": question})
    return {"answer": result.get("result", "")}


# ========= RECOMENDACIÓN (elige índice por tipo) =========
@app.post("/recomendar-materias")
async def recomendar_materias(request: Request):
    data = await request.json()
    intereses = (data.get("intereses") or "").strip()
    creditos = data.get("creditos", None)
    tipo = (data.get("tipo") or "cualquiera").strip().lower()

    store = get_store(tipo)
    candidatos = recuperar_candidatos(intereses, store, k=30)
    candidatos = dedupe_por_id(candidatos)

    # Filtrado determinista por créditos (si el usuario pidió un número)
    candidatos_filtrados = filtrar_por_creditos(candidatos, creditos)

    # Caso sin coincidencias exactas con el filtro de créditos
    if str(creditos).strip().lower() != "cualquiera" and len(candidatos_filtrados) == 0:
        return {
            "materias": [],
            "explicacion": f"No hubo coincidencias exactas con {creditos} créditos para los intereses dados.",
            "observaciones": {
                "filtros_aplicados": {"intereses": intereses, "creditos": str(creditos), "tipo": tipo},
                "fuente": "faiss",
                "advertencias": []
            }
        }

    # Si no hay filtro de créditos, quedarnos con top-N
    seleccion_docs = candidatos_filtrados if str(creditos).strip().lower() != "cualquiera" else candidatos[:8]

    # Preparar items con metadatos (los números SIEMPRE salen de aquí)
    items = []
    for d in seleccion_docs:
        md = d.metadata or {}
        items.append({
            "id": md.get("id", ""),
            "nombre": md.get("nombre", ""),
            "grado": md.get("grado", "Pregrado"),
            "creditos": md.get("creditos", None),
            "numero_catalogo": md.get("numero_catalogo", None),
            "numero_oferta": md.get("numero_oferta", None),
            "descripcion": d.page_content or "",
        })

    # El LLM SOLO genera explicaciones por id (no toca números)
    explic_map = pedir_explicaciones(
        intereses,
        [{"id": it["id"], "nombre": it["nombre"], "descripcion": it["descripcion"]} for it in items if it.get("id")]
    )

    # Ensamblado final del JSON con números de metadatos
    materias = []
    for it in items:
        materias.append({
            "nombre": it["nombre"],
            "grado": it["grado"],
            "id": it["id"],
            "creditos": it["creditos"],
            "numero_catalogo": it["numero_catalogo"],
            "numero_oferta": it["numero_oferta"],
            "explicacion": explic_map.get(it["id"], f"Se alinea con tus intereses: {intereses}.")
        })

    return {
        "materias": materias,
        "explicacion": "Resultados generados por recuperación semántica + filtrado determinista por metadatos.",
        "observaciones": {
            "filtros_aplicados": {"intereses": intereses, "creditos": str(creditos), "tipo": tipo},
            "fuente": "faiss",
            "advertencias": []
        }
    }
