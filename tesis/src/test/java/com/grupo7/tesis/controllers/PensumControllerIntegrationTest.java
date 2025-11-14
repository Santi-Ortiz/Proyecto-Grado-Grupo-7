package com.grupo7.tesis.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

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
import com.grupo7.tesis.repositories.PensumRepository;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class PensumControllerIntegrationTest {

    private String SERVER_URL;

    private String TEST_EMAIL;

    private String TEST_PASSWORD;

    private String token;

    @Autowired
    private FacultadRepository facultadRepository;

    @Autowired
    private PensumRepository pensumRepository;

    @Autowired
    private WebTestClient webTestClient;

    public PensumControllerIntegrationTest(@Value("${server.port}") int port) {
        this.SERVER_URL = "http://localhost:" + port;
        this.TEST_EMAIL = "test.email@javeriana.edu.co";
        this.TEST_PASSWORD = "password";
    }

    @BeforeEach
    void init() {
        facultadRepository.save(new Facultad("Facultad de Ingeniería"));
        pensumRepository.save(new Pensum("Ingenieria de Sistemas", 138L, 8L));
        crearUsuarioBase();
        crearMateriasBase();
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


    void crearPensumBase() {
        Pensum pensum = new Pensum("Ingenieria Electrónica", 138L, 8L);

        webTestClient.post()
            .uri(SERVER_URL + "/api/pensums")
            .header("Authorization", "Bearer " + this.token)
            .bodyValue(pensum)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(Pensum.class);
    }

    void crearMateriasBase() {
        webTestClient.post()
            .uri(SERVER_URL + "/api/materias")
            .header("Authorization", "Bearer " + this.token)
            .bodyValue(new Materia("1295", "Cálculo Diferencial", 3, 1, null, null, "nucleoCienciasBasicas", null))
            .exchange()
            .expectStatus().isCreated();

        webTestClient.post()
            .uri(SERVER_URL + "/api/materias")
            .header("Authorization", "Bearer " + this.token)
            .bodyValue(new Materia("1297", "Cálculo Integral", 3, 1, new ArrayList<>(List.of("1295")), null, "nucleoCienciasBasicas", null))
            .exchange()
            .expectStatus().isCreated();

        webTestClient.post()
            .uri(SERVER_URL + "/api/materias")
            .header("Authorization", "Bearer " + this.token)
            .bodyValue(new Materia("1299", "Cálculo Vectorial", 3, 1, new ArrayList<>(List.of("1297")), null, "nucleoCienciasBasicas", null))
            .exchange()
            .expectStatus().isCreated();
    }

    @Test
    void obtenerPensum() {
        webTestClient.get()
            .uri(SERVER_URL + "/api/pensums/1")
            .header("Authorization", "Bearer " + this.token)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Pensum.class)
            .value(
                pensum -> {
                    System.out.println(pensum.getCarrera());
                    Pensum pensumEsperado = pensumRepository.findByCarrera("Ingenieria de Sistemas");
                    assertEquals(pensumEsperado.getCarrera(), pensum.getCarrera());
                }
            );

    }

    @Test
    void crearPensum() {
        webTestClient.post()
            .uri(SERVER_URL + "/api/pensums")
            .header("Authorization", "Bearer " + this.token)
            .bodyValue(new Pensum("Ingenieria Industrial", 138L, 8L))
            .exchange()
            .expectStatus().isCreated()
            .expectBody(Pensum.class)
            .value(
                pensum -> {
                    Pensum pensumEsperado = pensumRepository.findByCarrera("Ingenieria Industrial");
                    assertEquals(pensumEsperado.getCarrera(), pensum.getCarrera());
                }
            );

    }

    @Test
    void actualizarPensum() {
        webTestClient.put()
            .uri(SERVER_URL + "/api/pensums/1")
            .header("Authorization", "Bearer " + this.token)
            .bodyValue(new Pensum("Bioingenieria", 138L, 8L))
            .exchange()
            .expectStatus().isCreated()
            .expectBody(Pensum.class)
            .value(
                pensum -> {
                    assertEquals("Bioingenieria", pensum.getCarrera());
                }
            );
    }

    @Test
    void asociarMateriasAPensum() {
        webTestClient.post()
            .uri(SERVER_URL + "/api/pensums/1/materias/1")
            .header("Authorization", "Bearer " + this.token)
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .value(
                response -> {
                    assertEquals("Materia asociada exitosamente al pensum", response);
                }
            );
    }

    @Test
    void eliminarMateriaDePensum(){
        webTestClient.post()
            .uri(SERVER_URL + "/api/pensums/1/materias/1")
            .header("Authorization", "Bearer " + this.token)
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .value(
                response -> {
                    assertEquals("Materia asociada exitosamente al pensum", response);
                }
            );

        webTestClient.delete()
            .uri(SERVER_URL + "/api/pensums/1/materias/1")
            .header("Authorization", "Bearer " + this.token)
            .exchange()
            .expectStatus().isNoContent()
            .expectBody(Void.class);
    }

}
