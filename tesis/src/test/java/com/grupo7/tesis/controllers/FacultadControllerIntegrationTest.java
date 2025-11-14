package com.grupo7.tesis.controllers;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import com.grupo7.tesis.dtos.AuthResponseDTO;
import com.grupo7.tesis.dtos.LoginDTO;
import com.grupo7.tesis.dtos.RegisterDTO;
import com.grupo7.tesis.models.Estudiante;
import com.grupo7.tesis.models.Facultad;
import com.grupo7.tesis.models.Pensum;
import com.grupo7.tesis.repositories.EstudianteRepository;
import com.grupo7.tesis.repositories.FacultadRepository;
import com.grupo7.tesis.repositories.PensumRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class FacultadControllerIntegrationTest {

     @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private PensumRepository pensumRepository;

    @Autowired
    private FacultadRepository facultadRepository;

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
    //Revisar
    void testGetAllFacultades() {
        String token = tokenGenerado();
        // create two new facultades
        Facultad f1 = new Facultad("Facultad A");
        Facultad f2 = new Facultad("Facultad B");

        Facultad creada1 = webTestClient.post()
            .uri("/api/facultades")
            .header("Authorization", "Bearer " + token)
            .bodyValue(f1)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Facultad.class)
            .returnResult()
            .getResponseBody();

        Facultad creada2 = webTestClient.post()
            .uri("/api/facultades")
            .header("Authorization", "Bearer " + token)
            .bodyValue(f2)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Facultad.class)
            .returnResult()
            .getResponseBody();

        List<Facultad> facultades = webTestClient.get()
            .uri("/api/facultades")
            .header("Authorization", "Bearer " + token)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Facultad.class)
            .returnResult()
            .getResponseBody();

        long matches = facultades.stream()
            .filter(f -> (creada1 != null && creada1.getId() != null && creada1.getId().equals(f.getId()))
                      || (creada2 != null && creada2.getId() != null && creada2.getId().equals(f.getId())))
            .count();

        Assertions.assertEquals(2, matches, "Debe encontrarse exactamente las 2 facultades creadas");
    }

    @Test
    void testGetFacultadById() {
        String token = tokenGenerado();
        Facultad input = new Facultad("Facultad de Prueba");

        Facultad creada = webTestClient.post()
            .uri("/api/facultades")
            .header("Authorization", "Bearer " + token)
            .bodyValue(input)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Facultad.class)
            .returnResult()
            .getResponseBody();

        assertNotNull(creada, "La facultad creada no debe ser nula");
        assertNotNull(creada.getId(), "La facultad creada debe tener id");

        Facultad fetched = webTestClient.get()
            .uri("/api/facultades/{id}", creada.getId())
            .header("Authorization", "Bearer " + token)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Facultad.class)
            .returnResult()
            .getResponseBody();

        assertNotNull(fetched, "La facultad obtenida no debe ser nula");
        Assertions.assertEquals(creada.getId(), fetched.getId());
        Assertions.assertEquals(creada.getNombre(), fetched.getNombre());
    }

    @Test
    void testCrearFacultad() {
        String token = tokenGenerado();
        Facultad input = new Facultad("Facultad Nueva");

        Facultad creada = webTestClient.post()
            .uri("/api/facultades")
            .header("Authorization", "Bearer " + token)
            .bodyValue(input)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Facultad.class)
            .returnResult()
            .getResponseBody();

        assertNotNull(creada, "La facultad creada no debe ser nula");
        assertNotNull(creada.getId(), "La facultad creada debe tener id");
        Assertions.assertEquals(input.getNombre(), creada.getNombre());
    }

    @Test
    void testActualizarFacultad() {
        String token = tokenGenerado();
        Facultad input = new Facultad("Facultad Original");

        Facultad creada = webTestClient.post()
            .uri("/api/facultades")
            .header("Authorization", "Bearer " + token)
            .bodyValue(input)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Facultad.class)
            .returnResult()
            .getResponseBody();

        assertNotNull(creada, "La facultad creada no debe ser nula");
        assertNotNull(creada.getId(), "La facultad creada debe tener id");

        Facultad updateData = new Facultad("Facultad Actualizada");

        Facultad updated = webTestClient.put()
            .uri("/api/facultades/{id}", creada.getId())
            .header("Authorization", "Bearer " + token)
            .bodyValue(updateData)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Facultad.class)
            .returnResult()
            .getResponseBody();

        assertNotNull(updated, "La facultad actualizada no debe ser nula");
        Assertions.assertEquals(creada.getId(), updated.getId());
        Assertions.assertEquals(updateData.getNombre(), updated.getNombre());
    }


    @Test
    void testDeleteFacultad() {
        String token = tokenGenerado();
        Facultad input = new Facultad("Facultad a eliminar");

        Facultad creada = webTestClient.post()
            .uri("/api/facultades")
            .header("Authorization", "Bearer " + token)
            .bodyValue(input)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Facultad.class)
            .returnResult()
            .getResponseBody();

        assertNotNull(creada, "La facultad creada no debe ser nula");
        assertNotNull(creada.getId(), "La facultad creada debe tener id");

        Facultad deleted = webTestClient.delete()
            .uri("/api/facultades/{id}", creada.getId())
            .header("Authorization", "Bearer " + token)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Facultad.class)
            .returnResult()
            .getResponseBody();

        assertNotNull(deleted, "La facultad eliminada no debe ser nula");
        Assertions.assertEquals(creada.getId(), deleted.getId());
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
