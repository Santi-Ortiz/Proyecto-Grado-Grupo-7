document.addEventListener('DOMContentLoaded', function () {
    const form = document.querySelector('form');
    const fileInput = document.querySelector('input[type="file"]');

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
});