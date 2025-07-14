from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from langchain_community.embeddings import HuggingFaceEmbeddings
from langchain_community.vectorstores import FAISS
from langchain_community.llms import Ollama
from langchain.chains import RetrievalQA

app = FastAPI()

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
    embeddings = HuggingFaceEmbeddings(model_name="all-MiniLM-L6-v2")
    vectorstore = FAISS.load_local("faiss_index", embeddings)
    llm = Ollama(model="llama3")
    global qa
    qa = RetrievalQA.from_chain_type(llm=llm, retriever=vectorstore.as_retriever())
    print("✅ Servicio RAG cargado y listo.")

@app.post("/query")
async def query(request: Request):
    data = await request.json()
    question = data.get("question", "")
    result = qa.invoke({"query": question})
    return {"answer": result}