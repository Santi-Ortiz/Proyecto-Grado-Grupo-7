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

import com.grupo7.tesis.dtos.EstudianteDTO;
import com.grupo7.tesis.models.Estudiante;
import com.grupo7.tesis.models.Facultad;
import com.grupo7.tesis.models.Pensum;
import com.grupo7.tesis.repositories.EstudianteRepository;
import com.grupo7.tesis.repositories.FacultadRepository;
import com.grupo7.tesis.repositories.PensumRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class EstudianteControllerIntegrationTest {

    private String SERVER_URL;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private FacultadRepository facultadRepository;

    @Autowired 
    private PensumRepository pensumRepository;

    @Autowired
    private WebTestClient webTestClient;

    public EstudianteControllerIntegrationTest(@Value("${server.port}") int port) {
        this.SERVER_URL = "http://localhost:" + port;
    }

    @BeforeEach
    void init() {
        facultadRepository.save(new Facultad("Facultad de Ingeniería"));
        pensumRepository.save(new Pensum("Ingenieria de Sistemas", 138L, 8L));
    }

    @Test
    void crearEstudiante() {

        webTestClient.post()
            .uri(SERVER_URL + "/api/estudiantes")
            .bodyValue(new EstudianteDTO("12345678", "prueba@javeriana.edu.co", "psswd123", "Pepito", "Andres", "Perez", "Gonzalez", "Ingeniería de Sistemas", 2023L))
            .exchange()
            .expectStatus().isCreated()
            .expectBody(Estudiante.class)
            .value(
                estudiante -> {
                    assertEquals(estudianteRepository.findByCodigo("12345678"), estudiante);
                }
            );
    }

    @Test
    void crearEstudiante_CorreoDuplicado() {

        Estudiante estudianteCreado = webTestClient.post()
            .uri(SERVER_URL + "/api/estudiantes")
            .bodyValue(new EstudianteDTO("12345678", "prueba@javeriana.edu.co", "psswd123", "Pepito", "Andres", "Perez", "Gonzalez", "Ingeniería de Sistemas", 2023L))
            .exchange()
            .expectStatus().isCreated()
            .expectBody(Estudiante.class)
            .value(
                estudiante -> {
                    assertEquals(estudianteRepository.findByCodigo("12345678"), estudiante);
                }
            );

        webTestClient.post()
            .uri(SERVER_URL + "/api/estudiantes")
            .bodyValue(new EstudianteDTO("87654321", "prueba@javeriana.edu.co", "psswd456", "Juanito", "Luis", "Martinez", "Lopez", "Ingeniería de Sistemas", 2023L))
            .exchange()
            .expectStatus().is5xxServerError();
    }

}
