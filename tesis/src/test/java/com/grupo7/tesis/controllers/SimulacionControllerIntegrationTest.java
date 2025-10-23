package com.grupo7.tesis.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import com.grupo7.tesis.dtos.AuthResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import java.time.Duration;
import java.util.Map;
import java.util.function.Function;
import org.springframework.boot.test.mock.mockito.SpyBean;
import com.grupo7.tesis.services.SimulacionAsyncService;
import com.grupo7.tesis.services.SimulacionJobService;
import com.grupo7.tesis.dtos.LoginDTO;
import com.grupo7.tesis.dtos.MateriaDTO;
import com.grupo7.tesis.dtos.RegisterDTO;
import com.grupo7.tesis.dtos.SimulacionDTO;
import com.grupo7.tesis.models.Estudiante;
import com.grupo7.tesis.models.Facultad;
import com.grupo7.tesis.models.Materia;
import com.grupo7.tesis.models.Pensum;
import com.grupo7.tesis.models.Progreso;
import com.grupo7.tesis.models.Proyeccion;
import com.grupo7.tesis.models.Simulacion;
import com.grupo7.tesis.models.SimulacionJob;
import com.grupo7.tesis.repositories.EstudianteRepository;
import com.grupo7.tesis.repositories.FacultadRepository;
import com.grupo7.tesis.repositories.PensumRepository;
import java.util.Set;
import java.util.HashSet;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureWebTestClient
public class SimulacionControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private PensumRepository pensumRepository;

    @Autowired
    private FacultadRepository facultadRepository;

    @Autowired
    private com.grupo7.tesis.repositories.ProyeccionRepository proyeccionRepository;

    @Autowired
    private com.grupo7.tesis.repositories.SimulacionRepository simulacionRepository;

    @Autowired
    private com.grupo7.tesis.repositories.SimulacionMateriaRepository simulacionMateriaRepository;

    @Autowired
    private SimulacionJobService jobService;

    @SpyBean
    private SimulacionAsyncService simulacionAsyncService;

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
                        for (Proyeccion p : proyecciones) {
                            try {
                                List<com.grupo7.tesis.models.Simulacion> sims = simulacionRepository.findByProyeccionId_Id(p.getId());
                                if (sims != null && !sims.isEmpty()) {
                                    for (com.grupo7.tesis.models.Simulacion s : sims) {
                                        try {
                                            List<com.grupo7.tesis.models.SimulacionMateria> sms = simulacionMateriaRepository.findBySimulacionId(s.getId());
                                            if (sms != null && !sms.isEmpty()) {
                                                simulacionMateriaRepository.deleteAll(sms);
                                            }
                                        } catch (Exception ex) {
                                        }
                                        try {
                                            simulacionRepository.delete(s);
                                        } catch (Exception ex) {
                                        }
                                    }
                                }
                            } catch (Exception ex) {
                            }
                        }
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
    void generarSimulacion_sincrono() throws Exception {
        
        String token = tokenGenerado();
        SimulacionDTO simulacionDTO = new SimulacionDTO();
        simulacionDTO.setProgreso(crearProgresoEjemplo());
        simulacionDTO.setProyeccion(crearProyeccionEjemplo());

        WebTestClient client = webTestClient.mutate()
            .responseTimeout(Duration.ofSeconds(60))
            .build();

        WebTestClient.RequestBodySpec spec = client.post()
            .uri("/api/simulaciones/generar")
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON);

        if (token != null) {
            spec = spec.header("Authorization", "Bearer " + token);
        }

        spec.bodyValue(simulacionDTO)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Object.class)
            .consumeWith(response -> {
                List<Object> resp = response.getResponseBody();
                assertFalse(resp == null || resp.isEmpty(), "La lista de simulaciones no debe ser vacía");
            });
    }

    @Test
    void iniciarSimulacion_asincrona() throws Exception {

        String token = tokenGenerado();
        SimulacionDTO simulacionDTO = new SimulacionDTO();
        simulacionDTO.setProgreso(crearProgresoEjemplo());
        simulacionDTO.setProyeccion(crearProyeccionEjemplo());

        WebTestClient client = webTestClient.mutate().responseTimeout(Duration.ofSeconds(120)).build();

        @SuppressWarnings("unchecked")
        Map<String, String> inicioResp = client.post()
            .uri("/api/simulaciones/iniciar")
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + token)
            .bodyValue(simulacionDTO)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Map.class)
            .returnResult()
            .getResponseBody();

        String jobId = inicioResp != null ? inicioResp.get("jobId") : null;

        assertNotNull(jobId, "Se debe recibir un jobId al iniciar la simulación");

        SimulacionJob job = null;
        int attempts = 0;
        final int maxAttempts = 300;
        while (attempts < maxAttempts) {
            job = jobService.obtenerTrabajo(jobId);
            if (job != null && job.getEstado() == SimulacionJob.Estado.COMPLETADA) {
                break;
            }
            Thread.sleep(200);
            attempts++;
        }

        assertNotNull(job, "El trabajo debe existir en SimulacionJobService (jobId=" + jobId + ")");
        assertEquals(SimulacionJob.Estado.COMPLETADA, job.getEstado(), "El trabajo debe quedar en estado COMPLETADA — estado actual=" + (job != null ? job.getEstado() : "null") + ", mensaje=" + (job != null ? job.getMensaje() : "null"));

        Object storedResult = job.getResultado();
        System.out.println("[TEST DEBUG] jobId=" + jobId + " storedResult=" + storedResult);
        assertNotNull(storedResult, "El resultado almacenado no debe ser nulo");

        client.get()
            .uri("/api/simulaciones/resultado/" + jobId)
            .header("Authorization", "Bearer " + token)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Object.class)
            .consumeWith(response -> {
                List<Object> resp = response.getResponseBody();
                assertFalse(resp == null || resp.isEmpty(), "La lista de simulaciones no debe ser vacía");
            });
    }

    @Test
    void guardarSimulacion() throws Exception {
        String token = tokenGenerado();
        SimulacionDTO simulacionDTO = new SimulacionDTO();
        simulacionDTO.setProgreso(crearProgresoEjemplo());
        simulacionDTO.setProyeccion(crearProyeccionEjemplo());
        Map<Integer, Simulacion> simulacion = crearResultadoSimulacionEjemplo();
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.registerModule(new JavaTimeModule());
        } catch (NoClassDefFoundError | Exception ex) {
        }
        if (simulacionDTO != null && simulacionDTO.getProyeccion() != null) {
            try {
                simulacionDTO.getProyeccion().setFechaCreacion(null);
            } catch (Exception ignore) {
            }
        }

        TypeReference<Map<String,Object>> tr = new TypeReference<>() {};
        @SuppressWarnings("unchecked")
        Map<String, Object> simulacionDtoMap = mapper.convertValue(simulacionDTO, tr);
        @SuppressWarnings("unchecked")
        Map<String, Object> resultadoSimulacionMap = mapper.convertValue(simulacion, tr);

        Boolean resultado = webTestClient.post()
            .uri("/api/simulaciones/guardarSimulacion")
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + token)
            .bodyValue(new HashMap<String, Object>() {{
                put("simulacionDTO", simulacionDtoMap);
                put("resultadoSimulacion", resultadoSimulacionMap);
            }})
            .exchange()
            .expectStatus().isOk()
            .expectBody(Boolean.class)
            .returnResult()
            .getResponseBody();

        assertNotNull(resultado, "La respuesta no debe ser nula");
        assertTrue(resultado, "El guardado de la simulación debe devolver true");
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
    
    private Progreso crearProgresoEjemplo() {
        List<Materia> materiasFaltantes = new ArrayList<>();
        
        materiasFaltantes.add(createMateria("1295", "Cálculo Diferencial", 3, 1, Arrays.asList(), "nucleoCienciasBasicas"));
        materiasFaltantes.add(createMateria("33698", "Introducción a la programación", 4, 3, Arrays.asList(), "nucleoIngenieria"));
        materiasFaltantes.add(createMateria("1297", "Cálculo Integral", 3, 2, Arrays.asList("1295"), "nucleoCienciasBasicas"));
        materiasFaltantes.add(createMateria("1290", "Álgebra Lineal", 3, 2, Arrays.asList(), "nucleoCienciasBasicas"));
        materiasFaltantes.add(createMateria("33699", "Programación Avanzada", 3, 2, Arrays.asList("33698"), "nucleoIngenieria"));
        materiasFaltantes.add(createMateria("34816", "Gestión Financiera de Proyectos de TI", 2, 2, Arrays.asList(), "nucleoIngenieria"));
        materiasFaltantes.add(createMateria("33733", "Proyecto de Diseño en Ingeniería", 2, 2, Arrays.asList("33763"), "nucleoIngenieria"));
        materiasFaltantes.add(createMateria("34580", "Arquitectura y Organización del Computador", 2, 2, Arrays.asList("33698"), "nucleoIngenieria"));
        materiasFaltantes.add(createMateria("1299", "Cálculo Vectorial", 3, 3, Arrays.asList("1297"), "nucleoCienciasBasicas"));
        materiasFaltantes.add(createMateria("33700", "Bases de Datos", 4, 3, Arrays.asList("33699", "33518"), "nucleoIngenieria"));
        materiasFaltantes.add(createMateria("34805", "Análisis y Diseño de Software", 3, 3, Arrays.asList("33699"), "nucleoIngenieria"));
        materiasFaltantes.add(createMateria("4190", "Comunicaciones y Redes", 4, 3, Arrays.asList("33698"), "nucleoIngenieria"));
        materiasFaltantes.add(createMateria("2544", "Significación Teológica", 2, 3, Arrays.asList(), "nucleoSociohumanisticas"));
        materiasFaltantes.add(createMateria("1340", "Física Mecánica", 3, 4, Arrays.asList("1295"), "nucleoCienciasBasicas"));
        materiasFaltantes.add(createMateria("4196", "Estructuras de Datos", 3, 4, Arrays.asList("33699", "33518"), "nucleoIngenieria"));
        materiasFaltantes.add(createMateria("4082", "Sistemas de Información", 3, 4, Arrays.asList("4075"), "nucleoIngenieria"));
        materiasFaltantes.add(createMateria("34803", "Gestión de Proyectos de Innovación y Emprendimiento en TI", 3, 4, Arrays.asList("34816"), "nucleoIngenieria"));
        materiasFaltantes.add(createMateria("34806", "Fundamentos de Ingeniería de Software", 3, 4, Arrays.asList("34805"), "nucleoIngenieria"));
        materiasFaltantes.add(createMateria("4085", "Sistemas Operativos", 3, 4, Arrays.asList("34580"), "nucleoIngenieria"));
        materiasFaltantes.add(createMateria("33732", "Probabilidad y Estadística", 3, 5, Arrays.asList("1297"), "nucleoCienciasBasicas"));
        materiasFaltantes.add(createMateria("34801", "Teoría de la Computación", 2, 5, Arrays.asList("33518", "33699"), "nucleoIngenieria"));
        materiasFaltantes.add(createMateria("34810", "Proyecto de Innovación y Emprendimiento", 3, 5, Arrays.asList("4082", "34803", "33733", "34806"), "nucleoIngenieria"));
        materiasFaltantes.add(createMateria("22586", "Introducción a los Sistemas Distribuidos", 2, 5, Arrays.asList("34805", "4190", "34809", "4085"), "nucleoIngenieria"));
        materiasFaltantes.add(createMateria("16143", "Constitución y Derecho Civil", 2, 5, Arrays.asList(), "nucleoSociohumanisticas"));
        materiasFaltantes.add(createMateria("1300", "Ecuaciones Diferenciales", 3, 6, Arrays.asList("1297", "1290"), "nucleoCienciasBasicas"));
        materiasFaltantes.add(createMateria("34866", "Optimización y Simulación", 2, 6, Arrays.asList("1290", "33732"), "nucleoIngenieria"));
        materiasFaltantes.add(createMateria("3194", "Análisis de Algoritmos", 2, 6, Arrays.asList("34801", "4196"), "nucleoIngenieria"));
        materiasFaltantes.add(createMateria("34807", "Desarrollo Web", 3, 6, Arrays.asList("34801", "33700", "34806", "4190"), "nucleoIngenieria"));
        materiasFaltantes.add(createMateria("34808", "Introducción a la Computación Móvil", 2, 6, Arrays.asList("34805", "4190", "34806"), "nucleoIngenieria"));
        materiasFaltantes.add(createMateria("2476", "Fe y compromiso del Ingeniero", 2, 6, Arrays.asList("34810"), "nucleoSociohumanisticas"));
        materiasFaltantes.add(createMateria("1291", "Análisis Numérico", 3, 7, Arrays.asList("1300"), "nucleoCienciasBasicas"));
        materiasFaltantes.add(createMateria("4084", "Introducción a la Inteligencia Artificial", 3, 7, Arrays.asList("4196"), "nucleoIngenieria"));
        materiasFaltantes.add(createMateria("31339", "Planeación de Proyecto Final", 2, 7, Arrays.asList("4082", "34803"), "nucleoIngenieria"));
        materiasFaltantes.add(createMateria("4185", "Arquitectura de Software", 3, 7, Arrays.asList("34807", "34808", "22586"), "nucleoIngenieria"));
        materiasFaltantes.add(createMateria("5100", "Proyecto Social Universitario", 2, 7, Arrays.asList("34803", "34806", "2544"), "nucleoSociohumanisticas"));
        materiasFaltantes.add(createMateria("34863", "Ética en la Era de la Información", 2, 7, Arrays.asList(), "nucleoSociohumanisticas"));
        materiasFaltantes.add(createMateria("34802", "Tecnologías Digitales Emergentes", 2, 8, Arrays.asList(), "nucleoIngenieria"));
        materiasFaltantes.add(createMateria("34814", "Proyecto de Grado", 3, 8, Arrays.asList("31339", "34810"), "nucleoIngenieria"));
        materiasFaltantes.add(createMateria("34804", "Gerencia Estratégica", 2, 8, Arrays.asList("34810"), "nucleoIngenieria"));
        materiasFaltantes.add(createMateria("2356", "Epistemología de la ingeniería", 2, 8, Arrays.asList(), "nucleoSociohumanisticas"));

        List<MateriaDTO> cursosVacios = new ArrayList<>();
        List<String> lineasVacias = new ArrayList<>();
        
        List<MateriaDTO> materias = new ArrayList<>();
        
        materias.add(new MateriaDTO("PrimPe2022", "SALUD", "20000", "031179", "Celebra La Vida", "A", "0.00", "Ma"));
        materias.add(new MateriaDTO("PrimPe2022", "FILOSOF", "4400", "002356", "Epistemología de la ingeniería", "4.6", "2.00", "Ma"));
        materias.add(new MateriaDTO("PrimPe2022", "MATEMÁT", "1700", "001295", "Cálculo Diferencial", "2.7", "3.00", "Ma"));
        materias.add(new MateriaDTO("PrimPe2022", "MATEMÁT", "1700", "033518", "Lógica y Matemáticas Discretas", "4.9", "3.00", "Ma"));
        materias.add(new MateriaDTO("PrimPe2022", "PROCESOS", "4900", "033763", "Introducción a la ingeniería", "4.4", "2.00", "Ma"));
        materias.add(new MateriaDTO("PrimPe2022", "SISTEMAS", "4800", "004075", "Pensamiento Sistémico", "4.6", "3.00", "Ma"));
        materias.add(new MateriaDTO("PrimPe2022", "SISTEMAS", "4800", "034809", "Seguridad de la información", "4.7", "2.00", "Ma"));

        Progreso progreso = new Progreso(
            materias,
            lineasVacias,
            cursosVacios,
            cursosVacios,
            cursosVacios,
            cursosVacios,
            cursosVacios,
            cursosVacios,
            cursosVacios,
            cursosVacios,
            cursosVacios,
            cursosVacios,
            cursosVacios,
            cursosVacios,
            cursosVacios,
            materiasFaltantes
        );
        
        return progreso;
    }

    Proyeccion crearProyeccionEjemplo() {
        Proyeccion proyeccion = new Proyeccion();
        proyeccion.setSemestre(8);
        proyeccion.setNumMaxCreditos(20);
        proyeccion.setNumMaxMaterias(6);
        proyeccion.setNombreSimulacion("int-test");
        proyeccion.setTipoMatricula("NORMAL");
        proyeccion.setPracticaProfesional(false);
        proyeccion.setPriorizaciones(new boolean[] {false, false, false, false, false, false});
        return proyeccion;
    }

    private Map<Integer, Simulacion> crearResultadoSimulacionEjemplo() {
        Map<Integer, Simulacion> mapa = new HashMap<>();

        Function<List<Materia>, Set<Materia>> toSet = list -> new HashSet<>(list);

        Simulacion s2 = new Simulacion();
        s2.setMaterias(toSet.apply(java.util.Arrays.asList(
            createMateria("33698", "Introducción a la Programación", 3, 1, java.util.Arrays.asList(), "nucleoIngenieria"),
            createMateria("1290", "Álgebra Lineal", 3, 2, java.util.Arrays.asList(), "nucleoCienciasBasicas"),
            createMateria("33733", "Proyecto de Diseño en Ingeniería", 2, 2, java.util.Arrays.asList(), "nucleoIngenieria"),
            createMateria("1295", "Cálculo Diferencial", 3, 1, java.util.Arrays.asList(), "nucleoCienciasBasicas"),
            createMateria("34816", "Gestión Financiera de Proyectos de TI", 2, 2, java.util.Arrays.asList(), "nucleoIngenieria")
        )));
        mapa.put(2, s2);

        Simulacion s3 = new Simulacion();
        s3.setMaterias(toSet.apply(java.util.Arrays.asList(
            createMateria("1297", "Cálculo Integral", 3, 2, java.util.Arrays.asList("1295"), "nucleoCienciasBasicas"),
            createMateria("34803", "Gestión de Proyectos de Innovación y Emprendimiento en TI", 3, 4, java.util.Arrays.asList("34816"), "nucleoIngenieria"),
            createMateria("33699", "Programación Avanzada", 3, 2, java.util.Arrays.asList("33698"), "nucleoIngenieria"),
            createMateria("34580", "Arquitectura y Organización del Computador", 2, 2, java.util.Arrays.asList("33698"), "nucleoIngenieria"),
            createMateria("2544", "Significación Teológica", 2, 3, java.util.Arrays.asList(), "nucleoSociohumanisticas"),
            createMateria("4190", "Comunicaciones y Redes", 4, 3, java.util.Arrays.asList("33698"), "nucleoIngenieria")
        )));
        mapa.put(3, s3);

        Simulacion s4 = new Simulacion();
        s4.setMaterias(toSet.apply(java.util.Arrays.asList(
            createMateria("33700", "Bases de Datos", 4, 3, java.util.Arrays.asList("33699"), "nucleoIngenieria"),
            createMateria("34805", "Análisis y Diseño de Software", 3, 3, java.util.Arrays.asList("33699"), "nucleoIngenieria"),
            createMateria("4082", "Sistemas de Información", 3, 4, java.util.Arrays.asList("4075"), "nucleoIngenieria"),
            createMateria("4085", "Sistemas Operativos", 3, 4, java.util.Arrays.asList("34580"), "nucleoIngenieria"),
            createMateria("4196", "Estructuras de Datos", 3, 4, java.util.Arrays.asList("33699"), "nucleoIngenieria"),
            createMateria("1299", "Cálculo Vectorial", 3, 3, java.util.Arrays.asList("1297"), "nucleoCienciasBasicas")
        )));
        mapa.put(4, s4);

        Simulacion s5 = new Simulacion();
        s5.setMaterias(toSet.apply(java.util.Arrays.asList(
            createMateria("34801", "Teoría de la Computación", 2, 5, java.util.Arrays.asList("33699"), "nucleoIngenieria"),
            createMateria("22586", "Introducción a los Sistemas Distribuidos", 2, 5, java.util.Arrays.asList("34805"), "nucleoIngenieria"),
            createMateria("1340", "Física Mecánica", 3, 4, java.util.Arrays.asList("1295"), "nucleoCienciasBasicas"),
            createMateria("33732", "Probabilidad y Estadística", 3, 5, java.util.Arrays.asList("1297"), "nucleoCienciasBasicas"),
            createMateria("0", "Electiva", 3, 0, java.util.Arrays.asList(), "electiva"),
            createMateria("34806", "Fundamentos de Ingeniería de Software", 3, 4, java.util.Arrays.asList("34805"), "nucleoIngenieria")
        )));
        mapa.put(5, s5);

        Simulacion s6 = new Simulacion();
        s6.setMaterias(toSet.apply(java.util.Arrays.asList(
            createMateria("1", "Complementaria", 3, 0, java.util.Arrays.asList(), "complementaria"),
            createMateria("34807", "Desarrollo Web", 3, 6, java.util.Arrays.asList("34801"), "nucleoIngenieria"),
            createMateria("34810", "Proyecto de Innovación y Emprendimiento", 3, 5, java.util.Arrays.asList("4082"), "nucleoIngenieria"),
            createMateria("1300", "Ecuaciones Diferenciales", 3, 6, java.util.Arrays.asList("1297"), "nucleoCienciasBasicas"),
            createMateria("1", "Complementaria", 3, 0, java.util.Arrays.asList(), "complementaria"),
            createMateria("34808", "Introducción a la Computación Móvil", 2, 6, java.util.Arrays.asList("34805"), "nucleoIngenieria")
        )));
        mapa.put(6, s6);

        Simulacion s7 = new Simulacion();
        s7.setMaterias(toSet.apply(java.util.Arrays.asList(
            createMateria("4084", "Introducción a la Inteligencia Artificial", 3, 7, java.util.Arrays.asList("4196"), "nucleoIngenieria"),
            createMateria("6", "Electiva de Ciencias Básicas", 3, 0, java.util.Arrays.asList(), "electiva"),
            createMateria("31339", "Planeación de Proyecto Final", 2, 7, java.util.Arrays.asList("4082"), "nucleoIngenieria"),
            createMateria("0", "Electiva", 3, 0, java.util.Arrays.asList(), "electiva"),
            createMateria("1291", "Análisis Numérico", 3, 7, java.util.Arrays.asList("1300"), "nucleoCienciasBasicas"),
            createMateria("4185", "Arquitectura de Software", 3, 7, java.util.Arrays.asList("34807"), "nucleoIngenieria")
        )));
        mapa.put(7, s7);

        Simulacion s8 = new Simulacion();
        s8.setMaterias(toSet.apply(java.util.Arrays.asList(
            createMateria("5", "Énfasis", 3, 0, java.util.Arrays.asList(), "enfasis"),
            createMateria("34814", "Proyecto de Grado", 3, 8, java.util.Arrays.asList("31339"), "nucleoIngenieria"),
            createMateria("34866", "Optimización y Simulación", 2, 6, java.util.Arrays.asList("33732"), "nucleoIngenieria"),
            createMateria("16143", "Constitución y Derecho Civil", 2, 5, java.util.Arrays.asList(), "nucleoSociohumanisticas"),
            createMateria("5", "Énfasis", 3, 0, java.util.Arrays.asList(), "enfasis"),
            createMateria("34804", "Gerencia Estratégica", 2, 8, java.util.Arrays.asList("34810"), "nucleoIngenieria")
        )));
        mapa.put(8, s8);

        return mapa;
    }

    private Materia createMateria(String codigo, String nombre, int creditos, int semestre, List<String> requisitos, String tipo) {
        Materia materia = new Materia();
        materia.setCodigo(codigo);
        materia.setNombre(nombre);
        materia.setCreditos(creditos);
        materia.setSemestre(semestre);
        materia.setRequisitos(requisitos);
        materia.setTipo(tipo);
        return materia;
    }

}
