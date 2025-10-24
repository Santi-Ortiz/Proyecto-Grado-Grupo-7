from pathlib import Path
from langchain_community.embeddings import HuggingFaceEmbeddings
from langchain_community.vectorstores import FAISS
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain_community.document_loaders import DirectoryLoader, PyPDFLoader

def ingest(folder_path: str = "../data/Busquedas", index_path: str = "faiss_index"):
    # 1) Cargar TODOS los PDFs de la carpeta (recursivo)
    folder = Path(folder_path)
    loader = DirectoryLoader(
        str(folder),
        glob="**/*.pdf",              # busca todos los PDFs dentro de la carpeta
        loader_cls=PyPDFLoader,       # usa el mismo loader que ya tenías
        show_progress=True,
        use_multithreading=True,
    )
    documents = loader.load()
    if not documents:
        print(f"⚠️ No se encontraron PDFs en: {folder.resolve()}")
        return

    # 2) Particionar igual que antes
    text_splitter = RecursiveCharacterTextSplitter(chunk_size=1000, chunk_overlap=100)
    docs = text_splitter.split_documents(documents)

    # 3) Embeddings y FAISS igual que antes
    embeddings = HuggingFaceEmbeddings(model_name="all-MiniLM-L6-v2")
    db = FAISS.from_documents(docs, embeddings)

    # 4) Guardar índice
    db.save_local(index_path)
    print(f"✅ Ingesta completa ({len(documents)} PDFs, {len(docs)} chunks) y FAISS creado en '{index_path}'.")

if __name__ == "__main__":
    ingest()           # por defecto toma ../data; puedes pasar otra carpeta o nombre de índice
    # ej: ingest("../mis_pdfs", "faiss_reglamentos")
