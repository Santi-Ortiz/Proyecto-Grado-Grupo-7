package com.grupo7.tesis.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.grupo7.tesis.dtos.AuthResponseDTO;
import com.grupo7.tesis.dtos.LoginDTO;
import com.grupo7.tesis.dtos.RegisterDTO;
import com.grupo7.tesis.models.Facultad;
import com.grupo7.tesis.models.Materia;
import com.grupo7.tesis.models.Pensum;
import com.grupo7.tesis.repositories.FacultadRepository;
import com.grupo7.tesis.repositories.MateriaRepository;
import com.grupo7.tesis.repositories.PensumRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class MateriaControllerIntegrationTest {

    private String SERVER_URL;

    private String TEST_EMAIL;

    private String TEST_PASSWORD;

    private String token;

    @Autowired
    private FacultadRepository facultadRepository;

    @Autowired 
    private PensumRepository pensumRepository;

    @Autowired
    private MateriaRepository materiaRepository;

    @Autowired
    private WebTestClient webTestClient;

    public MateriaControllerIntegrationTest(@Value("${server.port}") int port) {
        this.SERVER_URL = "http://localhost:" + port;
        this.TEST_EMAIL = "test.email@javeriana.edu.co";
        this.TEST_PASSWORD = "password";
    }

    @BeforeEach
    void init() {
        facultadRepository.save(new Facultad("Facultad de Ingeniería"));
        pensumRepository.save(new Pensum("Ingenieria de Sistemas", 138L, 8L));
        crearUsuarioBase();
        crearMateriaBase();
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

    void crearMateriaBase() {
        webTestClient.post()
            .uri(SERVER_URL + "/api/materias")
            .header("Authorization", "Bearer " + this.token)
            .bodyValue(new Materia("1295", "Cálculo Diferencial", 3, 1, null, null, "nucleoCienciasBasicas", null))
            .exchange()
            .expectStatus().isCreated();

        webTestClient.post()
            .uri(SERVER_URL + "/api/materias")
            .header("Authorization", "Bearer " + this.token)
            .bodyValue(new Materia("1296", "Cálculo Integral", 3, 1, null, null, "nucleoCienciasBasicas", null))
            .exchange()
            .expectStatus().isCreated();

        webTestClient.post()
            .uri(SERVER_URL + "/api/materias")
            .header("Authorization", "Bearer " + this.token)
            .bodyValue(new Materia("1297", "Cálculo Vectorial", 3, 1, null, null, "nucleoCienciasBasicas", null))
            .exchange()
            .expectStatus().isCreated();
    }

    @Test
    void obtenerMaterias() {
        webTestClient.get()
            .uri(SERVER_URL+ "/api/materias")
            .header("Authorization", "Bearer " + this.token)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Materia.class)
            .value(
                materias -> {
                    assertEquals(3, materias.size());
                }
            );
    }

    @Test
    void crearMateria() {
        webTestClient.post()
            .uri(SERVER_URL + "/api/materias")
            .header("Authorization", "Bearer " + this.token)
            .bodyValue(new Materia("33698", "Introducción a la Programación", 3, 1, null, null, "nucleoIngenieria", null))
            .exchange()
            .expectStatus().isCreated()
            .expectBody(Materia.class)
            .value(
                materia -> {
                    Materia materiaEsperada = materiaRepository.findByCodigo("33698").orElse(null);
                    assertEquals(materiaEsperada.getCodigo(), materia.getCodigo());
                }
            );
    }

    @Test
    void crearMateriasDesdeJson() {
        webTestClient.post()
            .uri(SERVER_URL + "/api/materias/cargar-desde-json")
            .header("Authorization", "Bearer " + this.token)
            .exchange()
            .expectStatus().isCreated()
            .expectBodyList(Materia.class)
            .value(
                materias -> {
                    assertEquals(52, materias.size());
                }
            );

    }

    @Test
    void actualizarMateria(){

        webTestClient.put()
            .uri(SERVER_URL + "/api/materias/1")
            .header("Authorization", "Bearer " + this.token)
            .bodyValue(new Materia("1295", "Cálculo I", 4, 1, null, null, "nucleoCienciasBasicas", null))
            .exchange()
            .expectStatus().isOk()
            .expectBody(Materia.class)
            .value(
                materia -> {
                    assertEquals("Cálculo I", materia.getNombre());
                    assertEquals(4, materia.getCreditos());
                }
            );

    }

    @Test
    void eliminarMateria() {
        webTestClient.delete()
            .uri(SERVER_URL + "/api/materias/1")
            .header("Authorization", "Bearer " + this.token)
            .exchange()
            .expectStatus().isNoContent()
            .expectBody(Void.class)
            .value(
                response -> {
                    assertEquals(null, materiaRepository.findById(1L).orElse(null));
                }
            );

    }

}
