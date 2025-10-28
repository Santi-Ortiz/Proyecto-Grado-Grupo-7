import requests
import time
import random

URL = "http://localhost:8000/query"

# Lista de preguntas distintas
PREGUNTAS = [
    "¿Cómo se calcula el promedio acumulado?",
    "¿Cuántos créditos mínimos necesito para graduarme?",
    "¿Qué pasa si repruebo una materia tres veces?",
    "¿Puedo cancelar una asignatura después del segundo corte?",
    "¿Cómo se calcula la nota definitiva de una materia?",
    "¿Qué significa estar en prueba académica?",
    "¿Cuál es el procedimiento para solicitar reingreso?",
    "¿Qué condiciones debo cumplir para hacer doble programa?",
    "¿Cuántas veces puedo repetir una asignatura?",
    "¿Cómo se define la carga académica máxima por semestre?"
]

N = len(PREGUNTAS)
times = []

print(f"\n Ejecutando {N} consultas diferentes a /query...\n")

# Warm-up
for _ in range(2):
    requests.post(URL, json={"question": "warmup"})

for i, question in enumerate(PREGUNTAS, start=1):
    start = time.time()
    response = requests.post(URL, json={"question": question})
    elapsed = (time.time() - start) * 1000  # ms
    header_time = response.headers.get("X-Process-Time-ms")
    t = float(header_time) if header_time else elapsed
    times.append(t)
    print(f"req {i}: {t:.2f} ms  →  '{question[:40]}...'")

if times:
    avg = sum(times) / len(times)
    print(f"\n Total consultas: {len(times)}")
    print(f" Promedio: {avg:.2f} ms")
    print(f" Mínimo: {min(times):.2f} ms")
    print(f" Máximo: {max(times):.2f} ms")
else:
    print(" No se registraron tiempos.")
