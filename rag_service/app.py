from langchain_community.embeddings import HuggingFaceEmbeddings
from langchain_community.vectorstores import FAISS
from langchain_community.llms import Ollama
from langchain.chains import RetrievalQA
from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from langchain.prompts import PromptTemplate

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

global qa_reglamento, qa_materias

@app.on_event("startup")
def load_rag():
    embeddings = HuggingFaceEmbeddings(model_name="all-MiniLM-L6-v2")

    # Cargar índice del reglamento
    vectorstore_reglamento = FAISS.load_local("faiss_index", embeddings, allow_dangerous_deserialization=True)

    # Crear prompt personalizado en español
    prompt_template = PromptTemplate.from_template(
        "Responde la siguiente pregunta en español de forma clara y precisa usando la información disponible:\n\n{context}\n\nPregunta: {question}\n\nRespuesta:"
    )

    # Crear prompt personalizado en español
    prompt_template_materias = PromptTemplate.from_template(
        "Responde la recomendación de la materia en español de forma clara y precisa usando la información disponible :\n\n{context}\n\nPregunta: {question}\n\nRespuesta:"
    )

    llm = Ollama(model="llama3")
    global qa_reglamento
    qa_reglamento = RetrievalQA.from_chain_type(
        llm=llm,
        retriever=vectorstore_reglamento.as_retriever(),
        chain_type_kwargs={"prompt": prompt_template}
    )

    # Cargar índice de materias
    vectorstore_materias = FAISS.load_local("faiss_materias", embeddings, allow_dangerous_deserialization=True)
    global qa_materias
    qa_materias = RetrievalQA.from_chain_type(llm=llm, retriever=vectorstore_materias.as_retriever(), chain_type_kwargs={"prompt": prompt_template_materias})

    print("✅ Servicios RAG cargados correctamente")

@app.post("/query")
async def query(request: Request):
    data = await request.json()
    question = data.get("question", "")
    result = qa_reglamento.invoke({"query": question})
    
    # Solo retornar la respuesta (sin incluir el query original)
    return {"answer": result["result"]}

@app.post("/recomendar-materias")
async def recomendar_materias(request: Request):
    data = await request.json()
    intereses = data.get("intereses", "")
    result = qa_materias.invoke({"query": intereses})
    return {"answer": result["result"]}