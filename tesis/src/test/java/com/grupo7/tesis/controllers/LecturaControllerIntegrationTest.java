package com.grupo7.tesis.controllers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import com.grupo7.tesis.dtos.AuthResponseDTO;
import com.grupo7.tesis.dtos.LoginDTO;
import com.grupo7.tesis.dtos.RegisterDTO;
import com.grupo7.tesis.models.Estudiante;
import com.grupo7.tesis.models.Facultad;
import com.grupo7.tesis.models.Pensum;
import com.grupo7.tesis.repositories.EstudianteRepository;
import com.grupo7.tesis.repositories.FacultadRepository;
import com.grupo7.tesis.repositories.PensumRepository;
import com.grupo7.tesis.repositories.InformeAvanceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.hamcrest.Matchers;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import com.grupo7.tesis.models.Progreso;
    
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class LecturaControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private PensumRepository pensumRepository;

    @Autowired
    private FacultadRepository facultadRepository;

    @Autowired
    private InformeAvanceRepository informeAvanceRepository;

    String CORREO = "test1@javeriana.edu.co";
    String PASSWORD = "password";
    private Pensum savedPensum;
    private Facultad savedFacultad;

    @BeforeEach
    void init(){
        savedFacultad = facultadRepository.save(new Facultad("Facultad de Ingenieria"));
        savedPensum = pensumRepository.save(new Pensum("Ingenieria de Sistemas", 138L, 8L));
        crearUsuarioBase();
    }

    void crearUsuarioBase() {
        RegisterDTO registerDTO = new RegisterDTO(
            "87654321", 
            CORREO, 
            "Ingenieria de Sistemas", 
            PASSWORD, 
            2021L, 
            "Juan", 
            "Perez", 
            "Gomez"
        );

        webTestClient.post()
            .uri( "/api/auth/register")
            .bodyValue(registerDTO)
            .exchange()
            .expectStatus().isOk();
    }

    @AfterEach
    void cleanup(){
        Estudiante e = estudianteRepository.findByCorreo(CORREO);
        if (e != null) {
            try {
                try {
                    informeAvanceRepository.findAll().stream()
                        .filter(i -> i.getEstudiante() != null && i.getEstudiante().getId() != null && i.getEstudiante().getId().equals(e.getId()))
                        .forEach(i -> {
                            try {
                                informeAvanceRepository.delete(i);
                            } catch (Exception ex) {
                            }
                        });
                } catch (Exception ex) {
                }

                estudianteRepository.delete(e);
            } catch (Exception ex) {
            }
        }

        if (savedPensum != null) {
            try {
                pensumRepository.delete(savedPensum);
            } catch (Exception ex) {
            }
        }

        if (savedFacultad != null) {
            try {
                facultadRepository.delete(savedFacultad);
            } catch (Exception ex) {
            }
        }
    }

    @Test
    void testGuardarInformeAvance() throws Exception {
        ClassPathResource pdf = new ClassPathResource("informes/Informe.pdf");
        MultiValueMap<String, Object> multipartData = new LinkedMultiValueMap<>();
        multipartData.add("archivo", pdf);
        
        String token = tokenGenerado();

        var response = webTestClient.post()
            .uri("/guardarInforme")
            .headers(h -> h.setBearerAuth(token))
            .contentType(MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(multipartData))
            .exchange()
            .expectBody(Progreso.class)
            .returnResult();

        int status = response.getStatus().value();
        Assertions.assertEquals(200, status, "Expected 200 OK but got " + status);
        Assertions.assertEquals(8, response.getResponseBody().getSemestre(), "Expected semestre 8 but got " + response.getResponseBody().getSemestre());
    }

    @Test
    void testObtenerUltimoInforme() throws Exception {
        ClassPathResource pdf = new ClassPathResource("informes/Informe.pdf");
        MultiValueMap<String, Object> multipartData = new LinkedMultiValueMap<>();
        multipartData.add("archivo", pdf);
        
        String token = tokenGenerado();

        webTestClient.post()
            .uri("/guardarInforme")
            .headers(h -> h.setBearerAuth(token))
            .contentType(MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(multipartData))
            .exchange()
            .expectBody(Progreso.class)
            .returnResult();

        webTestClient.get()
            .uri("/ultimo-informe")
            .headers(h -> h.setBearerAuth(token))
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.nombreArchivo").value(Matchers.containsString("87654321"));
    }

    private String tokenGenerado(){
        LoginDTO login = new LoginDTO(CORREO, PASSWORD);

        AuthResponseDTO authResp = webTestClient.post()
            .uri("/api/auth/login")
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
            .bodyValue(login)
            .exchange()
            .expectStatus().isOk()
            .expectBody(AuthResponseDTO.class)
            .returnResult()
            .getResponseBody();

        String token = null;
        if (authResp != null && authResp.getAccessToken() != null) {
            token = authResp.getAccessToken();
        }
        return token;
    }

}
