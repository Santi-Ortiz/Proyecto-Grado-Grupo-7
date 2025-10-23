package com.grupo7.tesis.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.grupo7.tesis.services.RagService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class RagControllerIntegrationTest {

    private final String SERVER_URL;

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private RagService ragService;

    public RagControllerIntegrationTest(@Value("${server.port}") int port) {
        this.SERVER_URL = "http://localhost:" + port;
    }

    @Test
    void consultarRag_ok() {
        String pregunta = "¿Cómo se calcula el promedio acumulado?";
        String respuestaMock = "El promedio acumulado se calcula ponderando las notas por los créditos.";
        when(ragService.obtenerRespuestaRag(pregunta)).thenReturn(respuestaMock);

        Map<String, Object> body = new HashMap<>();
        body.put("question", pregunta);

        webTestClient.post()
            .uri(SERVER_URL + "/api/rag")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .value(resp -> assertEquals(respuestaMock, resp));
    }

    @Test
    void recomendarMaterias_ok() {
        String intereses = "sistemas distribuidos";
        Object creditos = "cualquiera";
        String tipo = "cualquiera";

        String jsonMock = """
            {
              "materias": [
                { "nombre": "Sistemas Distribuidos", "grado": "Pregrado", "id": "SD-101", "creditos": 3, "numero_catalogo": "1299", "numero_oferta": "2025-1" }
              ],
              "explicacion": "Se recomienda por afinidad con sistemas distribuidos y microservicios.",
              "materias_sugeridas": [],
              "explicacion_sugeridas": "",
              "observaciones": { "fuente": "faiss", "advertencias": [] }
            }
            """;

        when(ragService.recomendarMaterias(intereses, creditos, tipo)).thenReturn(jsonMock);

        Map<String, Object> body = new HashMap<>();
        body.put("intereses", intereses);
        body.put("creditos", creditos);
        body.put("tipo", tipo);

        webTestClient.post()
            .uri(SERVER_URL + "/api/rag/recomendar")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .value(resp -> assertEquals(removeSpaces(jsonMock), removeSpaces(resp)));
    }

    private String removeSpaces(String s) {
        return s == null ? null : s.replaceAll("\\s+", "");
    }
}
