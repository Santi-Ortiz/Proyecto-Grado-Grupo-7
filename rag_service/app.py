from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from langchain_community.embeddings import HuggingFaceEmbeddings
from langchain_community.vectorstores import FAISS
from langchain_community.llms import Ollama
from langchain.chains import RetrievalQA
from langchain.prompts import PromptTemplate

app = FastAPI()

# CORS para permitir conexión desde frontend
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

global qa

@app.on_event("startup")
def load_rag():
    # Cargar embeddings y vectorstore
    embeddings = HuggingFaceEmbeddings(model_name="all-MiniLM-L6-v2")
    vectorstore = FAISS.load_local("faiss_index", embeddings, allow_dangerous_deserialization=True)

    # Crear prompt personalizado en español
    prompt_template = PromptTemplate.from_template(
        "Responde la siguiente pregunta en español de forma clara y precisa usando la información disponible:\n\n{context}\n\nPregunta: {question}\n\nRespuesta:"
    )

    # Cargar LLM
    llm = Ollama(model="llama3")

    # Crear cadena de QA con el prompt en español
    global qa
    qa = RetrievalQA.from_chain_type(
        llm=llm,
        retriever=vectorstore.as_retriever(),
        chain_type_kwargs={"prompt": prompt_template}
    )

    print("✅ Servicio RAG cargado y listo.")

@app.post("/query")
async def query(request: Request):
    data = await request.json()
    question = data.get("question", "")
    result = qa.invoke({"query": question})
    
    # Solo retornar la respuesta (sin incluir el query original)
    return {"answer": result["result"]}
