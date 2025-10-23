package com.grupo7.tesis.controllers;

import org.junit.jupiter.api.Test;
import java.util.List;
import com.grupo7.tesis.dtos.AuthResponseDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import com.grupo7.tesis.dtos.LoginDTO;
import com.grupo7.tesis.dtos.RegisterDTO;
import com.grupo7.tesis.models.Facultad;
import com.grupo7.tesis.models.Pensum;
import com.grupo7.tesis.models.Proyeccion;
import com.grupo7.tesis.models.Estudiante;
import com.grupo7.tesis.repositories.EstudianteRepository;
import com.grupo7.tesis.repositories.ProyeccionRepository;
import com.grupo7.tesis.repositories.FacultadRepository;
import com.grupo7.tesis.repositories.PensumRepository;
import org.junit.jupiter.api.AfterEach;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ProyeccionControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private PensumRepository pensumRepository;

    @Autowired
    private FacultadRepository facultadRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private ProyeccionRepository proyeccionRepository;

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
                    List<Proyeccion> proyecciones = proyeccionRepository.findByestudianteId(e);
                    if (proyecciones != null && !proyecciones.isEmpty()) {
                        proyeccionRepository.deleteAll(proyecciones);
                    }
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
    void testObtenerTodasProyecciones() {
        String token = tokenGenerado();

        webTestClient.get()
            .uri("/api/proyecciones")
            .header("Authorization", "Bearer " + token)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Proyeccion.class);

    }

    @Test
    void testObtenerProyeccionPorId() {
        String token = tokenGenerado();
        Proyeccion proyeccion = crearProyeccionEjemplo();

        Proyeccion creadaProyeccion = webTestClient.post()
            .uri("/api/proyecciones")
            .header("Authorization", "Bearer " + token)
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
            .bodyValue(proyeccion)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Proyeccion.class)
            .returnResult()
            .getResponseBody();

        Assertions.assertNotNull(creadaProyeccion);
        Assertions.assertNotNull(creadaProyeccion.getId());

        Long proyeccionId = creadaProyeccion.getId();

        webTestClient.get()
            .uri("/api/proyecciones/{id}", proyeccionId)
            .header("Authorization", "Bearer " + token)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Proyeccion.class)
            .consumeWith(response -> {
                Proyeccion fetchedProyeccion = response.getResponseBody();
                Assertions.assertNotNull(fetchedProyeccion);
                Assertions.assertEquals(proyeccionId, fetchedProyeccion.getId());
                Assertions.assertEquals("int-test1", fetchedProyeccion.getNombreSimulacion());
            });
    }

    @Test
    void testCrearProyeccion() {
        String token = tokenGenerado();
        Proyeccion proyeccion = crearProyeccionEjemplo();

        Proyeccion creadaProyeccion = webTestClient.post()
            .uri("/api/proyecciones")
            .header("Authorization", "Bearer " + token)
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
            .bodyValue(proyeccion)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Proyeccion.class)
            .returnResult()
            .getResponseBody();

        Assertions.assertNotNull(creadaProyeccion);
        Assertions.assertNotNull(creadaProyeccion.getId());
        Assertions.assertEquals("int-test1", creadaProyeccion.getNombreSimulacion());
    }

    @Test
    void testActualizarProyeccion() {
        String token = tokenGenerado();
        Proyeccion proyeccion = crearProyeccionEjemplo();

        Proyeccion creadaProyeccion = webTestClient.post()
            .uri("/api/proyecciones")
            .header("Authorization", "Bearer " + token)
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
            .bodyValue(proyeccion)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Proyeccion.class)
            .returnResult()
            .getResponseBody();

        Assertions.assertNotNull(creadaProyeccion);
        Assertions.assertNotNull(creadaProyeccion.getId());

        Long proyeccionId = creadaProyeccion.getId();
        creadaProyeccion.setNombreSimulacion("updated-name");

        Proyeccion actualizadaProyeccion = webTestClient.put()
            .uri("/api/proyecciones/{id}", proyeccionId)
            .header("Authorization", "Bearer " + token)
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
            .bodyValue(creadaProyeccion)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Proyeccion.class)
            .returnResult()
            .getResponseBody();

        Assertions.assertNotNull(actualizadaProyeccion);
        Assertions.assertEquals("updated-name", actualizadaProyeccion.getNombreSimulacion());
    }

    @Test
    void testEliminarProyeccionConId() {
        String token = tokenGenerado();
        Proyeccion proyeccion = crearProyeccionEjemplo();

        Proyeccion creadaProyeccion = webTestClient.post()
            .uri("/api/proyecciones")
            .header("Authorization", "Bearer " + token)
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
            .bodyValue(proyeccion)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Proyeccion.class)
            .returnResult()
            .getResponseBody();

        Assertions.assertNotNull(creadaProyeccion);
        Assertions.assertNotNull(creadaProyeccion.getId());

        Long proyeccionId = creadaProyeccion.getId();

        webTestClient.delete()
            .uri("/api/proyecciones/eliminar/{id}", proyeccionId)
            .header("Authorization", "Bearer " + token)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Boolean.class)
            .consumeWith(response -> {
                Boolean eliminado = response.getResponseBody();
                Assertions.assertTrue(eliminado);
            });
    }

    @Test 
    void testEliminarProyeccionesDeEstudiante() {
        String token = tokenGenerado();
        Proyeccion proyeccion1 = crearProyeccionEjemplo();
        Proyeccion proyeccion2 = crearProyeccionEjemplo();
        proyeccion2.setNombreSimulacion("int-test2");

        webTestClient.post()
            .uri("/api/proyecciones")
            .header("Authorization", "Bearer " + token)
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
            .bodyValue(proyeccion1)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Proyeccion.class);

        webTestClient.post()
            .uri("/api/proyecciones")
            .header("Authorization", "Bearer " + token)
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
            .bodyValue(proyeccion2)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Proyeccion.class);

        webTestClient.delete()
            .uri("/api/proyecciones/eliminarTodo")
            .header("Authorization", "Bearer " + token)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Boolean.class)
            .consumeWith(response -> {
                Boolean eliminado = response.getResponseBody();
                Assertions.assertTrue(eliminado);
            });
    }

    @Test
    void testExisteProyeccionConNombre() {
        String token = tokenGenerado();
        Proyeccion proyeccion = crearProyeccionEjemplo();

        webTestClient.post()
            .uri("/api/proyecciones")
            .header("Authorization", "Bearer " + token)
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
            .bodyValue(proyeccion)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Proyeccion.class);

        String nombreBuscado = "int-test1";

        webTestClient.get()
            .uri("/api/proyecciones/existe/nombre/{nombre}", nombreBuscado)
            .header("Authorization", "Bearer " + token)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Boolean.class)
            .consumeWith(response -> {
                Boolean existe = response.getResponseBody();
                Assertions.assertTrue(existe);
            });
    }

    @Test
    void testObtenerProyeccionesEstudianteAutenticado() {
        String token = tokenGenerado();
        Proyeccion proyeccion = crearProyeccionEjemplo();

        webTestClient.post()
            .uri("/api/proyecciones")
            .header("Authorization", "Bearer " + token)
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
            .bodyValue(proyeccion)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Proyeccion.class);

        webTestClient.get()
            .uri("/api/proyecciones/mis-proyecciones")
            .header("Authorization", "Bearer " + token)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Proyeccion.class)
            .consumeWith(response -> {
                List<Proyeccion> proyecciones = response.getResponseBody();
                Assertions.assertNotNull(proyecciones);
                Assertions.assertFalse(proyecciones.isEmpty());
            });
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
        System.out.println(token);
        return token;
    }

    Proyeccion crearProyeccionEjemplo() {
        Proyeccion proyeccion = new Proyeccion();
        proyeccion.setSemestre(8);
        proyeccion.setNumMaxCreditos(20);
        proyeccion.setNumMaxMaterias(6);
        proyeccion.setNombreSimulacion("int-test1");
        proyeccion.setTipoMatricula("NORMAL");
        proyeccion.setPracticaProfesional(false);
        proyeccion.setPriorizaciones(new boolean[] {false, false, false, false, false, false});
        return proyeccion;
    }
}