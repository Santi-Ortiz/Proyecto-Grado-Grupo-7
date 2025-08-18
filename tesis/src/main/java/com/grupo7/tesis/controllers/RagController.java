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
    public String recomendarMaterias(@RequestBody InteresesDTO dto) {
        return ragService.recomendarMaterias(dto.getIntereses(), dto.getCreditos());
    }

    // DTO para preguntas sobre el reglamento
    public static class QuestionDTO {
        private String question;
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
    }

    // DTO para recomendación: intereses + creditos
    public static class InteresesDTO {
        private String intereses;
        private Integer creditos; // 👈 nuevo

        public String getIntereses() { return intereses; }
        public void setIntereses(String intereses) { this.intereses = intereses; }

        public Integer getCreditos() { return creditos; }
        public void setCreditos(Integer creditos) { this.creditos = creditos; }
    }
}