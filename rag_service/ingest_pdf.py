# ingest_pdf.py
from pathlib import Path
from typing import List, Dict
import re

from langchain_community.vectorstores import FAISS
from langchain_community.document_loaders import DirectoryLoader, PyPDFLoader
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain_community.embeddings import HuggingFaceEmbeddings

# Heurística: línea en MAYÚSCULAS con números romanos o "ARTÍCULO", etc.
SECTION_RE = re.compile(r"^\s*(?:[IVXLC]+\.\s+.*|CAP[IÍ]TULO\s+.*|ART[IÍ]CULO\s+\d+.*)$")

def _clean(txt: str) -> str:
    if not txt:
        return ""
    t = txt.replace("\r", "\n")
    # Colapsar saltos múltiples
    t = re.sub(r"\n{3,}", "\n\n", t)
    # Quitar numeración de pie de página común
    t = re.sub(r"\n?\s*\d+\s*$", "", t)
    return t.strip()

def _section_hints(page_text: str) -> str:
    # Devuelve primera línea que parece encabezado/sección
    for line in page_text.splitlines():
        if SECTION_RE.match(line.strip()):
            return line.strip()
    return ""

def ingest(folder_path: str = "../data/Busquedas", index_path: str = "faiss_index"):
    folder = Path(folder_path)
    loader = DirectoryLoader(
        str(folder),
        glob="**/*.pdf",
        loader_cls=PyPDFLoader,
        show_progress=True,
        use_multithreading=True,
    )
    pages = loader.load()
    if not pages:
        print(f"⚠️ No se encontraron PDFs en: {folder.resolve()}")
        return

    # Añadir metadatos útiles y limpieza ligera por página
    enriched = []
    for d in pages:
        md = d.metadata or {}
        fname = Path(md.get("source", "documento.pdf")).name
        page = md.get("page", None)
        text = _clean(d.page_content or "")
        if not text:
            continue
        sec = _section_hints(text)
        md_new = {
            **md,
            "filename": fname,
            "page": page,
            "section_hint": sec,
        }
        d.page_content = text
        d.metadata = md_new
        enriched.append(d)

    # Split con respeto a párrafos para mantener coherencia normativa
    splitter = RecursiveCharacterTextSplitter(
        chunk_size=900, chunk_overlap=120,
        separators=["\n\n", "\n", ". ", " "],
        is_separator_regex=False,
    )
    docs = splitter.split_documents(enriched)

    # Embeddings multilingües (mejor en español) + normalización
    embeddings = HuggingFaceEmbeddings(
        model_name="intfloat/multilingual-e5-base",
        encode_kwargs={"normalize_embeddings": True},
    )

    db = FAISS.from_documents(docs, embeddings)
    db.save_local(index_path)
    print(f"✅ Ingesta completa: {len(enriched)} páginas → {len(docs)} chunks → '{index_path}'")

if __name__ == "__main__":
    ingest()
