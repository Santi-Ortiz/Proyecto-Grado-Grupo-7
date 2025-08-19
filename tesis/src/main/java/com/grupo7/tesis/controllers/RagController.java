package com.grupo7.tesis.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.grupo7.tesis.services.RagService;

@RestController
@RequestMapping("/api/rag")
@CrossOrigin(origins = "*")
public class RagController {

    private final RagService ragService;

    @Autowired
    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping
    public String consultarRag(@RequestBody QuestionDTO question) {
        return ragService.obtenerRespuestaRag(question.getQuestion());
    }

    @PostMapping("/recomendar")
    public String recomendarMaterias(@RequestBody FiltroDTO dto) {
        return ragService.recomendarMaterias(dto.getIntereses(), dto.getCreditos(), dto.getTipo());
    }

    // DTO reglamento
    public static class QuestionDTO {
        private String question;
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
    }

    // DTO filtros para recomendación
    public static class FiltroDTO {
        private String intereses;
        private Object creditos; // puede venir número o "cualquiera"
        private String tipo;     // "cualquiera", "énfasis", "electivas", "complementarias"

        public String getIntereses() { return intereses; }
        public void setIntereses(String intereses) { this.intereses = intereses; }

        public Object getCreditos() { return creditos; }
        public void setCreditos(Object creditos) { this.creditos = creditos; }

        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
    }
}