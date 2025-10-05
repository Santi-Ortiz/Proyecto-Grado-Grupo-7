# app.py
from typing import List, Dict, Any, Tuple
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
# Globales y configuración
# =========================
qa_reglamento = None
vector_all = None
vector_enfasis = None
vector_electivas = None
vector_complementarias = None
vector_ciencias_basicas = None
llm = None
embeddings = None

def _env_float(name: str, default: float) -> float:
    try:
        v = float(os.getenv(name, default))
        return max(0.0, min(1.0, v))
    except Exception:
        return default

def _env_int(name: str, default: int) -> int:
    try:
        v = int(os.getenv(name, default))
        return max(0, v)
    except Exception:
        return default

def _env_bool(name: str, default: bool) -> bool:
    val = os.getenv(name, None)
    if val is None:
        return default
    return str(val).strip().lower() in ("1", "true", "yes", "y", "on")

# Puntajes para clasificación en tablas
SCORE_MIN_STRICT = _env_float("RAG_SCORE_MIN", 0.60)      
SUGGEST_MIN      = _env_float("RAG_SUGGEST_MIN", 0.30)    
SUGGEST_MAX_CFG  = _env_float("RAG_SUGGEST_MAX", 0.59)    
SUGGEST_MAX      = min(SUGGEST_MAX_CFG, max(0.0, SCORE_MIN_STRICT - 0.01))  # sin solape

# Límite de materias por tabla
MAIN_MAX            = _env_int("RAG_MAIN_MAX", 2)             
SUGGEST_ITEMS_MAX   = _env_int("RAG_SUGGEST_ITEMS_MAX", 6)

# Recuperación (k candidatos crudos a FAISS antes de filtros)
RETRIEVE_K          = _env_int("RAG_RETRIEVE_K", 60)

# Fallback: si no hay sugeridas en el rango, tomar los siguientes mejores
SUGGEST_FALLBACK    = _env_bool("RAG_SUGGEST_FALLBACK", True)

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
        global vector_all; vector_all = load_index("faiss_materias")
    except Exception:
        vector_all = None
    try:
        global vector_enfasis; vector_enfasis = load_index("faiss_enfasis")
    except Exception:
        vector_enfasis = None
    try:
        global vector_electivas; vector_electivas = load_index("faiss_electivas")
    except Exception:
        vector_electivas = None
    try:
        global vector_complementarias; vector_complementarias = load_index("faiss_complementarias")
    except Exception:
        vector_complementarias = None
    try:
        global vector_ciencias_basicas; vector_ciencias_basicas = load_index("faiss_ciencias_basicas")
    except Exception:
        vector_ciencias_basicas = None

    print(
        "✅ RAG listo "
        f"(RAG_SCORE_MIN={SCORE_MIN_STRICT} | RAG_SUGGEST_MIN={SUGGEST_MIN} | SUGGEST_MAX={SUGGEST_MAX} | "
        f"RAG_MAIN_MAX={MAIN_MAX} | RAG_SUGGEST_ITEMS_MAX={SUGGEST_ITEMS_MAX} | RAG_RETRIEVE_K={RETRIEVE_K} | "
        f"RAG_SUGGEST_FALLBACK={SUGGEST_FALLBACK})"
    )


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
        "electivas_ciencias_basicas": vector_ciencias_basicas,
    }.get(t, vector_all)

def recuperar_candidatos_raw(intereses: str, store, k: int) -> List[Tuple[Document, float]]:
    """Devuelve [(doc, dist)] crudos desde FAISS."""
    if store is None:
        return []
    try:
        raw = store.similarity_search_with_score(intereses, k=k)
    except Exception:
        return []
    return list(raw)

def _doc_key(d: Document) -> str:
    """Clave robusta para dedupe: id | nombre|catalogo|oferta | hash del contenido."""
    md = d.metadata or {}
    _id = (md.get("id") or "").strip()
    if _id:
        return f"id:{_id}"
    nombre = (md.get("nombre") or "").strip().lower()
    nc = str(md.get("numero_catalogo") or "").strip()
    no = str(md.get("numero_oferta") or "").strip()
    if nombre or nc or no:
        return f"k:{nombre}|{nc}|{no}"
    return f"h:{hash(d.page_content or '')}"

def dedupe_raw(pares: List[Tuple[Document, float]]) -> List[Tuple[Document, float]]:
    visto = set()
    out: List[Tuple[Document, float]] = []
    for d, dist in pares:
        key = _doc_key(d)
        if key not in visto:
            visto.add(key)
            out.append((d, dist))
    return out

def normalizar_scores(pares: List[Tuple[Document, float]]) -> List[Tuple[Document, float]]:
    """
    Min–max sobre distancias para score en [0..1] (por consulta):
    - menor distancia => score más alto (1.0)
    - mayor distancia => score más bajo (0.0)
    """
    if not pares:
        return []
    dists = [float(dist) for _, dist in pares]
    min_d = min(dists); max_d = max(dists)
    span = (max_d - min_d)
    if span == 0:
        return [(doc, 1.0) for doc, _ in pares]
    norm: List[Tuple[Document, float]] = []
    for (doc, dist) in pares:
        score = 1.0 - ((float(dist) - min_d) / span)
        if score < 0.0: score = 0.0
        if score > 1.0: score = 1.0
        norm.append((doc, score))
    return norm

def filtrar_por_creditos_scored(pares: List[Tuple[Document, float]], creditos_usuario) -> List[Tuple[Document, float]]:
    """
    Filtra una lista [(doc, score)] por créditos exactos según metadata.
    Si 'cualquiera', devuelve la lista tal cual.
    """
    if creditos_usuario is None or str(creditos_usuario).lower() == "cualquiera":
        return pares
    try:
        objetivo = float(creditos_usuario)
    except Exception:
        return []
    out = []
    for d, s in pares:
        c = (d.metadata or {}).get("creditos", None)
        try:
            if c is not None and float(c) == objetivo:
                out.append((d, s))
        except Exception:
            continue
    return out

def pedir_explicacion_global(intereses: str, tipo: str, creditos, cursos: List[Dict[str, str]]) -> str:
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

    # 1) Recuperación cruda + dedupe
    pares_raw = recuperar_candidatos_raw(intereses, store, k=RETRIEVE_K)
    pares_raw = dedupe_raw(pares_raw)

    # 2) Normalización a score 0..1 (ANTES de cualquier filtro por créditos)
    #    Así garantizamos que la tabla 2 (que ignora créditos) y la 1 (que sí filtra)
    #    compartan la misma escala de score para esta consulta.
    pares_scored_all = normalizar_scores(pares_raw)  # [(doc, score)]

    # 3) TABLA 1 (FUERTES): filtra por créditos (si aplica) + umbral fuerte
    candidatos_fuertes = filtrar_por_creditos_scored(pares_scored_all, creditos)
    fuertes: List[Tuple[Document, float]] = [
        (d, s) for d, s in candidatos_fuertes if s >= SCORE_MIN_STRICT
    ]
    fuertes = fuertes[:MAIN_MAX]

    # 4) TABLA 2 (SUGERIDAS): IGNORA CRÉDITOS (solo intereses)
    #    a) Primero, cursos de alta afinidad (>= SCORE_MIN_STRICT) que NO están en 'fuertes'
    keys_fuertes = set(_doc_key(d) for d, _ in fuertes)
    high_no_credits_match: List[Tuple[Document, float]] = []
    for d, s in pares_scored_all:
        if s >= SCORE_MIN_STRICT and _doc_key(d) not in keys_fuertes:
            high_no_credits_match.append((d, s))

    #    b) Luego, cursos de afinidad moderada (SUGGEST_MIN..SUGGEST_MAX) ignorando créditos
    moderados: List[Tuple[Document, float]] = []
    for d, s in pares_scored_all:
        if SUGGEST_MIN <= s <= SUGGEST_MAX and _doc_key(d) not in keys_fuertes:
            moderados.append((d, s))

    #    c) Unimos priorizando alta afinidad que no entró a la tabla 1
    sugeridos: List[Tuple[Document, float]] = []
    seen = set()
    for d, s in high_no_credits_match + moderados:
        k = _doc_key(d)
        if k not in seen:
            seen.add(k)
            sugeridos.append((d, s))

    #    d) Limitar tamaño
    sugeridos = sugeridos[:SUGGEST_ITEMS_MAX]

    #    e) Fallback opcional: si quedó vacío, tomar los siguientes mejores < SCORE_MIN_STRICT (sin solape con fuertes)
    if SUGGEST_FALLBACK and not sugeridos and pares_scored_all:
        for d, s in pares_scored_all:
            if _doc_key(d) in keys_fuertes:
                continue
            if s < SCORE_MIN_STRICT:
                sugeridos.append((d, s))
            if len(sugeridos) >= SUGGEST_ITEMS_MAX:
                break

    # 5) Si usuario pidió créditos exactos y no hay NADA en ninguna tabla
    if str(creditos).lower() != "cualquiera" and not fuertes and not sugeridos:
        return {
            "materias": [],
            "explicacion": f"No hubo coincidencias exactas con {creditos} créditos para los intereses dados.",
            "materias_sugeridas": [],
            "explicacion_sugeridas": "",
            "observaciones": {
                "filtros_aplicados": {"intereses": intereses, "creditos": str(creditos), "tipo": tipo},
                "fuente": "faiss",
                "advertencias": []
            }
        }

    # 6) Armar payloads (SIEMPRE desde metadatos deterministas)
    def to_item(d: Document) -> Dict[str, Any]:
        md = d.metadata or {}
        return {
            "nombre": md.get("nombre", ""),
            "grado": md.get("grado", "Pregrado"),
            "id": md.get("id", ""),
            "creditos": md.get("creditos", None),
            "numero_catalogo": md.get("numero_catalogo", None),
            "numero_oferta": md.get("numero_oferta", None),
        }

    materias = [to_item(d) for d, _ in fuertes]
    materias_sugeridas = [to_item(d) for d, _ in sugeridos]

    # 7) Explicaciones
    explicacion = ""
    if materias:
        explicacion = pedir_explicacion_global(
            intereses, tipo, creditos,
            [{"nombre": it["nombre"], "descripcion": ""} for it in materias]
        )

    explicacion_sugeridas = ""
    if materias_sugeridas:
        explicacion_sugeridas = (
            "Las asignaturas que se presentan a continuación se han seleccionado por su afinidad temática con los "
            "intereses registrados, prescindiendo del filtro de créditos. Si bien pueden no ajustarse a la restricción "
            "numérica establecida, podrían aportar perspectivas complementarias pertinentes a su formación."
        )

    # 8) Si no hubo nada en absoluto
    if not materias and not materias_sugeridas:
        return {
            "materias": [],
            "explicacion": "No se encontraron materias con suficiente afinidad para los intereses proporcionados. Intente ser más específico o pruebe con otros términos.",
            "materias_sugeridas": [],
            "explicacion_sugeridas": "",
            "observaciones": {
                "filtros_aplicados": {"intereses": intereses, "creditos": str(creditos), "tipo": tipo},
                "fuente": "faiss",
                "advertencias": ["sin_resultados_en_umbrales"]
            }
        }

    return {
        "materias": materias,                      # tabla 1 (fuertes con créditos)
        "explicacion": explicacion,
        "materias_sugeridas": materias_sugeridas,  # tabla 2 (intereses, sin créditos)
        "explicacion_sugeridas": explicacion_sugeridas,
        "observaciones": {
            "filtros_aplicados": {"intereses": intereses, "creditos": str(creditos), "tipo": tipo},
            "fuente": "faiss",
            "advertencias": []
        }
    }
