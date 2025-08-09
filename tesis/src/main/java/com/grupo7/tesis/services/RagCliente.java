package com.grupo7.tesis.services;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.HashMap;
import java.util.Map;

@Component
public class RagCliente {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String RAG_SERVICE_URL = "http://localhost:8000/query";

    public String queryRag(String question) {
        Map<String, String> request = new HashMap<>();
        request.put("question", question);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                RAG_SERVICE_URL,
                HttpMethod.POST,
                entity,
                Map.class);

        Map<String, Object> body = response.getBody();
        if (body != null && body.containsKey("answer")) {
            return body.get("answer").toString();
        } else {
            return "No se pudo obtener respuesta del servicio RAG.";
        }
    }
}