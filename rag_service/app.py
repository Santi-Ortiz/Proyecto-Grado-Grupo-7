# app.py
from langchain_community.embeddings import HuggingFaceEmbeddings
from langchain_community.vectorstores import FAISS
from langchain_community.llms import Ollama
from langchain.chains import RetrievalQA
from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from langchain.prompts import PromptTemplate
import json

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ====== Globales ======
qa_reglamento = None
qa_all = None
qa_enfasis = None
qa_electivas = None
qa_complementarias = None


@app.on_event("startup")
def load_rag():
    embeddings = HuggingFaceEmbeddings(model_name="all-MiniLM-L6-v2")

    # ========= REGLAMENTO =========
    vectorstore_reglamento = FAISS.load_local(
        "faiss_index", embeddings, allow_dangerous_deserialization=True
    )

    prompt_reglamento = PromptTemplate.from_template(
        """Responde la siguiente pregunta en español de forma clara y precisa usando la información disponible:

{context}

Pregunta: {question}

Respuesta:"""
    )

    llm = Ollama(model="llama3", temperature=0.3)

    global qa_reglamento
    qa_reglamento = RetrievalQA.from_chain_type(
        llm=llm,
        retriever=vectorstore_reglamento.as_retriever(),
        chain_type_kwargs={"prompt": prompt_reglamento},
        input_key="question",
    )

    # ========= MATERIAS (4 índices) =========
    def load_index(path: str):
        return FAISS.load_local(path, embeddings, allow_dangerous_deserialization=True)

    vector_all = load_index("faiss_materias")               # TODAS (por defecto)
    vector_enfasis = load_index("faiss_enfasis")            # Énfasis
    vector_electivas = load_index("faiss_electivas")        # Electivas
    vector_complementarias = load_index("faiss_complementarias")  # Complementarias

    prompt_materias = PromptTemplate.from_template(
        """
Eres un sistema de recomendación de materias universitarias. 
Debes responder **únicamente en formato JSON válido**.

📌 Reglas estrictas:
- SOLO utiliza materias presentes en el CONTEXTO.
- Si el estudiante indica un número específico de créditos, SOLO devuelve materias que tengan exactamente esos créditos (usa el valor literal tal como aparece).
- Si el estudiante elige "Cualquiera" en créditos, ignora ese filtro y recomienda solo en base a intereses.
- NO inventes ni cambies los valores de créditos, ID, catálogo ni oferta. Copia exactamente lo que aparezca en el contexto.
- Incluye una justificación clara: afinidad entre los intereses del estudiante y el contenido/competencias de la materia.
- Si no hay coincidencias exactas, devuelve "materias": [] y una explicación clara.

📌 Formato de salida obligatorio:
{{
  "materias": [
    {{
      "nombre": "...",
      "grado": "...",
      "id": "...",
      "creditos": "...",
      "numero_catalogo": "...",
      "numero_oferta": "...",
      "razon": "Explica brevemente por qué esta materia fue recomendada según los intereses del estudiante."
    }}
  ],
  "explicacion": "Explicación general de la recomendación o por qué no se encontraron resultados."
}}

📌 Contexto:
{context}

📌 Consulta del estudiante:
{question}
"""
    )

    global qa_all, qa_enfasis, qa_electivas, qa_complementarias
    qa_all = RetrievalQA.from_chain_type(
        llm=llm,
        retriever=vector_all.as_retriever(search_kwargs={"k": 10}),
        chain_type_kwargs={"prompt": prompt_materias},
        input_key="question",
    )
    qa_enfasis = RetrievalQA.from_chain_type(
        llm=llm,
        retriever=vector_enfasis.as_retriever(search_kwargs={"k": 10}),
        chain_type_kwargs={"prompt": prompt_materias},
        input_key="question",
    )
    qa_electivas = RetrievalQA.from_chain_type(
        llm=llm,
        retriever=vector_electivas.as_retriever(search_kwargs={"k": 10}),
        chain_type_kwargs={"prompt": prompt_materias},
        input_key="question",
    )
    qa_complementarias = RetrievalQA.from_chain_type(
        llm=llm,
        retriever=vector_complementarias.as_retriever(search_kwargs={"k": 10}),
        chain_type_kwargs={"prompt": prompt_materias},
        input_key="question",
    )

    print("✅ Servicios RAG cargados correctamente")


# ========= REGLAMENTO =========
@app.post("/query")
async def query(request: Request):
    data = await request.json()
    question = data.get("question", "")
    result = qa_reglamento.invoke({"question": question})
    return {"answer": result["result"]}


# ========= RECOMENDACIÓN (elige índice por tipo) =========
@app.post("/recomendar-materias")
async def recomendar_materias(request: Request):
    data = await request.json()
    intereses = (data.get("intereses") or "").strip()
    creditos = data.get("creditos", None)
    tipo = (data.get("tipo") or "cualquiera").strip().lower()

    # Construcción de consulta
    if creditos and str(creditos).lower() != "cualquiera":
        consulta = f"Intereses del estudiante: {intereses}. SOLO devolver materias con Créditos: {creditos}."
    else:
        consulta = f"Intereses del estudiante: {intereses}. No aplicar filtro de créditos."

    # Elegir QA por tipo
    qa = {
        "cualquiera": qa_all,
        "énfasis": qa_enfasis,
        "enfasis": qa_enfasis,
        "electivas": qa_electivas,
        "complementarias": qa_complementarias,
    }.get(tipo, qa_all)

    result = qa.invoke({"question": consulta})
    raw = result["result"]

    print(f"🔍 Tipo seleccionado: {tipo}")
    print("🔍 Respuesta cruda del modelo:")
    print(raw)

    try:
        parsed = json.loads(raw)
        return parsed
    except json.JSONDecodeError:
        return {
            "materias": [],
            "explicacion": "No se pudo interpretar correctamente la recomendación generada."
        }
