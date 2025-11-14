package com.grupo7.tesis.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.grupo7.tesis.dtos.AuthResponseDTO;
import com.grupo7.tesis.dtos.LoginDTO;
import com.grupo7.tesis.dtos.RegisterDTO;
import com.grupo7.tesis.models.Facultad;
import com.grupo7.tesis.models.Pensum;
import com.grupo7.tesis.repositories.FacultadRepository;
import com.grupo7.tesis.repositories.PensumRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class AuthControllerIntegrationTest {

    private String SERVER_URL;

    private String TEST_EMAIL;

    private String TEST_PASSWORD;

    @Autowired
    private FacultadRepository facultadRepository;

    @Autowired 
    private PensumRepository pensumRepository;

    @Autowired
    private WebTestClient webTestClient;

    public AuthControllerIntegrationTest(@Value("${server.port}") int port) {
        this.SERVER_URL = "http://localhost:" + port;
        this.TEST_EMAIL = "test.email@javeriana.edu.co";
        this.TEST_PASSWORD = "password";
    }

    @BeforeEach
    void init() {
        facultadRepository.save(new Facultad("Facultad de Ingeniería"));
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
    }


    @Test
    void registarUsuario(){

        webTestClient.post()
            .uri(SERVER_URL + "/api/auth/register")
            .bodyValue(new RegisterDTO("12345678", "prueba@javeriana.edu.co", "Ingeniería de Sistemas", "psswd123", 2022L, "Pepito", "Andres", "Perez", "Gonzalez"))
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .value(
                usuario -> {
                    assertEquals("Usuario registrado exitosamente", usuario);
                }
            );

    }

    @Test
    void iniciarSesion() {

        LoginDTO login = new LoginDTO(TEST_EMAIL, TEST_PASSWORD);
        
        webTestClient.post()
            .uri("/api/auth/login")
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
            .bodyValue(login)
            .exchange()
            .expectStatus().isOk()
            .expectBody(AuthResponseDTO.class)
            .value(
                response -> {
                    assertEquals(true, response.getAccessToken() != null && !response.getAccessToken().isEmpty());
                }
            );
    }

    @Test
    void cerrarSesion() {

        webTestClient.post()
            .uri("/api/auth/logout")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class).isEqualTo("Logout exitoso")
            .value(
                response -> {
                    assertEquals("Logout exitoso", response);
                }
            );

    }

    

    
}