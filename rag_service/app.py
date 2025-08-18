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
Eres un sistema de recomendación de materias universitarias. 
Debes responder **únicamente en formato JSON válido**.

📌 Reglas estrictas:
- SOLO utiliza materias presentes en el CONTEXTO.
- Si el estudiante indica un número específico de créditos, SOLO devuelve materias que tengan exactamente esos créditos (usa el valor literal de "Créditos: X.0" en el contexto).
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

    # Construcción de la consulta
    if creditos and str(creditos).lower() != "cualquiera":
        consulta = f"Intereses del estudiante: {intereses}. SOLO devolver materias con Créditos: {creditos}."
    else:
        consulta = f"Intereses del estudiante: {intereses}. No aplicar filtro de créditos."

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
