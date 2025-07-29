document.addEventListener('DOMContentLoaded', function () {
    const form = document.querySelector('form');
    const fileInput = document.querySelector('input[type="file"]');

    if (form && fileInput) {
        form.addEventListener('submit', function (event) {
            const file = fileInput.files[0];

            if (!file) {
                alert("Por favor selecciona un archivo antes de continuar.");
                event.preventDefault();
                return;
            }

            if (!file.name.toLowerCase().endsWith(".pdf")) {
                alert("Solo se permiten archivos PDF.");
                event.preventDefault();
            }
        });
    }

    // =======================
    // Funcionalidad de consultas RAG
    // =======================

    const consultarRAGBtn = document.getElementById('consultarRAGBtn');
    const preguntaRAG = document.getElementById('preguntaRAG');
    const respuestaRAG = document.getElementById('respuestaRAG');

    if (consultarRAGBtn && preguntaRAG && respuestaRAG) {
        consultarRAGBtn.addEventListener('click', async function () {
            const pregunta = preguntaRAG.value.trim();

            if (!pregunta) {
                respuestaRAG.innerText = "Por favor ingresa una pregunta.";
                respuestaRAG.style.display = "block";
                return;
            }

            respuestaRAG.innerText = "Consultando, por favor espera...";
            respuestaRAG.style.display = "block";

            try {
                const response = await fetch('/rag/consulta', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(pregunta)
                });

                if (!response.ok) {
                    throw new Error("Error en la consulta: " + response.statusText);
                }

                const data = await response.text();
                respuestaRAG.innerText = data;
            } catch (error) {
                respuestaRAG.innerText = "Error: " + error.message;
            }
        });
    }
});
