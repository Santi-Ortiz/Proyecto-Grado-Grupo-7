# ingest_materias.py
"""
Ingesta de materias desde PDF hacia FAISS con metadatos completos.

Ajusta:
- PDF_PATH: ruta al PDF a procesar
- OUT_DIR: nombre del índice a generar (faiss_complementarias | faiss_enfasis | faiss_electivas | faiss_materias)
- TIPO: etiqueta de tipo para metadatos
"""

import re
from pathlib import Path
from typing import List

from langchain_community.vectorstores import FAISS
from langchain_community.embeddings import HuggingFaceEmbeddings
from langchain_community.document_loaders import PyPDFLoader
from langchain.docstore.document import Document


# ===== Configuración =====
PDF_PATH = "../data/Enfasis/Materias_Enfasis.pdf"
OUT_DIR = "faiss_enfasis"        # Cambia a faiss_enfasis / faiss_electivas / faiss_materias según corresponda
TIPO = "enfasis"                 # "enfasis" | "electivas" | "complementarias" | "todas"


# ===== Patrones =====
# Buscamos bloques por estructura típica:
# Nombre:
# Descripción: ...
# Grado: ...
# ID: ...
# Créditos: ...
# Número de catálogo: ...
# Número de oferta: ...
pat_nombre_linea = re.compile(r"^\s*([^\n:]+):\s*$", re.MULTILINE)
pat_desc   = re.compile(r"Descripción:\s*(.+?)(?:\n\s*Grado:|\Z)", re.DOTALL)
pat_grado  = re.compile(r"Grado:\s*([^\n]+)")
pat_id     = re.compile(r"ID:\s*([0-9]+)")
pat_cred   = re.compile(r"Créditos:\s*([0-9]+(?:[.,][0-9]+)?)")
pat_cat    = re.compile(r"Número de catálogo:\s*([0-9]+)")
pat_ofe    = re.compile(r"Número de oferta:\s*([0-9]+)")


def normalizar_float(s: str):
    if s is None:
        return None
    return float(s.replace(",", ".").strip())


def extraer_bloques(texto: str) -> List[str]:
    """
    Divide el texto en bloques por cada 'Nombre:' (línea que termina con ':').
    """
    # Encuentra todas las líneas de nombre
    nombres = [m.group(1).strip() for m in pat_nombre_linea.finditer(texto)]
    if not nombres:
        return []

    # Split por el patrón de nombre para reconstruir pares [nombre, resto]
    splits = pat_nombre_linea.split(texto)[1:]  # [nombre1, resto1, nombre2, resto2, ...]
    bloques = []
    for i in range(0, len(splits), 2):
        nombre = splits[i].strip()
        resto = splits[i + 1] if i + 1 < len(splits) else ""
        bloque = f"Nombre: {nombre}\n{resto}"
        bloques.append(bloque)
    return bloques


def parsear_bloque(b: str):
    # Nombre (primera línea capturada por el patrón de nombre)
    m_nombre = re.search(r"^Nombre:\s*(.+)$", b, re.MULTILINE)
    nombre = m_nombre.group(1).strip() if m_nombre else "Desconocido"

    m_desc = pat_desc.search(b)
    m_gr   = pat_grado.search(b)
    m_id   = pat_id.search(b)
    m_cr   = pat_cred.search(b)
    m_cat  = pat_cat.search(b)
    m_ofe  = pat_ofe.search(b)

    descripcion = (m_desc.group(1).strip() if m_desc else "").strip()
    grado = (m_gr.group(1).strip() if m_gr else "Pregrado").strip()
    _id = (m_id.group(1).strip() if m_id else "").strip()
    creditos = normalizar_float(m_cr.group(1)) if m_cr else None
    numero_catalogo = int(m_cat.group(1)) if m_cat else None
    numero_oferta = int(m_ofe.group(1)) if m_ofe else None

    meta = {
        "nombre": nombre,
        "grado": grado,
        "id": _id,
        "creditos": creditos,
        "numero_catalogo": numero_catalogo,
        "numero_oferta": numero_oferta,
        "tipo": TIPO,
        "source": Path(PDF_PATH).name,
    }

    return descripcion, meta


def main():
    print(f"📄 Cargando PDF: {PDF_PATH}")
    loader = PyPDFLoader(PDF_PATH)
    pages = loader.load()

    full_text = "\n".join([p.page_content for p in pages])
    bloques = extraer_bloques(full_text)

    if not bloques:
        print("⚠️ No se detectaron bloques con el patrón esperado. Revisa el PDF o los regex.")
        return

    docs = []
    for b in bloques:
        descripcion, meta = parsear_bloque(b)
        # Usamos la descripción como contenido para ranking semántico
        docs.append(Document(page_content=descripcion or meta["nombre"], metadata=meta))

    embeddings = HuggingFaceEmbeddings(model_name="all-MiniLM-L6-v2")
    faiss_index = FAISS.from_documents(docs, embeddings)
    faiss_index.save_local(OUT_DIR)
    print(f"✅ Índice '{OUT_DIR}' creado con {len(docs)} documentos.")


if __name__ == "__main__":
    main()
