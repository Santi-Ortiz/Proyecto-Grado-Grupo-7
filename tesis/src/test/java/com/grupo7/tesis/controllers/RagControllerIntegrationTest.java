package com.grupo7.tesis.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
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

import com.grupo7.tesis.dtos.AuthResponseDTO;
import com.grupo7.tesis.dtos.LoginDTO;
import com.grupo7.tesis.dtos.RegisterDTO;
import com.grupo7.tesis.models.Facultad;
import com.grupo7.tesis.models.Pensum;
import com.grupo7.tesis.repositories.FacultadRepository;
import com.grupo7.tesis.repositories.PensumRepository;
import com.grupo7.tesis.services.RagService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class RagControllerIntegrationTest {

    private final String SERVER_URL;

    private String TEST_EMAIL;

    private String TEST_PASSWORD;

    @Autowired
    private PensumRepository pensumRepository;

    @Autowired
    private FacultadRepository facultadRepository;

    @Autowired
    private WebTestClient webTestClient;

    private String token;

    @MockBean
    private RagService ragService;

    String CORREO = "test1@javeriana.edu.co";
    String PASSWORD = "password";

    public RagControllerIntegrationTest(@Value("${server.port}") int port) {
        this.SERVER_URL = "http://localhost:" + port;
        this.TEST_EMAIL = "test.email@javeriana.edu.co";
        this.TEST_PASSWORD = "password";
    }

    @BeforeEach
    void init(){
        facultadRepository.save(new Facultad("Facultad de Ingenieria"));
        pensumRepository.save(new Pensum("Ingenieria de Sistemas", 138L, 8L));
        crearUsuarioBase();
    }

    void crearUsuarioBase() {
        RegisterDTO registerDTO = new RegisterDTO(
            "87654321", 
            TEST_EMAIL, 
            "Ingeniería de Sistemas", 
            TEST_PASSWORD, 
            2021L, 
            "Juan", 
            "Perez", 
            "Gomez"
        );

        webTestClient.post()
            .uri(SERVER_URL + "/api/auth/register")
            .bodyValue(registerDTO)
            .exchange()
            .expectStatus().isOk();

        
        LoginDTO login = new LoginDTO(TEST_EMAIL, TEST_PASSWORD);
        
        AuthResponseDTO response = webTestClient.post()
            .uri(SERVER_URL + "/api/auth/login")
            .bodyValue(login)
            .exchange()
            .expectStatus().isOk()
            .expectBody(AuthResponseDTO.class)
            .returnResult()
            .getResponseBody();

        if(response != null) {
            this.token = response.getAccessToken();
        }
        
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
            .header("Authorization", "Bearer " + this.token)
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
            .header("Authorization", "Bearer " + this.token)
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
