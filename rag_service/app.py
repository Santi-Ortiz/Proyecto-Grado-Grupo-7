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

    # Cargar índice del reglamento
    vectorstore_reglamento = FAISS.load_local("faiss_index", embeddings, allow_dangerous_deserialization=True)

    prompt_template = PromptTemplate.from_template(
        "Responde la siguiente pregunta en español de forma clara y precisa usando la información disponible:\n\n{context}\n\nPregunta: {question}\n\nRespuesta:"
    )

    prompt_template_materias = PromptTemplate.from_template(
        """
        Eres un sistema de recomendación de materias. Devuelve la respuesta en formato JSON estricto.

        Formato esperado:
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

        Consulta:
        {question}
        """
    )

    llm = Ollama(model="llama3", temperature=0.3)

    # Cadena para reglamento
    global qa_reglamento
    qa_reglamento = RetrievalQA.from_chain_type(
        llm=llm,
        retriever=vectorstore_reglamento.as_retriever(),
        chain_type_kwargs={"prompt": prompt_template}
    )

    # Cargar índice de materias
    vectorstore_materias = FAISS.load_local("faiss_materias", embeddings, allow_dangerous_deserialization=True)

    global qa_materias
    qa_materias = RetrievalQA.from_chain_type(
        llm=llm,
        retriever=vectorstore_materias.as_retriever(),
        chain_type_kwargs={"prompt": prompt_template_materias}
    )

    print("✅ Servicios RAG cargados correctamente")

@app.post("/query")
async def query(request: Request):
    data = await request.json()
    question = data.get("question", "")
    result = qa_reglamento.invoke({"query": question})
    return {"answer": result["result"]}

@app.post("/recomendar-materias")
async def recomendar_materias(request: Request):
    data = await request.json()
    intereses = data.get("intereses", "")
    result = qa_materias.invoke({"query": intereses})

    # Mostrar la respuesta cruda para debugging
    print("🔍 Respuesta cruda del modelo:")
    print(result["result"])

    try:
        parsed = json.loads(result["result"])
        return parsed
    except json.JSONDecodeError:
        return {
            "materias": [],
            "explicacion": "No se pudo interpretar correctamente la recomendación generada."
        }
