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
@ActiveProfiles("test")
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
        Facultad facultad =facultadRepository.save(new Facultad("Facultad de Ingeniería"));
        Pensum pensum = pensumRepository.save(new Pensum("Ingenieria de Sistemas", 138L, 8L));
        estudianteRepository.save(new Estudiante("98765432", "test@javeriana.edu.co", "psswd123", "Pepito", "Andres", "Perez", "Gonzalez", "Ingeniería de Sistemas", 2023L,pensum, facultad));
        estudianteRepository.save(new Estudiante("35789654", "test_prueba@javeriana.edu.co", "psswd123", "Pepito", "Andres", "Perez", "Gonzalez", "Ingeniería de Sistemas", 2023L,pensum, facultad));
    }

    @Test
    void obtenerEstudiantes() {

        webTestClient.get()
            .uri(SERVER_URL + "/api/estudiantes")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Estudiante.class)
            .value(
                estudiantes -> {
                    assertEquals(2, estudiantes.size());
                }
            );

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
                    assertEquals(estudianteRepository.findByCodigo("12345678").getCorreo(), estudiante.getCorreo());
                }
            );
    }

    @Test
    void actualizarEstudiante() {

        webTestClient.put()
            .uri(SERVER_URL + "/api/estudiantes/1")
            .bodyValue(new EstudianteDTO("12345678", "estudiante_javeriana@javeriana.edu.co", "psswd123", "Pepito", "Andres", "Perez", "Gonzalez", "Ingeniería de Sistemas", 2023L))
            .exchange()
            .expectStatus().isOk()
            .expectBody(Estudiante.class)
            .value(
                estudiante -> {
                    assertEquals(estudianteRepository.findByCorreo("estudiante_javeriana@javeriana.edu.co").getCorreo(), estudiante.getCorreo());
                }
            );
    }

    @Test
    void eliminarEstudiante() {
        webTestClient.delete()
            .uri(SERVER_URL + "/api/estudiantes/1")
            .exchange()
            .expectStatus().isNoContent()
            .expectBody(Void.class)
            .value(
                response -> {
                    assertEquals(null, estudianteRepository.findById(1L).orElse(null));
                }
            );
    }

    @Test
    void obtenerPensumEstudiante() {
        webTestClient.get()
            .uri(SERVER_URL + "/api/estudiantes/1/pensum")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Pensum.class)
            .value(
                pensum -> {
                    Pensum pensumEsperado = pensumRepository.findById(1L).orElse(null);
                    assertEquals(pensumEsperado.getId(), pensum.getId());
                }
            );
    }

    @Test
    void obtenerFacultadEstudiante() {
        webTestClient.get()
            .uri(SERVER_URL + "/api/estudiantes/1/facultad")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Facultad.class)
            .value(
                facultad -> {
                    Facultad facultadEsperada = facultadRepository.findById(1L).orElse(null);
                    assertEquals(facultadEsperada.getId(), facultad.getId());
                }
            );
    }

}
