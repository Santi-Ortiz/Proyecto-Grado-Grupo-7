package com.grupo7.tesis.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.grupo7.tesis.services.RagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rag")
@CrossOrigin(origins = "*")
public class RagController {

    private final RagService ragService;

    @Autowired
    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    // ========= Reglamento / QA =========
    @PostMapping
    public String consultarRag(@RequestBody QuestionDTO question) {
        return ragService.obtenerRespuestaRag(question.getQuestion());
    }

    // ========= Recomendación de materias =========
    @PostMapping("/recomendar")
    public String recomendarMaterias(@RequestBody FiltroDTO dto) {
        return ragService.recomendarMaterias(
                dto.getIntereses(),
                dto.getCreditos(),
                dto.getCreditosMin(),
                dto.getCreditosMax(),
                dto.getTipo()
        );
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
        private Object creditos;
        private String tipo;

        @JsonProperty("creditos_min")
        private Integer creditosMin;

        @JsonProperty("creditos_max")
        private Integer creditosMax;

        public String getIntereses() { return intereses; }
        public void setIntereses(String intereses) { this.intereses = intereses; }

        public Object getCreditos() { return creditos; }
        public void setCreditos(Object creditos) { this.creditos = creditos; }

        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }

        public Integer getCreditosMin() { return creditosMin; }
        public void setCreditosMin(Integer creditosMin) { this.creditosMin = creditosMin; }

        public Integer getCreditosMax() { return creditosMax; }
        public void setCreditosMax(Integer creditosMax) { this.creditosMax = creditosMax; }
    }
}
