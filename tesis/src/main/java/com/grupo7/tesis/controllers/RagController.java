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

    // Clase estática interna que actúa como DTO para recibir el cuerpo del POST
    public static class QuestionDTO {
        private String question;

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }
    }
}

/*
 * package com.grupo7.tesis.controller;
 * 
 * import com.grupo7.tesis.service.RagService;
 * import org.springframework.beans.factory.annotation.Autowired;
 * import org.springframework.web.bind.annotation.*;
 * 
 * @RestController
 * 
 * @RequestMapping("/rag")
 * 
 * @CrossOrigin(origins = "*") // Permite acceso desde el frontend
 * public class RagController {
 * 
 * private final RagService ragService;
 * 
 * @Autowired
 * public RagController(RagService ragService) {
 * this.ragService = ragService;
 * }
 * 
 * @PostMapping("/consulta")
 * public String consultaRag(@RequestBody String pregunta) {
 * return ragService.obtenerRespuestaRag(pregunta);
 * }
 * }
 */