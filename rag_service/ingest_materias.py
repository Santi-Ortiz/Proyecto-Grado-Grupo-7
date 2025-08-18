from langchain_community.vectorstores import FAISS
from langchain_community.embeddings import HuggingFaceEmbeddings
from langchain_community.document_loaders import PyPDFLoader
from langchain.text_splitter import RecursiveCharacterTextSplitter

# Cargar el PDF con las materias
loader = PyPDFLoader("../data/Cursos_Javeriana.pdf")
pages = loader.load()

# Dividir texto en fragmentos manejables
text_splitter = RecursiveCharacterTextSplitter(chunk_size=500, chunk_overlap=50)
docs = text_splitter.split_documents(pages)

# Crear embeddings
embeddings = HuggingFaceEmbeddings(model_name="all-MiniLM-L6-v2")

# Crear índice FAISS
faiss_index = FAISS.from_documents(docs, embeddings)
faiss_index.save_local("faiss_materias")
print("✅ Índice de materias creado correctamente.")
