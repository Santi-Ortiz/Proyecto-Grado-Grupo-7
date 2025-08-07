package com.grupo7.tesis.service;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.HashMap;
import java.util.Map;

@Component
public class RagCliente {

    private final RestTemplate restTemplate = new RestTemplate();

    private final String RAG_REGLAMENTO_URL = "http://localhost:8000/query";
    private final String RAG_MATERIAS_URL = "http://localhost:8000/recomendar-materias";

    // Consulta al endpoint del reglamento (clave: "question")
    public String queryRag(String question) {
        Map<String, String> request = new HashMap<>();
        request.put("question", question);  // FastAPI espera "question"

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                RAG_REGLAMENTO_URL,
                HttpMethod.POST,
                entity,
                Map.class
        );

        Map<String, Object> body = response.getBody();
        if (body != null && body.containsKey("answer")) {
            return body.get("answer").toString();
        } else {
            return "No se pudo obtener respuesta del servicio RAG.";
        }
    }

    // Consulta al endpoint de recomendación de materias (clave: "intereses")
    public String queryMaterias(String intereses) {
        Map<String, String> request = new HashMap<>();
        request.put("intereses", intereses);  // FastAPI espera "intereses"

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                RAG_MATERIAS_URL,
                HttpMethod.POST,
                entity,
                String.class
        );

        return response.getBody(); // Retorna directamente el JSON string
    }
}