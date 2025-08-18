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

# Variables globales
global qa_reglamento, qa_materias


@app.on_event("startup")
def load_rag():
    embeddings = HuggingFaceEmbeddings(model_name="all-MiniLM-L6-v2")

    # ========= INDICE DEL REGLAMENTO =========
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

    # ========= INDICE DE MATERIAS =========
    vectorstore_materias = FAISS.load_local(
        "faiss_materias", embeddings, allow_dangerous_deserialization=True
    )

    prompt_materias = PromptTemplate.from_template(
        """
Eres un sistema de recomendación de materias universitarias. Responde en JSON ESTRICTO.

Reglas:
- SOLO devuelve materias del contexto.
- Respeta estrictamente las restricciones y requisitos que vengan en la consulta del estudiante (por ejemplo, número de créditos).
- Si no hay coincidencias, devuelve "materias": [] y una explicación breve de por qué.

Formato:
{{
  "materias": [
    {{
      "nombre": "...",
      "grado": "...",
      "id": "...",
      "creditos": "...",
      "numero_catalogo": "...",
      "numero_oferta": "..."
    }}
  ],
  "explicacion": "..."
}}

Contexto:
{context}

Consulta del estudiante:
{question}
"""
    )

    global qa_materias
    qa_materias = RetrievalQA.from_chain_type(
        llm=llm,
        retriever=vectorstore_materias.as_retriever(search_kwargs={"k": 10}),
        chain_type_kwargs={"prompt": prompt_materias},
        input_key="question",
    )

    print("✅ Servicios RAG cargados correctamente")


# ========= ENDPOINT PARA PREGUNTAS DEL REGLAMENTO =========
@app.post("/query")
async def query(request: Request):
    data = await request.json()
    question = data.get("question", "")
    result = qa_reglamento.invoke({"question": question})
    return {"answer": result["result"]}


# ========= ENDPOINT PARA RECOMENDACIÓN DE MATERIAS =========
@app.post("/recomendar-materias")
async def recomendar_materias(request: Request):
    data = await request.json()
    intereses = data.get("intereses", "")
    creditos = data.get("creditos", None)

    # Construcción de la consulta: intereses + créditos
    if creditos:
        consulta = f"{intereses}. Buscar SOLO materias con Créditos: {creditos}."
    else:
        consulta = intereses

    # 👇 SOLO enviamos 'question', nada más
    result = qa_materias.invoke({"question": consulta})

    raw = result["result"]

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
