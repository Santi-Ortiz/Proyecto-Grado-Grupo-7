package com.grupo7.tesis.services;

import com.grupo7.tesis.models.Materia;
import com.grupo7.tesis.models.Progreso;
import com.grupo7.tesis.models.Proyeccion;
import com.grupo7.tesis.dtos.MateriaDTO;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class SimulacionServiceTest {

    @Autowired
    private SimulacionService simulacionService;

    @Test 
public void testJUnit() {
    ejecutarTest();
}

public void ejecutarTest() {
    System.out.println("TEST DE LÍMITES DE COMBINACIONES");
    System.out.println("===============================================");
    
    Progreso progreso = crearProgresoWorstCase();
    Proyeccion proyeccion = crearProyeccion();
    boolean[] prioridades = {false, false, false, false, false, false};

    int[] limites = {1, 3, 5, 10, 15, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100, 150, 200};

    System.out.printf("%-8s %-12s %-15s %-20s%n", "Limite", "Tiempo(ms)", "Nodos Creados", "Combinaciones");
    System.out.println("-------------------------------------------------------------------");

    for (int limite : limites) {
        try {
            Map<String, Object> stats = simulacionService.generarSimulacionConEstadisticas(
                progreso, proyeccion, proyeccion.getSemestre(), prioridades, limite);
            
            long tiempo = (Long) stats.get("tiempoMs");
            int nodos = (Integer) stats.get("nodosCreados");
            int combinaciones = (Integer) stats.get("combinacionesGeneradas");

            System.out.printf("%-8d %-12d %-15d %-20d%n",
                             limite, tiempo, nodos, combinaciones);

            Assertions.assertTrue(tiempo < 300000, 
                "El tiempo excede el máximo permitido para el límite " + limite);

            if (tiempo > 300000) {
                System.out.println("Tiempo excesivo, interrumpiendo pruebas...");
                break;
            }

        } catch (Exception e) {
            System.out.printf("%-8d %-12s %-15s %-20s%n",
                             limite, "ERROR", "ERROR", "ERROR");
            System.out.println("Error: " + e.getMessage());
            Assertions.fail("Error inesperado al ejecutar con límite " + limite + ": " + e.getMessage());
        }
    }
}


    private Progreso crearProgresoWorstCase() {
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
        
        Progreso progreso = new Progreso(
            4.24,           // promedio
            35,             // materiasCursadas
            38,             // materiasFaltantes  
            materiasFaltantes, // listaMateriasFaltantes
            52,             // totalMaterias
            38,             // totalFaltantes
            7,              // totalCursando
            98,             // totalCreditos
            92,             // creditosCursados
            6,              // creditosCursando
            110,            // creditosFaltantes
            92,             // creditosPensum
            6,              // creditosExtra
            8,              // faltanElectiva
            6,              // faltanComplementaria
            6,              // faltanEnfasis
            3,              // faltanElectivaBasicas
            1,              // semestre (peor caso: empezando desde semestre 1)
            cursosVacios,   // cursosElectivas
            cursosVacios,   // cursosEnfasis
            cursosVacios,   // cursosComplementariaLenguas
            cursosVacios,   // cursosComplementariaInformacion
            cursosVacios,   // cursosIA
            cursosVacios,   // cursosDesarrolloComputacion
            cursosVacios,   // cursosDesarrolloGestion
            cursosVacios,   // cursosComputacionVisual
            cursosVacios,   // cursosCVtoIA
            cursosVacios,   // cursosSIGtoIA
            cursosVacios    // cursosElectivaBasicas
        );
        
        return progreso;
    }

    private Proyeccion crearProyeccion() {
        Proyeccion proyeccion = new Proyeccion();
        proyeccion.setSemestre(8); 
        proyeccion.setCreditos(20);
        proyeccion.setMaterias(6);
        return proyeccion;
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
