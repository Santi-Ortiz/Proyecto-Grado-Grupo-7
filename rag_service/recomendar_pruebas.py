import requests
import time
import random

URL = "http://localhost:8000/recomendar-materias"

# Lista de consultas
CONSULTAS = [
    {"intereses": "sistemas distribuidos y microservicios", "tipo": "cualquiera", "creditos": "cualquiera"},
    {"intereses": "inteligencia artificial y machine learning", "tipo": "énfasis", "creditos": "cualquiera"},
    {"intereses": "ciberseguridad y hacking ético", "tipo": "electivas", "creditos": "cualquiera"},
    {"intereses": "análisis de datos y big data", "tipo": "complementarias", "creditos": "cualquiera"},
    {"intereses": "bases de datos relacionales y NoSQL", "tipo": "cualquiera", "creditos": 3},
    {"intereses": "desarrollo web full stack", "tipo": "énfasis", "creditos": 4},
    {"intereses": "optimización y algoritmos heurísticos", "tipo": "electivas", "creditos": "cualquiera"},
    {"intereses": "tecnologías de nube y DevOps", "tipo": "complementarias", "creditos": "cualquiera"},
    {"intereses": "redes neuronales profundas", "tipo": "énfasis", "creditos": "cualquiera"},
    {"intereses": "arquitectura de software empresarial", "tipo": "cualquiera", "creditos": "cualquiera"}
]

N = len(CONSULTAS)
times = []

print(f"\n Ejecutando {N} consultas diferentes a /recomendar-materias...\n")

# Warm-up
for _ in range(2):
    requests.post(URL, json=CONSULTAS[0])

for i, payload in enumerate(CONSULTAS, start=1):
    start = time.time()
    response = requests.post(URL, json=payload)
    elapsed = (time.time() - start) * 1000  # ms
    header_time = response.headers.get("X-Process-Time-ms")
    t = float(header_time) if header_time else elapsed
    times.append(t)
    print(f"req {i}: {t:.2f} ms  →  intereses='{payload['intereses'][:35]}...'  tipo={payload['tipo']}")

if times:
    avg = sum(times) / len(times)
    print(f"\n Total consultas: {len(times)}")
    print(f" Promedio: {avg:.2f} ms")
    print(f" Mínimo: {min(times):.2f} ms")
    print(f" Máximo: {max(times):.2f} ms")
else:
    print(" No se registraron tiempos.")
