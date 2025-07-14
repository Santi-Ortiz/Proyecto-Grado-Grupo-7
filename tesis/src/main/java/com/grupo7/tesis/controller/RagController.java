package com.grupo7.tesis.controller;

import com.grupo7.tesis.service.RagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rag")
@CrossOrigin(origins = "*") // Permite acceso desde el frontend
public class RagController {

    private final RagService ragService;

    @Autowired
    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/consulta")
    public String consultaRag(@RequestBody String pregunta) {
        return ragService.obtenerRespuestaRag(pregunta);
    }
}