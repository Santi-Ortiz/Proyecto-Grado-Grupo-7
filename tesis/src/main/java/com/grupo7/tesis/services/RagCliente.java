package com.grupo7.tesis.services;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.web.client.RestClientResponseException;

import java.util.HashMap;
import java.util.Map;

@Component
public class RagCliente {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String RAG_REGLAMENTO_URL = "http://localhost:8000/query";
    private static final String RAG_MATERIAS_URL   = "http://localhost:8000/recomendar-materias";

    public String queryRag(String question) {
        Map<String, String> request = new HashMap<>();
        request.put("question", question);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                RAG_REGLAMENTO_URL, HttpMethod.POST, entity, Map.class
        );

        Map<String, Object> body = response.getBody();
        if (body != null && body.containsKey("answer")) {
            return body.get("answer").toString();
        }
        return "No se pudo obtener respuesta del servicio RAG.";
    }

    public String queryMaterias(String intereses,
                                Object creditos,
                                Integer creditosMin,
                                Integer creditosMax,
                                String tipo) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("intereses", intereses);

            if (creditos != null) {
                request.put("creditos", creditos);
            }

            if (creditosMin != null) {
                request.put("creditos_min", creditosMin);
            }
            if (creditosMax != null) {
                request.put("creditos_max", creditosMax);
            }

            request.put("tipo", (tipo == null || tipo.isBlank()) ? "cualquiera" : tipo);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    RAG_MATERIAS_URL, HttpMethod.POST, entity, String.class
            );

            return response.getBody();
        } catch (RestClientResponseException e) {
            return "{ \"materias\": [], \"explicacion\": \"No fue posible obtener recomendaciones en este momento.\" }";
        }
    }

    public String queryMaterias(String intereses, Object creditos, String tipo) {
        return queryMaterias(intereses, creditos, null, null, tipo);
    }
}
