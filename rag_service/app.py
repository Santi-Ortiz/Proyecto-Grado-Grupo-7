# app.py
from typing import List, Dict, Any, Tuple
import json
import os
import time

from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from starlette.middleware.base import BaseHTTPMiddleware

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

# CORS (exponemos el header de tiempo)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
    expose_headers=["X-Process-Time-ms"],
)

# Middleware de medición
class TimingMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request, call_next):
        t0 = time.perf_counter()
        response = await call_next(request)
        dt_ms = (time.perf_counter() - t0) * 1000.0
        response.headers["X-Process-Time-ms"] = f"{dt_ms:.2f}"
        return response

app.add_middleware(TimingMiddleware)

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

# ✅ Dos embeddings separados
embeddings_reglamento = None
embeddings_materias = None

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

# Puntajes para clasificación en tablas (recomendaciones)
SCORE_MIN_STRICT = _env_float("RAG_SCORE_MIN", 0.60)
SUGGEST_MIN      = _env_float("RAG_SUGGEST_MIN", 0.30)
SUGGEST_MAX_CFG  = _env_float("RAG_SUGGEST_MAX", 0.59)
SUGGEST_MAX      = min(SUGGEST_MAX_CFG, max(0.0, SCORE_MIN_STRICT - 0.01))

# Límite de materias por tabla
MAIN_MAX            = _env_int("RAG_MAIN_MAX", 2)
SUGGEST_ITEMS_MAX   = _env_int("RAG_SUGGEST_ITEMS_MAX", 6)

# Recuperación (k candidatos crudos)
RETRIEVE_K          = _env_int("RAG_RETRIEVE_K", 60)

# Fallback sugerencias
SUGGEST_FALLBACK    = _env_bool("RAG_SUGGEST_FALLBACK", True)

# =========================
# Prompt estricto para búsquedas
# =========================
STRICT_PROMPT_TMPL = """Responde EN ESPAÑOL SOLO con base en las FUENTES dadas.
- Si la respuesta NO está en las fuentes o hay duda, responde: "No encontrado en los documentos proporcionados."
- Cita entre corchetes la(s) fuente(s) con formato [archivo.pdf, p. X] al final de cada oración que contenga dato normativo.
- Sé literal cuando menciones artículos, numerales o porcentajes. No inventes ejemplos ni cifras.

FUENTES:
{context}

PREGUNTA: {question}

RESPUESTA:"""

prompt_reglamento = PromptTemplate.from_template(STRICT_PROMPT_TMPL)

def _build_retriever(vs: FAISS):
    return vs.as_retriever(
        search_type="similarity_score_threshold",
        search_kwargs={
            "score_threshold": float(os.getenv("RAG_SCORE_THRESHOLD", 0.35)),
            "k": int(os.getenv("RAG_TOP_K", 8)),
        },
    )

# =========================
# Startup
# =========================
@app.on_event("startup")
def load_rag():
    global embeddings_reglamento, embeddings_materias
    global qa_reglamento, vector_all, vector_enfasis, vector_electivas, vector_complementarias, vector_ciencias_basicas, llm

    # ⚖️ e5 para REGLAMENTO (si el índice fue creado con e5)
    embeddings_reglamento = HuggingFaceEmbeddings(
        model_name="intfloat/multilingual-e5-base",
        encode_kwargs={"normalize_embeddings": True},
    )

    # ✅ MiniLM para MATERIAS (índices existentes)
    embeddings_materias = HuggingFaceEmbeddings(model_name="all-MiniLM-L6-v2")

    # ========= REGLAMENTO / BÚSQUEDAS =========
    try:
        vectorstore_reglamento = FAISS.load_local(
            "faiss_index", embeddings_reglamento, allow_dangerous_deserialization=True
        )
    except Exception:
        vectorstore_reglamento = None

    llm = Ollama(model="llama3", temperature=0.0)

    if vectorstore_reglamento is not None:
        qa_reglamento = RetrievalQA.from_chain_type(
            llm=llm,
            retriever=_build_retriever(vectorstore_reglamento),
            chain_type_kwargs={
                "prompt": prompt_reglamento,
                "document_variable_name": "context",
            },
            input_key="question",
            return_source_documents=True,
        )

    # ========= MATERIAS (índices) =========
    def load_index(path: str):
        return FAISS.load_local(path, embeddings_materias, allow_dangerous_deserialization=True)

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
        "✅ RAG listo (dual embeddings) "
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
    if store is None:
        return []
    try:
        raw = store.similarity_search_with_score(intereses, k=k)
    except Exception:
        return []
    return list(raw)

def _doc_key(d: Document) -> str:
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
    question = data.get("question", "").strip()
    if not question:
        return {"answer": "Debes enviar una pregunta en el campo 'question'."}
    if qa_reglamento is None:
        return {"answer": "El índice de reglamento no está disponible en este servidor."}

    result = qa_reglamento.invoke({"question": question})
    answer = (result.get("result") or "").strip()

    sources = []
    for d in result.get("source_documents", []):
        md = d.metadata or {}
        sources.append({
            "file": md.get("filename") or md.get("source"),
            "page": md.get("page"),
            "section_hint": md.get("section_hint", ""),
            "snippet": (d.page_content or "")[:300]
        })

    if (not answer) or ("No encontrado en los documentos proporcionados" in answer):
        return {"answer": "No encontrado en los documentos proporcionados.", "citations": []}

    return {"answer": answer, "citations": sources}

@app.post("/recomendar-materias")
async def recomendar_materias(request: Request):
    data = await request.json()
    intereses = (data.get("intereses") or "").strip()
    creditos = data.get("creditos", None)
    tipo = (data.get("tipo") or "cualquiera").strip().lower()

    store = get_store(tipo)

    pares_raw = recuperar_candidatos_raw(intereses, store, k=RETRIEVE_K)
    pares_raw = dedupe_raw(pares_raw)
    pares_scored_all = normalizar_scores(pares_raw)

    candidatos_fuertes = filtrar_por_creditos_scored(pares_scored_all, creditos)
    fuertes: List[Tuple[Document, float]] = [
        (d, s) for d, s in candidatos_fuertes if s >= SCORE_MIN_STRICT
    ]
    fuertes = fuertes[:MAIN_MAX]

    keys_fuertes = set(_doc_key(d) for d, _ in fuertes)
    high_no_credits_match: List[Tuple[Document, float]] = []
    for d, s in pares_scored_all:
        if s >= SCORE_MIN_STRICT and _doc_key(d) not in keys_fuertes:
            high_no_credits_match.append((d, s))

    moderados: List[Tuple[Document, float]] = []
    for d, s in pares_scored_all:
        if SUGGEST_MIN <= s <= SUGGEST_MAX and _doc_key(d) not in keys_fuertes:
            moderados.append((d, s))

    sugeridos: List[Tuple[Document, float]] = []
    seen = set()
    for d, s in high_no_credits_match + moderados:
        k = _doc_key(d)
        if k not in seen:
            seen.add(k)
            sugeridos.append((d, s))
    sugeridos = sugeridos[:SUGGEST_ITEMS_MAX]

    if SUGGEST_FALLBACK and not sugeridos and pares_scored_all:
        for d, s in pares_scored_all:
            if _doc_key(d) in keys_fuertes:
                continue
            if s < SCORE_MIN_STRICT:
                sugeridos.append((d, s))
            if len(sugeridos) >= SUGGEST_ITEMS_MAX:
                break

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
        "materias": materias,
        "explicacion": explicacion,
        "materias_sugeridas": materias_sugeridas,
        "explicacion_sugeridas": explicacion_sugeridas,
        "observaciones": {
            "filtros_aplicados": {"intereses": intereses, "creditos": str(creditos), "tipo": tipo},
            "fuente": "faiss",
            "advertencias": []
        }
    }
