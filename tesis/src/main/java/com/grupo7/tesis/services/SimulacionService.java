package com.grupo7.tesis.services;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
/*import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;*/
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.grupo7.tesis.config.CustomProperties;
import com.grupo7.tesis.dtos.MateriaConPuntajeDTO;
import com.grupo7.tesis.dtos.MateriaDTO;
import com.grupo7.tesis.models.Estudiante;
import com.grupo7.tesis.models.Materia;
import com.grupo7.tesis.models.NodoA;
import com.grupo7.tesis.models.Simulacion;
import com.grupo7.tesis.models.SimulacionMateria;
import com.grupo7.tesis.models.Progreso;
import com.grupo7.tesis.models.Proyeccion;
import com.grupo7.tesis.repositories.SimulacionRepository;
import com.grupo7.tesis.repositories.ProyeccionRepository;
import com.grupo7.tesis.repositories.SimulacionMateriaRepository;
import com.grupo7.tesis.repositories.MateriaRepository;

@Service
public class SimulacionService {

    @Autowired
    private SimulacionRepository simulacionRepository;

    @Autowired
    private ProyeccionRepository proyeccionRepository;

    @Autowired
    private SimulacionMateriaRepository simulacionMateriaRepository;

    @Autowired
    private MateriaRepository materiaRepository;

    @Autowired
    private EstudianteService estudianteService;

    private int contadorCombinaciones = 0;
    private int contadorNodosCreados = 0;

    //Para el txt
    /*private int contadorIdNodos = 0;
    private FileWriter logWriter = null;
    private String logFileName = "";
    private Map<NodoA, Integer> mapaNodosIds = new HashMap<>(); */

    @Autowired
    private PensumService pensumService;

    @Autowired
    private CustomProperties customProperties;

    // ALGORITMO A*
    public Map<Integer, Simulacion> generarSimulacionMultiSemestreAStar(Progreso progreso, Proyeccion proyeccionBase,
            int semestreObjetivo, List<Materia> materiasPensum, boolean[] prioridades, boolean practicaProfesional, String correo) throws Exception {

        contadorCombinaciones = 0;
        contadorNodosCreados = 0;

        //Para el txt
        /*contadorIdNodos = 0;
        mapaNodosIds.clear();*/

        // Inicializar logging
        //inicializarLog(progreso.getSemestre(), semestreObjetivo);

        System.out.println("================ INICIO SIMULACIÓN A*  ================");
        System.out.println("Semestre actual: " + progreso.getSemestre());
        System.out.println("Semestre objetivo: " + semestreObjetivo);

        int maxCombinacionesPorNodo = customProperties.getCombinaciones();

        PriorityQueue<NodoA> frontera = new PriorityQueue<>(Comparator.comparingDouble(NodoA::getCostoTotal));
        Set<String> visitados = new HashSet<>();
        Map<Integer, Simulacion> rutaInicial = new HashMap<>();
        double heuristicaInicial = calcularHeuristica(progreso, semestreObjetivo, proyeccionBase, materiasPensum, prioridades, progreso.getSemestre());
        NodoA nodoInicial = new NodoA(rutaInicial, progreso.getSemestre(), heuristicaInicial, progreso);
        contadorNodosCreados++;

        /* 
        // Asignar ID al nodo inicial
        int idNodoInicial = ++contadorIdNodos;
        mapaNodosIds.put(nodoInicial, idNodoInicial);

        // Log del nodo inicial
        logNodoDetallado(nodoInicial, 0.0, heuristicaInicial, "INICIAL", null, idNodoInicial, -1);*/

        frontera.offer(nodoInicial);

        int nodosExplorados = 0;
        long tiempoInicio = System.currentTimeMillis();

        while (!frontera.isEmpty() && nodosExplorados < 25000) {
            NodoA nodoActual = frontera.poll();
            nodosExplorados++;

            /*// Log del nodo que se está explorando
            double funcionG = nodoActual.getTotalCreditos();
            double heuristicaNodo = nodoActual.getCostoTotal() - funcionG;
            Simulacion ultimaSimulacion = null;
            if (!nodoActual.getRutaParcial().isEmpty()) {
                int ultimoSemestre = nodoActual.getRutaParcial().keySet().stream().max(Integer::compareTo).orElse(0);
                ultimaSimulacion = nodoActual.getRutaParcial().get(ultimoSemestre);
            }
            
            int idNodoActual = mapaNodosIds.get(nodoActual);
            
            logNodoDetallado(nodoActual, funcionG, heuristicaNodo, "EXPLORADO", ultimaSimulacion, idNodoActual, -1);*/

            if (haCompletadoTodasLasMaterias(nodoActual.getProgresoActual())) {
                // Si se completaron todas las materias pero aún no llegamos al semestre objetivo
                if (nodoActual.getSemestreActual() < semestreObjetivo && practicaProfesional) {
                    try {
                        if (validarPrerequisitoPracticaProfesional(nodoActual.getProgresoActual(), pensumService.obtenerPensumJson())) {
                            System.out.println("Todas las materias completadas pero continuando hasta semestre objetivo para práctica profesional");
                        } else {
                            // No puede tomar práctica profesional, terminar aquí
                            long tiempoTotal = System.currentTimeMillis() - tiempoInicio;
                            System.out.println("SOLUCION OPTIMA A* ENCONTRADA (Todas las materias completadas - No puede tomar práctica profesional)");
                            System.out.println("Semestre de finalización: " + nodoActual.getSemestreActual());
                            System.out.println("Semestre objetivo original: " + semestreObjetivo);
                            System.out.println("Nodos explorados: " + nodosExplorados);
                            System.out.println("Nodos creados: " + contadorNodosCreados);
                            System.out.println("Combinaciones generadas: " + contadorCombinaciones);
                            System.out.println("Tiempo total: " + tiempoTotal + "ms");

                            Map<Integer, Simulacion> rutaCompleta = ordenarRuta(nodoActual.getRutaParcial());
                            rutaCompleta = convertirSimulaciones(rutaCompleta, proyeccionBase, correo);
                            mostrarResultados(rutaCompleta, progreso);
                            return rutaCompleta;
                        }
                    } catch (Exception e) {
                        System.err.println("Error al verificar prerequisitos: " + e.getMessage());
                        long tiempoTotal = System.currentTimeMillis() - tiempoInicio;
                        System.out.println("SOLUCION OPTIMA A* ENCONTRADA (Todas las materias completadas - Error en verificación)");
                        System.out.println("Semestre de finalización: " + nodoActual.getSemestreActual());
                        System.out.println("Semestre objetivo original: " + semestreObjetivo);
                        System.out.println("Nodos explorados: " + nodosExplorados);
                        System.out.println("Nodos creados: " + contadorNodosCreados);
                        System.out.println("Combinaciones generadas: " + contadorCombinaciones);
                        System.out.println("Tiempo total: " + tiempoTotal + "ms");

                        Map<Integer, Simulacion> rutaCompleta = ordenarRuta(nodoActual.getRutaParcial());
                        rutaCompleta = convertirSimulaciones(rutaCompleta, proyeccionBase, correo);
                        mostrarResultados(rutaCompleta, progreso);
                        //cerrarLog();
                        return rutaCompleta;
                    }
                } else {
                    long tiempoTotal = System.currentTimeMillis() - tiempoInicio;
                    System.out.println("SOLUCION OPTIMA A* ENCONTRADA (Todas las materias completadas)");
                    System.out.println("Semestre de finalización: " + nodoActual.getSemestreActual());
                    System.out.println("Semestre objetivo original: " + semestreObjetivo);
                    System.out.println("Nodos explorados: " + nodosExplorados);
                    System.out.println("Nodos creados: " + contadorNodosCreados);
                    System.out.println("Combinaciones generadas: " + contadorCombinaciones);
                    System.out.println("Tiempo total: " + tiempoTotal + "ms");

                    Map<Integer, Simulacion> rutaCompleta = ordenarRuta(nodoActual.getRutaParcial());
                    rutaCompleta = convertirSimulaciones(rutaCompleta, proyeccionBase, correo);
                    mostrarResultados(rutaCompleta, progreso);
                    //cerrarLog();
                    return rutaCompleta;
                }
            }

            if (nodoActual.getSemestreActual() == semestreObjetivo) {
                double heuristicaActual = calcularHeuristica(nodoActual.getProgresoActual(), semestreObjetivo, proyeccionBase, materiasPensum, prioridades, nodoActual.getSemestreActual());
                
                int nodosMinimosParaComparar = 50; 
                
                if (nodosExplorados >= nodosMinimosParaComparar) {
                    boolean hayMejorOpcion = false;
                    for (NodoA nodoFrontera : frontera) {
                        double heuristicaFrontera = calcularHeuristica(nodoFrontera.getProgresoActual(), semestreObjetivo, proyeccionBase, materiasPensum, prioridades, nodoFrontera.getSemestreActual());
                        if (heuristicaFrontera < heuristicaActual) {
                            hayMejorOpcion = true;
                            break;
                        }
                    }

                    if (!hayMejorOpcion || nodosExplorados >= 25000) {
                        long tiempoTotal = System.currentTimeMillis() - tiempoInicio;
                        System.out.println("SOLUCION A* ENCONTRADA (Semestre objetivo alcanzado)");
                        System.out.println("Heurística del nodo seleccionado: " + heuristicaActual);
                        System.out.println("Nodos explorados: " + nodosExplorados);
                        System.out.println("Nodos creados: " + contadorNodosCreados);
                        System.out.println("Combinaciones generadas: " + contadorCombinaciones);
                        System.out.println("Tiempo total: " + tiempoTotal + "ms");

                        Map<Integer, Simulacion> rutaCompleta = ordenarRuta(nodoActual.getRutaParcial());
                        rutaCompleta = convertirSimulaciones(rutaCompleta, proyeccionBase, correo);
                        mostrarResultados(rutaCompleta, progreso);
                        //cerrarLog();
                        return rutaCompleta;
                    }
                }
            }

            String claveEstado = generarClaveEstado(nodoActual.getProgresoActual(), nodoActual.getSemestreActual());
            if (visitados.contains(claveEstado))
                continue;
            visitados.add(claveEstado);

            expandirNodo(nodoActual, frontera, semestreObjetivo, proyeccionBase, materiasPensum,
                    maxCombinacionesPorNodo, prioridades, practicaProfesional);
        }

        long tiempoTotal = System.currentTimeMillis() - tiempoInicio;
        System.out.println("A* alcanzó límite de nodos: ");
        System.out.println("Nodos creados: " + contadorNodosCreados);
        System.out.println("Combinaciones generadas: " + contadorCombinaciones);
        System.out.println("Tiempo transcurrido: " + tiempoTotal + "ms");
        System.out.println("A* no pudo encontrar ninguna solución completa");
        //cerrarLog();
        return new HashMap<>();
    }

    public void expandirNodo(NodoA nodoActual, PriorityQueue<NodoA> frontera, int semestreObjetivo,
            Proyeccion proyeccionBase, List<Materia> materiasPensum, int maxCombinaciones, boolean[] prioridades, boolean practicaProfesional) {

        int siguienteSemestre = nodoActual.getSemestreActual() + 1;
        if (siguienteSemestre > semestreObjetivo)
            return;

        Proyeccion proyeccionSemestre = crearProyeccionParaSemestre(proyeccionBase, siguienteSemestre);
        
        // Si es el último semestre y se quiere práctica profesional, verificar prerequisitos y limitar recursos
        boolean aplicarPracticaProfesional = false;
        if (practicaProfesional && siguienteSemestre == semestreObjetivo) {
            try {
                if (validarPrerequisitoPracticaProfesional(nodoActual.getProgresoActual(), pensumService.obtenerPensumJson())) {
                    proyeccionSemestre.setNumMaxCreditos(4);
                    proyeccionSemestre.setNumMaxMaterias(3);
                    aplicarPracticaProfesional = true;
                }
            } catch (Exception e) {
                System.err.println("Error al verificar prerequisitos de práctica profesional: " + e.getMessage());
            }
        }

        Set<Simulacion> combinaciones = generarCombinaciones(nodoActual.getProgresoActual(), proyeccionSemestre,
                materiasPensum, maxCombinaciones, prioridades, false); // false para no incluir práctica en generación normal

        // Crear una simulación vacía para agregar solo la práctica profesional
        if (aplicarPracticaProfesional && combinaciones.isEmpty()) {
            Simulacion simulacionVacia = new Simulacion();
            combinaciones.add(simulacionVacia);
        }

        for (Simulacion combinacion : combinaciones) {
            Map<Integer, Simulacion> nuevaRuta = new HashMap<>(nodoActual.getRutaParcial());

            Simulacion simulacionSemestre = new Simulacion();
            // Validar que la combinación tenga materias antes de iterar
            if (combinacion.getMaterias() != null) {
                for (Materia materia : combinacion.getMaterias()) {
                    simulacionSemestre.agregarMateria(materia);
                }
            }
            
            // Si aplica práctica profesional, agregarla a la simulación
            if (aplicarPracticaProfesional) {
                Materia practicaProfesionalMateria = new Materia();
                practicaProfesionalMateria.setCodigo("Practica");
                practicaProfesionalMateria.setNombre("Práctica Profesional");
                practicaProfesionalMateria.setCreditos(6);
                practicaProfesionalMateria.setSemestre(siguienteSemestre);
                practicaProfesionalMateria.setTipo("practicaProfesional");
                List<String> prerequisitos = List.of("4190", "4075", "4085", "34803", "34807", "34801");
                practicaProfesionalMateria.setRequisitos(prerequisitos);
                simulacionSemestre.agregarMateria(practicaProfesionalMateria);
            }
            
            double puntajeSemestre = 0.0;
            for (Materia materia : simulacionSemestre.getMaterias()) {
                puntajeSemestre += calcularPuntajeMateria(materia, nodoActual.getProgresoActual(), proyeccionSemestre, prioridades);
            }
            simulacionSemestre.setPuntajeTotal(puntajeSemestre);
            
            nuevaRuta.put(siguienteSemestre, simulacionSemestre);

            Progreso nuevoProgreso = nodoActual.getProgresoActual().copy();
            
            nuevoProgreso = actualizarProgresoTemporal(nuevoProgreso, simulacionSemestre, siguienteSemestre);
            
            double nuevoCosto = nodoActual.getTotalCreditos() + calcularCostoTransicion(combinacion, nuevoProgreso, proyeccionSemestre, prioridades);
            double nuevaHeuristica = calcularHeuristica(nuevoProgreso, semestreObjetivo, proyeccionBase,materiasPensum, prioridades, siguienteSemestre);
            
            //Funcion f(n)=h(n)+g(n)
            double costoTotal = nuevoCosto + nuevaHeuristica;

            NodoA nuevoNodo = new NodoA(nuevaRuta, siguienteSemestre, costoTotal, nuevoProgreso);
            contadorNodosCreados++;
            
            /*// Asignar ID al nodo hijo y establecer relación padre-hijo
            int idNuevoNodo = ++contadorIdNodos;
            mapaNodosIds.put(nuevoNodo, idNuevoNodo);
            int idPadre = mapaNodosIds.get(nodoActual);
            
            // Log del nodo hijo creado
            logNodoDetallado(nuevoNodo, nuevoCosto, nuevaHeuristica, "HIJO", simulacionSemestre, idNuevoNodo, idPadre);*/
            
            frontera.offer(nuevoNodo);
        }
    }

    // Verificar si el estudiante puede tomar práctica profesional (prerequisitos)
    public boolean validarPrerequisitoPracticaProfesional(Progreso progreso, List<Materia> materiasPensum) {
        if (progreso.getMaterias() != null) {
            for (MateriaDTO materia : progreso.getMaterias()) {
                if ("Practica".equals(materia.getMateria()) || "Práctica Profesional".equals(materia.getTitulo())) {
                    return false; // Ya la tomó
                }
            }
        }
        
        List<String> prerequisitosPractica = List.of("4190", "4075", "4085", "34803", "34807", "34801");
        boolean cumplePrerequisitos = validarPrerequisito(progreso, materiasPensum, prerequisitosPractica);
        
        return cumplePrerequisitos;
    }

    public Progreso actualizarProgresoTemporal(Progreso progreso, Simulacion simulacion, int semestreSimulado) {

        // Remover materias de núcleo que se simulan como cursadas
        List<Materia> materiasARemover = new ArrayList<>();
        for (Materia materiaSimulada : simulacion.getMaterias()) {

            // Solo procesar materias de núcleo (no electivas especiales)
            if (!materiaSimulada.getCodigo().equals("0") && !materiaSimulada.getCodigo().equals("1")
                    && !materiaSimulada.getCodigo().equals("5") && !materiaSimulada.getCodigo().equals("6")) {

                for (Materia materiaFaltante : progreso.getListaMateriasFaltantes()) {
                    if (materiaFaltante.getCodigo().equals(materiaSimulada.getCodigo())
                            || (materiaFaltante.getNombre().equals(materiaSimulada.getNombre())
                                    && materiaFaltante.getSemestre() == materiaSimulada.getSemestre())) {
                        materiasARemover.add(materiaFaltante);
                        break;
                    }
                }
            }
        }

        // Remover las materias de núcleo de la lista de faltantes
        progreso.getListaMateriasFaltantes().removeAll(materiasARemover);

        // Simular que se cursaron las electivas/complementarias/énfasis agregándolas a las listas correspondientes
        List<MateriaDTO> nuevasElectivas = new ArrayList<>(progreso.getCursosElectivas() != null ? progreso.getCursosElectivas() : new ArrayList<>());
        List<MateriaDTO> nuevasComplementarias = new ArrayList<>();
        if (progreso.getCursosComplementariaLenguas() != null) {
            nuevasComplementarias.addAll(progreso.getCursosComplementariaLenguas());
        }
        if (progreso.getCursosComplementariaInformacion() != null) {
            nuevasComplementarias.addAll(progreso.getCursosComplementariaInformacion());
        }
        List<MateriaDTO> nuevosEnfasis = new ArrayList<>(progreso.getCursosEnfasis() != null ? progreso.getCursosEnfasis() : new ArrayList<>());
        List<MateriaDTO> nuevasElectivaBasicas = new ArrayList<>(progreso.getCursosElectivaBasicas() != null ? progreso.getCursosElectivaBasicas() : new ArrayList<>());

        for (Materia materia : simulacion.getMaterias()) {
            switch (materia.getCodigo()) {
                case "0":
                    // Agregar electiva simulada
                    MateriaDTO electivaSimulada = new MateriaDTO(
                        "SimSem" + semestreSimulado, 
                        "ELEC", 
                        "000", 
                        "ELEC" + System.nanoTime(),
                        materia.getNombre(),
                        "3.0",
                        String.valueOf(materia.getCreditos()),
                        ""
                    );
                    nuevasElectivas.add(electivaSimulada);
                    break;
                case "1":
                    // Agregar complementaria simulada
                    MateriaDTO complementariaSimulada = new MateriaDTO(
                        "SimSem" + semestreSimulado,
                        "COMP",
                        "000",
                        "COMP" + System.nanoTime(),
                        materia.getNombre(),
                        "3.0",
                        String.valueOf(materia.getCreditos()), 
                        ""
                    );
                    nuevasComplementarias.add(complementariaSimulada);
                    break;
                case "5":
                    // Agregar énfasis simulado
                    MateriaDTO enfasisSimulado = new MateriaDTO(
                        "SimSem" + semestreSimulado,
                        "ENF",
                        "000",
                        "ENF" + System.nanoTime(),
                        materia.getNombre(),
                        "3.0",
                        String.valueOf(materia.getCreditos()),
                        ""
                    );
                    nuevosEnfasis.add(enfasisSimulado);
                    break;
                case "6":
                    // Agregar electiva de ciencias básicas simulada
                    MateriaDTO electivaCBSimulada = new MateriaDTO(
                        "SimSem" + semestreSimulado,
                        "ELCB",
                        "000",
                        "ELCB" + System.nanoTime(),
                        materia.getNombre(),
                        "3.0",
                        String.valueOf(materia.getCreditos()),
                        ""
                    );
                    nuevasElectivaBasicas.add(electivaCBSimulada);
                    break;
            }
        }

        progreso.setCursosElectivas(nuevasElectivas);
        progreso.setCursosComplementariaLenguas(nuevasComplementarias);
        progreso.setCursosEnfasis(nuevosEnfasis);
        progreso.setCursosElectivaBasicas(nuevasElectivaBasicas);

        List<MateriaDTO> materiasActualizadas = new ArrayList<>(progreso.getMaterias() != null ? progreso.getMaterias() : new ArrayList<>());
        
        for (Materia materiaRemovida : materiasARemover) {
            MateriaDTO materiaSimulada = new MateriaDTO(
                "SimSem" + semestreSimulado,
                materiaRemovida.getCodigo(),
                "000",
                materiaRemovida.getCodigo(), 
                materiaRemovida.getNombre(),
                "3.0",
                String.valueOf(materiaRemovida.getCreditos()),
                ""
            );
            materiasActualizadas.add(materiaSimulada);
        }

        progreso.setMaterias(materiasActualizadas);

        return progreso;
    }

    // Heuristica
    public double calcularHeuristica(Progreso progreso, int semestreObjetivo, Proyeccion proyeccionBase,
            List<Materia> materiasPensum, boolean[] prioridades, int semestreActual) {

        if (haCompletadoTodasLasMaterias(progreso)) {
            return 0.0;
        }

        double heuristica = 0.0;
        double peso = 1.0;

        int creditosNucleoCBFaltantes = contarCreditosNucleoCBFaltantes(progreso);
        int creditosNucleoIngenieria = contarCreditosNucleoIngeFaltantes(progreso);
        int creditosNucleoSociohumanisticas = contarCreditosNucleoSocioFaltantes(progreso);
        int materiasQueDesbloquean = calcularMateriasQueDesbloquean(progreso, materiasPensum);
        int contarCreditosAtrasados = contarCreditosAtrasados(progreso, semestreActual);

        double electivasFaltantes = progreso.getFaltanElectiva();
        double complementariasFaltantes = progreso.getFaltanComplementaria();
        double enfasisFaltantes = progreso.getFaltanEnfasis();
        double electivasCBFaltantes = progreso.getFaltanElectivaBasicas();

        double cbPrioridad = (prioridades != null && prioridades.length > 0 && prioridades[0]) ? 0.5 : 0;
        double ingenieriaPrioridad = (prioridades != null && prioridades.length > 1 && prioridades[1]) ? 0.5 : 0;
        double sociohumanisticasPrioridad = (prioridades != null && prioridades.length > 2 && prioridades[2]) ? 0.5 : 0;
        double electivasPrioridad = (prioridades != null && prioridades.length > 3 && prioridades[3]) ? 0.5 : 0;
        double complementariasPrioridad = (prioridades != null && prioridades.length > 4 && prioridades[4]) ? 0.5 : 0;
        double enfasisPrioridad = (prioridades != null && prioridades.length > 5 && prioridades[5]) ? 0.5 : 0;

        heuristica = creditosNucleoCBFaltantes * ( (peso) + cbPrioridad) + creditosNucleoIngenieria * ( (peso) + ingenieriaPrioridad) + creditosNucleoSociohumanisticas * ( (peso) + sociohumanisticasPrioridad) + electivasFaltantes * ((peso) + electivasPrioridad) + complementariasFaltantes * ((peso) + complementariasPrioridad) + enfasisFaltantes * ((peso) + enfasisPrioridad) + electivasCBFaltantes * ((peso) + cbPrioridad) + materiasQueDesbloquean * peso + contarCreditosAtrasados * 3.0;

        return heuristica;
    }

    // Función G 
    public double calcularCostoTransicion(Simulacion combinacion, Progreso progreso, Proyeccion proyeccion, boolean[] prioridades) {
        
        double costoCreditos = combinacion.getTotalCreditos();
        
        return costoCreditos;
    }

    // Puntaje de cada materia con prioridades
    public double calcularPuntajeMateria(Materia materia, Progreso progreso, Proyeccion proyeccion,
            boolean[] prioridades) {
        double puntaje = 0;
        int distanciaSemestral = 0;
        double coeficienteMateria = 0;
        double coeficienteDistancia = 0;
        String codigo = materia.getCodigo();
        String tipo = materia.getTipo();

        double factorPrioridad = 0.7;
        if (prioridades != null && prioridades.length >= 6) {

            if (tipo != null) {
                switch (tipo) {
                    case "nucleoCienciasBasicas":
                        if (prioridades[0])
                            factorPrioridad = 1.0;
                        break;
                    case "nucleoIngenieria":
                        if (prioridades[1])
                            factorPrioridad = 1.0;
                        break;
                    case "nucleoSociohumanisticas":
                        if (prioridades[2])
                            factorPrioridad = 1.0;
                        break;
                }
            }

            switch (codigo) {
                case "0": // Electiva
                    if (prioridades[3])
                        factorPrioridad = 1.0;
                    break;
                case "1": // Complementaria
                    if (prioridades[4])
                        factorPrioridad = 1.0;
                    break;
                case "5": // Énfasis
                    if (prioridades[5])
                        factorPrioridad = 1.0;
                    break;
                case "6": // ElectivaCB
                    if (prioridades[0])
                        factorPrioridad = 1.0;
                    break;
            }
        }
    
        switch (codigo) {
            case "0": // Electiva
                coeficienteMateria = 0.6 * factorPrioridad;
                break;
            case "1": // Complementaria
                coeficienteMateria = 0.8 * factorPrioridad;
                break;
            case "5": // Énfasis
                coeficienteMateria = 0.8 * factorPrioridad;
                break;
            case "6": // ElectivaCB
                coeficienteMateria = 1.0 * factorPrioridad;
                break;
            default: // Núcleo
                coeficienteMateria = 1.0 * factorPrioridad;
                break;
        }

        distanciaSemestral = materia.getSemestre() - proyeccion.getSemestre();

        if (distanciaSemestral > 1) {
            coeficienteDistancia = 0.2; // Materia de semestres posteriores (era 0.3)
        } else if (distanciaSemestral == 1) {
            coeficienteDistancia = 0.6; // Materia de un semestre adelante (era 0.7)
        } else if (distanciaSemestral == 0) {
            coeficienteDistancia = 1.0; // Materia del semestre actual
        } else {
            coeficienteDistancia = 1.8; // Materia de semestres anteriores (era 1.3)
        }

        puntaje = coeficienteMateria * coeficienteDistancia;

        return puntaje;
    }

    // Contar materias de núcleo faltantes
    public int contarMateriasNucleoFaltantes(Progreso progreso) {
        int count = 0;
        for (Materia materia : progreso.getListaMateriasFaltantes()) {
            if (esMateriaNucleo(materia)) {
                count++;
            }
        }
        return count;
    }

    // Contar créditos de núcleo faltantes
    public int contarCreditosNucleoFaltantes(Progreso progreso) {
        int count = 0;
        for (Materia materia : progreso.getListaMateriasFaltantes()) {
            if (esMateriaNucleo(materia)) {
                count += materia.getCreditos();
            }
        }
        return count;
    }

    // Contar créditos de núcleo complementarias faltantes
    public int contarCreditosNucleoCBFaltantes(Progreso progreso) {
        int count = 0;
        for (Materia materia : progreso.getListaMateriasFaltantes()) {
            if (materia.getTipo().equals("nucleoCienciasBasicas")) {
                count += materia.getCreditos();
            }
        }
        return count;
    }

    // Contar créditos de núcleo ingeniería faltantes
    public int contarCreditosNucleoIngeFaltantes(Progreso progreso) {
        int count = 0;
        for (Materia materia : progreso.getListaMateriasFaltantes()) {
            if (materia.getTipo().equals("nucleoIngenieria")) {
                count += materia.getCreditos();
            }
        }
        return count;
    }

    // Contar créditos de núcleo sociohumanísticas faltantes
    public int contarCreditosNucleoSocioFaltantes(Progreso progreso) {
        int count = 0;
        for (Materia materia : progreso.getListaMateriasFaltantes()) {
            if (materia.getTipo().equals("nucleoSociohumanisticas")) {
                count += materia.getCreditos();
            }
        }
        return count;
    }

    //Contar créditos atrasados
    public int contarCreditosAtrasados(Progreso progreso, int semestreActual) {
        int count = 0;
        for (Materia materia : progreso.getListaMateriasFaltantes()) {
            if (materia.getSemestre() <= semestreActual) {
                count += materia.getCreditos();
            }
        }
        return count;
    }

    public int calcularMateriasQueDesbloquean(Progreso progreso, List<Materia> materiasPensum) {
        List<Materia> materiasFaltantes = progreso.getMateriasFaltantes();
        int contadorDesbloqueos = 0;
        
        for (Materia materiaFaltante : materiasFaltantes) {
            for (Materia otraMateria : materiasFaltantes) {
                if (!materiaFaltante.equals(otraMateria) && 
                    otraMateria.getRequisitos() != null && 
                    otraMateria.getRequisitos().contains(materiaFaltante.getCodigo())) {
                    contadorDesbloqueos+= otraMateria.getCreditos();
                }
            }
        }
        
        return contadorDesbloqueos;
    }

    // Validar si una materia es de cualquier tipo de núcleo
    public boolean esMateriaNucleo(Materia materia) {
        String tipo = materia.getTipo();
        return tipo.equals("nucleoCienciasBasicas") || tipo.equals("nucleoIngenieria")
                || tipo.equals("nucleoSociohumanisticas");
    }

    // Generar una clave de estado única para el progreso y semestre
    public String generarClaveEstado(Progreso progreso, int semestre) {
        StringBuilder sb = new StringBuilder();
        sb.append("sem:").append(semestre);
        sb.append("|mat:").append(
                progreso.getListaMateriasFaltantes().stream()
                        .map(Materia::getCodigo)
                        .sorted()
                        .collect(Collectors.joining(",")));
        sb.append("|cred:").append(progreso.getTotalCreditos());
        sb.append("|elec:").append(progreso.getFaltanElectiva());
        sb.append("|comp:").append(progreso.getFaltanComplementaria());
        sb.append("|enf:").append(progreso.getFaltanEnfasis());
        sb.append("|cb:").append(progreso.getFaltanElectivaBasicas());
        return sb.toString();
    }

    // Ordenar la ruta por semestre
    public Map<Integer, Simulacion> ordenarRuta(Map<Integer, Simulacion> ruta) {
        return ruta.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new));
    }

    // Verifica si el progreso ha completado todas las materias requeridas
    public boolean haCompletadoTodasLasMaterias(Progreso progreso) {
        boolean nucleoCompleto = contarMateriasNucleoFaltantes(progreso) == 0;
        boolean electivasCompletas = progreso.getFaltanElectiva() <= 0;
        boolean complementariasCompletas = progreso.getFaltanComplementaria() <= 0;
        boolean enfasisCompleto = progreso.getFaltanEnfasis() <= 0;
        boolean electivasCBCompletas = progreso.getFaltanElectivaBasicas() <= 0;

        return nucleoCompleto && electivasCompletas && complementariasCompletas && enfasisCompleto
                && electivasCBCompletas;
    }

    // Esto valida si una materia puede ser añadida en la simulación a partir de los
    // prerequisitos
    public Boolean validarPrerequisito(Progreso progreso, List<Materia> materiasPensum,
            List<String> prerequisitos) {

        if (prerequisitos == null || prerequisitos.isEmpty())
            return true; // Si no hay prerequisitos, se puede añadir la materia

        for (String pr : prerequisitos) {
            for (Materia materia : progreso.getListaMateriasFaltantes()) { // Aca si añadir la lista de materias
                                                                           // vistas
                if (materia.getCodigo().equals(pr)) {
                    return false;
                }
            }
        }
        return true;
    }

    // Devuelve la cantidad de créditos que faltan por cursar en electivas para ese
    // semestre
    public int validarElectivas(Progreso progreso, List<Materia> materiasPensum, int semestre) {
        int numCreditosElectivas = 0;
        int numCreditosElectivasVistas = Math.max(8 - progreso.getFaltanElectiva(), 0);

        for (Materia materia : materiasPensum) {
            if (materia.getCodigo().equals("0") &&
                    materia.getSemestre() <= semestre) {
                numCreditosElectivas += materia.getCreditos();
            }
        }

        return Math.max(numCreditosElectivas - numCreditosElectivasVistas, 0);
    }

    // Devuelve la cantidad de créditos que faltan por cursar en complementarias
    // para ese semestre
    public int validarComplementarias(Progreso progreso, List<Materia> materiasPensum, int semestre) {
        int numCreditosComplementarias = 0;
        int numCreditosComplementariasVistas = Math.max(6 - progreso.getFaltanComplementaria(), 0);

        for (Materia materia : materiasPensum) {
            if (materia.getCodigo().equals("1") &&
                    materia.getSemestre() <= semestre) {
                numCreditosComplementarias += materia.getCreditos();
            }
        }

        return Math.max(numCreditosComplementarias - numCreditosComplementariasVistas, 0);
    }

    // Devuelve la cantidad de créditos que faltan por cursar en énfasis para ese
    // semestre
    public int validarEnfasis(Progreso progreso, List<Materia> materiasPensum, int semestre) {
        int numCreditosEnfasis = 0;
        int numCreditosEnfasisVistas = Math.max(6 - progreso.getFaltanEnfasis(), 0);

        for (Materia materia : materiasPensum) {
            if (materia.getCodigo().equals("5") &&
                    materia.getSemestre() <= semestre) {
                numCreditosEnfasis += materia.getCreditos();
            }
        }

        return Math.max(numCreditosEnfasis - numCreditosEnfasisVistas, 0);
    }

    // Devuelve la cantidad de créditos que faltan por cursar en electivas de
    // cs.básicas para ese semestre
    public int validarElectivasCB(Progreso progreso, List<Materia> materiasPensum, int semestre) {
        int numCreditosElectivasCB = 0;
        int numCreditosElectivasCBVistas = 3 - progreso.getFaltanElectivaBasicas();

        for (Materia materia : materiasPensum) {
            if (materia.getCodigo().equals("6") &&
                    materia.getSemestre() <= semestre) {
                numCreditosElectivasCB += materia.getCreditos();
            }
        }

        return Math.max(numCreditosElectivasCB - numCreditosElectivasCBVistas, 0);
    }

    // Verifica si se puede añadir una materia especial (electiva, complementaria, énfasis o núcleo)
    public Materia verificarMateria(int creditosRestantes, int creditosRestantesGeneral,
            int materiasRestantesGeneral, String codigo, String nombre, int semestre) {
        if (creditosRestantes > 0) {

            if (creditosRestantesGeneral > 0 && materiasRestantesGeneral > 0) {
                Materia materiaSugerida = new Materia();
                materiaSugerida.setCodigo(codigo);
                materiaSugerida.setNombre(nombre);
                materiaSugerida.setSemestre(semestre);

                if (codigo.equals("5") || codigo.equals("6") || codigo.equals("1")) {
                    if (creditosRestantesGeneral >= 3 && creditosRestantes >= 3) {
                        materiaSugerida.setCreditos(3);
                    } else {
                        return null;
                    }
                } else if (codigo.equals("0")) {
                    materiaSugerida.setCreditos(Math.min(creditosRestantes, creditosRestantesGeneral));
                } else {
                    return null;
                }

                if (codigo.equals("0")) {
                    materiaSugerida.setTipo("electiva");
                } else if (codigo.equals("5")) {
                    materiaSugerida.setTipo("enfasis");
                } else if (codigo.equals("1")) {
                    materiaSugerida.setTipo("complementaria");
                } else if (codigo.equals("6")) {
                    materiaSugerida.setTipo("nucleoCienciasBasicas");
                }

                return materiaSugerida;
            }
        }
        return null;
    }

    // Buscar materias faltantes y que pueda cursar
    public List<Materia> filtrarMateriasDisponibles(Progreso progreso, List<Materia> materiasPensum,
            Proyeccion proyeccion) {
        List<Materia> materiasDisponibles = new ArrayList<>();

        // Agregar materias nucleo faltantes
        for (Materia materia : progreso.getListaMateriasFaltantes()) {
            if (materia.getSemestre() <= proyeccion.getSemestre() + 1 &&
                    validarPrerequisito(progreso, materiasPensum, materia.getRequisitos())) {
                materiasDisponibles.add(materia);
            }
        }

        agregarMateriasAdicionalesDisponibles(materiasDisponibles, progreso, materiasPensum, proyeccion);

        return materiasDisponibles;
    }

    // Agrega electivas, complementarias y de énfasis a las materias disponibles
    public void agregarMateriasAdicionalesDisponibles(List<Materia> materiasDisponibles, Progreso progreso,
            List<Materia> materiasPensum, Proyeccion proyeccion) {

        int creditosDisponibles = proyeccion.getNumMaxCreditos();
        int materiasDisponiblesNum = proyeccion.getNumMaxMaterias();
        int semestre = proyeccion.getSemestre();

        // ELECTIVAS
        int usadosElectivas = 0;
        usadosElectivas += agregarElectivasPorSemestre(materiasDisponibles,
                validarElectivas(progreso, materiasPensum, semestre - 1), creditosDisponibles, materiasDisponiblesNum,
                semestre - 1, "Electiva Atrasada");
        usadosElectivas += agregarElectivasPorSemestre(materiasDisponibles,
                validarElectivas(progreso, materiasPensum, semestre) - usadosElectivas, creditosDisponibles,
                materiasDisponiblesNum, semestre, "Electiva Actual");
        agregarElectivasPorSemestre(materiasDisponibles,
                validarElectivas(progreso, materiasPensum, semestre + 1) - usadosElectivas, creditosDisponibles,
                materiasDisponiblesNum, semestre + 1, "Electiva Futura");

        // COMPLEMENTARIAS
        int usadosComp = 0;
        usadosComp += agregarMateriasGenericas(materiasDisponibles,
                validarComplementarias(progreso, materiasPensum, semestre - 1), creditosDisponibles,
                materiasDisponiblesNum, "1", "Complementaria Atrasada", semestre - 1);
        usadosComp += agregarMateriasGenericas(materiasDisponibles,
                validarComplementarias(progreso, materiasPensum, semestre) - usadosComp, creditosDisponibles,
                materiasDisponiblesNum, "1", "Complementaria Actual", semestre);
        agregarMateriasGenericas(materiasDisponibles,
                validarComplementarias(progreso, materiasPensum, semestre + 1) - usadosComp, creditosDisponibles,
                materiasDisponiblesNum, "1", "Complementaria Futura", semestre + 1);

        // ÉNFASIS
        int usadosEnf = 0;
        usadosEnf += agregarMateriasGenericas(materiasDisponibles,
                validarEnfasis(progreso, materiasPensum, semestre - 1), creditosDisponibles, materiasDisponiblesNum,
                "5", "Énfasis Atrasado", semestre - 1);
        usadosEnf += agregarMateriasGenericas(materiasDisponibles,
                validarEnfasis(progreso, materiasPensum, semestre) - usadosEnf, creditosDisponibles,
                materiasDisponiblesNum, "5", "Énfasis Actual", semestre);
        agregarMateriasGenericas(materiasDisponibles,
                validarEnfasis(progreso, materiasPensum, semestre + 1) - usadosEnf, creditosDisponibles,
                materiasDisponiblesNum, "5", "Énfasis Futuro", semestre + 1);

        // ELECTIVAS CIENCIAS BÁSICAS
        int usadosCB = 0;
        usadosCB += agregarMateriasGenericas(materiasDisponibles,
                validarElectivasCB(progreso, materiasPensum, semestre - 1), creditosDisponibles, materiasDisponiblesNum,
                "6", "Electiva CB Atrasada", semestre - 1);
        usadosCB += agregarMateriasGenericas(materiasDisponibles,
                validarElectivasCB(progreso, materiasPensum, semestre) - usadosCB, creditosDisponibles,
                materiasDisponiblesNum, "6", "Electiva CB Actual", semestre);
        agregarMateriasGenericas(materiasDisponibles,
                validarElectivasCB(progreso, materiasPensum, semestre + 1) - usadosCB, creditosDisponibles,
                materiasDisponiblesNum, "6", "Electiva CB Futura", semestre + 1);
    }

    public int agregarElectivasPorSemestre(List<Materia> materiasDisponibles, int creditosRequeridos,
            int creditosDisponibles, int materiasDisponiblesNum, int semestre, String descripcionBase) {

        int usados = 0;
        for (int credito = 3; credito >= 1; credito--) {
            while (creditosRequeridos >= credito) {
                Materia electiva = verificarMateria(
                        credito, creditosDisponibles, materiasDisponiblesNum, "0",
                        descripcionBase + " " + credito + "C", semestre);
                if (electiva != null) {
                    materiasDisponibles.add(electiva);
                    creditosRequeridos -= credito;
                    usados += credito;
                } else {
                    break;
                }
            }
        }
        return usados;
    }

    // Agregar materias genéricas a la simulación
    public int agregarMateriasGenericas(List<Materia> materiasDisponibles, int creditosRequeridos,
            int creditosDisponibles, int materiasDisponiblesNum, String codigo, String descripcion, int semestre) {

        int usados = 0;
        Materia m1 = verificarMateria(creditosRequeridos, creditosDisponibles, materiasDisponiblesNum, codigo,
                descripcion, semestre);
        if (m1 != null) {
            materiasDisponibles.add(m1);
            usados += m1.getCreditos();
        }

        if (creditosRequeridos >= 6) {
            Materia m2 = verificarMateria(3, creditosDisponibles, materiasDisponiblesNum, codigo,
                    descripcion + " 2", semestre);
            if (m2 != null) {
                materiasDisponibles.add(m2);
                usados += m2.getCreditos();
            }
        }

        return usados;
    }

    // Darle valor a las materias con prioridades
    public List<MateriaConPuntajeDTO> calcularPuntajes(List<Materia> materias, Progreso progreso,
            Proyeccion proyeccion, boolean[] prioridades) {
        List<MateriaConPuntajeDTO> materiasConPuntaje = new ArrayList<>();

        for (Materia materia : materias) {
            double puntaje = calcularPuntajeMateria(materia, progreso, proyeccion, prioridades);
            materiasConPuntaje.add(new MateriaConPuntajeDTO(materia, puntaje));
        }

        // Ordenar de mayor a menor para dar prioridad
        materiasConPuntaje.sort((a, b) -> Double.compare(b.getPuntaje(), a.getPuntaje()));

        return materiasConPuntaje;
    }

    // Combinaciones
    public Set<Simulacion> generarMejoresCombinacionesUnicas(List<MateriaConPuntajeDTO> materiasConPuntaje, int creditosMax,int materiasMax, int iteraciones) {

        Set<Simulacion> todasLasCombinaciones = new HashSet<>();
        Set<Materia> combinacionActual = new HashSet<>();

        backtrackCombinacionesUnicas(materiasConPuntaje, combinacionActual, todasLasCombinaciones, 0, creditosMax,
                materiasMax, 0, 0);

        //Filtrar combinaciones que aprovechen bien los límites y ordenar
        todasLasCombinaciones = filtrarYOrdenarCombinaciones(todasLasCombinaciones, creditosMax, materiasMax);

        return todasLasCombinaciones.stream().limit(iteraciones).collect(Collectors.toSet());
    }

    // Backtracking para generar combinaciones de materias únicas
    public void backtrackCombinacionesUnicas(List<MateriaConPuntajeDTO> materias, Set<Materia> combinacionActual,
            Set<Simulacion> resultado, int indice, int creditosMax, int materiasMax, int creditosActuales,
            int materiasActuales) {

        // Condición de parada: hemos revisado todas las materias
        if (indice == materias.size()) {
            if (!combinacionActual.isEmpty()) {
                contadorCombinaciones++;
                Set<Materia> setCombinacion = new HashSet<>(combinacionActual);
                List<Materia> listaCombinacion = new ArrayList<>(combinacionActual);
                double puntajeTotal = calcularPuntajeCombinacion(listaCombinacion, materias);
                resultado.add(new Simulacion(setCombinacion, puntajeTotal));
            }
            return;
        }

        // Condición de parada temprana: ya alcanzamos ambos límites
        if (materiasActuales == materiasMax && creditosActuales >= creditosMax) {
            if (!combinacionActual.isEmpty()) {
                contadorCombinaciones++;
                Set<Materia> setCombinacion = new HashSet<>(combinacionActual);
                List<Materia> listaCombinacion = new ArrayList<>(combinacionActual);
                double puntajeTotal = calcularPuntajeCombinacion(listaCombinacion, materias);
                resultado.add(new Simulacion(setCombinacion, puntajeTotal));
            }
            return;
        }

        Materia materiaActual = materias.get(indice).getMateria();

        // Incluir la materia si es posible (no exceder ningún límite)
        if (creditosActuales + materiaActual.getCreditos() <= creditosMax && materiasActuales + 1 <= materiasMax) {
            combinacionActual.add(materiaActual);
            backtrackCombinacionesUnicas(materias, combinacionActual, resultado, indice + 1, creditosMax, materiasMax,
                    creditosActuales + materiaActual.getCreditos(), materiasActuales + 1);
            combinacionActual.remove(materiaActual);
        }

        // No incluir la materia (continuar explorando)
        backtrackCombinacionesUnicas(materias, combinacionActual, resultado, indice + 1, creditosMax, materiasMax,
                creditosActuales, materiasActuales);
    }

    // Filtrar y ordenar combinaciones priorizando las que mejor aprovechan los límites
    public Set<Simulacion> filtrarYOrdenarCombinaciones(Set<Simulacion> combinaciones, int creditosMax, int materiasMax) {
        return combinaciones.stream()
                .sorted((a, b) -> {
                    // Calcular qué tan cerca están de los límites (factor de aprovechamiento)
                    double aprovechamientoA = calcularAprovechamientoLimites(a, creditosMax, materiasMax);
                    double aprovechamientoB = calcularAprovechamientoLimites(b, creditosMax, materiasMax);
                    
                    // Primero comparar por aprovechamiento de límites
                    int comparacionAprovechamiento = Double.compare(aprovechamientoB, aprovechamientoA);
                    if (comparacionAprovechamiento != 0) {
                        return comparacionAprovechamiento;
                    }
                    
                    // Si el aprovechamiento es similar, comparar por puntaje
                    return Double.compare(b.getPuntajeTotal(), a.getPuntajeTotal());
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    // Calcular qué tan bien aprovecha una combinación los límites disponibles
    public double calcularAprovechamientoLimites(Simulacion simulacion, int creditosMax, int materiasMax) {
        int creditos = simulacion.getTotalCreditos();
        int materias = simulacion.getMaterias().size();
        
        // Porcentaje de aprovechamiento de créditos (0.0 a 1.0)
        double aprovechamientoCreditos = (double) creditos / creditosMax;
        
        // Porcentaje de aprovechamiento de materias (0.0 a 1.0)
        double aprovechamientoMaterias = (double) materias / materiasMax;
        
        // Promedio ponderado dando más peso a los créditos (típicamente más restrictivo)
        double aprovechamientoTotal = (aprovechamientoCreditos * 0.6) + (aprovechamientoMaterias * 0.4);
        
        // Bonificación por alcanzar exactamente los límites
        double bonificacion = 0.0;
        if (creditos == creditosMax || materias == materiasMax) {
            bonificacion = 0.1; // 10% de bonificación
        }
        
        // Penalización por estar muy por debajo de los límites
        double penalizacion = 0.0;
        if (aprovechamientoCreditos < 0.7 && aprovechamientoMaterias < 0.7) {
            penalizacion = 0.1; // 10% de penalización si ambos están por debajo del 70%
        }
        
        return Math.min(1.0, aprovechamientoTotal + bonificacion - penalizacion);
    }

    // Puntaje de la combinación de materias
    public double calcularPuntajeCombinacion(List<Materia> combinacion,
            List<MateriaConPuntajeDTO> materiasConPuntaje) {
        double puntajeTotal = 0;

        for (Materia materia : combinacion) {
            for (MateriaConPuntajeDTO mp : materiasConPuntaje) {
                if (mp.getMateria().getCodigo().equals(materia.getCodigo())) {
                    puntajeTotal += mp.getPuntaje();
                    break;
                }
            }
        }

        return puntajeTotal;
    }

    // Materias con puntajes
    public void mostrarMateriasPuntajes(List<MateriaConPuntajeDTO> materiasConPuntaje) {
        System.out.println("\nMATERIAS DISPONIBLES CON PUNTAJES");
        for (int i = 0; i < materiasConPuntaje.size(); i++) {
            MateriaConPuntajeDTO mp = materiasConPuntaje.get(i);
            System.out.printf("%d. %s (%s) - %d créditos - Sem %d - Puntaje: %.2f%n",
                    i + 1,
                    mp.getMateria().getNombre(),
                    mp.getMateria().getCodigo(),
                    mp.getMateria().getCreditos(),
                    mp.getMateria().getSemestre(),
                    mp.getPuntaje());
        }
    }
    
    // Resultados
    public void mostrarResultadosCombinaciones(Set<Simulacion> combinaciones) {
        System.out.println("\nMEJORES COMBINACIONES ENCONTRADAS");

        List<Simulacion> listaOrdenada = new ArrayList<>(combinaciones);
        listaOrdenada.sort((a, b) -> Double.compare(b.getPuntajeTotal(), a.getPuntajeTotal()));

        for (int i = 0; i < 5; i++) {
            Simulacion comb = listaOrdenada.get(i);
            System.out.printf("\n--- COMBINACIÓN %d (Puntaje: %.1f, Créditos: %d) ---%n",
                    i + 1, comb.getPuntajeTotal(), comb.getTotalCreditos());

            int contador = 1;
            for (Materia materia : comb.getMaterias()) {
                System.out.printf("  %d. %s (%s) - %d créditos - Semestre %d%n",
                        contador,
                        materia.getNombre(),
                        materia.getCodigo(),
                        materia.getCreditos(),
                        materia.getSemestre());
                contador++;
            }
        }

        if (combinaciones.isEmpty()) {
            System.out.println("\nNo se encontraron combinaciones válidas");
        }
    }

    // Generar combinaciones principales
    public Set<Simulacion> generarCombinaciones(Progreso progreso, Proyeccion proyeccion, List<Materia> materiasPensum, int numCombinaciones, boolean[] prioridades, boolean practicaProfesional) {

        List<Materia> materiasDisponibles = filtrarMateriasDisponibles(progreso, materiasPensum, proyeccion);
        List<MateriaConPuntajeDTO> materiasConPuntaje = calcularPuntajes(materiasDisponibles, progreso, proyeccion,prioridades);
        //mostrarMateriasPuntajes(materiasConPuntaje);
        Set<Simulacion> mejoresCombinacionesUnicas = generarMejoresCombinacionesUnicas(materiasConPuntaje, proyeccion.getNumMaxCreditos(), proyeccion.getNumMaxMaterias(), numCombinaciones);
        //mostrarResultadosCombinaciones(mejoresCombinacionesUnicas);

        return mejoresCombinacionesUnicas;
    }

    // Mostrar resultados de la simulación
    public void mostrarResultados(Map<Integer, Simulacion> ruta) {
        for (Map.Entry<Integer, Simulacion> entry : ruta.entrySet()) {
            int semestre = entry.getKey();
            Simulacion sim = entry.getValue();

            System.out.println("\n--- SEMESTRE " + semestre + " ---");
            System.out.println("Materias: " + sim.getMaterias().size());
            int creditosSemestre = sim.getMaterias().stream().mapToInt(Materia::getCreditos).sum();
            System.out.println("Créditos: " + creditosSemestre);

            int i = 1;
            for (Materia materia : sim.getMaterias()) {
                System.out.printf("  %d. %s (%s) - %d créditos%n",
                        i, materia.getNombre(), materia.getCodigo(), materia.getCreditos());
                i++;
            }
        }
    }
    
    // Versión sobrecargada que incluye análisis de pendientes
    public void mostrarResultados(Map<Integer, Simulacion> ruta, Progreso progresoInicial) {
        mostrarResultados(ruta);
        analizarMateriasPendientes(ruta, progresoInicial);
    }

    public Proyeccion crearProyeccionParaSemestre(Proyeccion base, int semestre) {
        Proyeccion proyeccion = new Proyeccion();
        proyeccion.setSemestre(semestre);
        proyeccion.setNumMaxCreditos(base.getNumMaxCreditos());
        proyeccion.setNumMaxMaterias(base.getNumMaxMaterias());
        return proyeccion;
    }

    //Métodos para test
    public Map<String, Object> generarSimulacionConEstadisticas(Progreso progreso, Proyeccion proyeccionBase,
            int semestreObjetivo, boolean[] prioridades, int limiteCombinaciones, boolean practicaProfesional, String correo) throws Exception {

        // Guardar configuración original de salida
        PrintStream originalOut = System.out;

        List<Materia> materiasPensum = pensumService.obtenerPensumJson();
        
        try {
            // Redirigir salida para evitar spam en tests
            System.setOut(new PrintStream(new ByteArrayOutputStream()));
            
            // Resetear contadores
            contadorCombinaciones = 0;
            contadorNodosCreados = 0;
            
            long tiempoInicio = System.currentTimeMillis();
            
            // Usar la versión con límite personalizable
            Map<Integer, Simulacion> resultado = generarSimulacionMultiSemestreAStarConLimite(
                progreso, proyeccionBase, semestreObjetivo, materiasPensum, prioridades, limiteCombinaciones, practicaProfesional, correo);
            
            long tiempoTotal = System.currentTimeMillis() - tiempoInicio;
            
            // Crear estadísticas
            Map<String, Object> estadisticas = new HashMap<>();
            estadisticas.put("tiempoMs", tiempoTotal);
            estadisticas.put("nodosCreados", contadorNodosCreados);
            estadisticas.put("combinacionesGeneradas", contadorCombinaciones);
            estadisticas.put("solucionEncontrada", !resultado.isEmpty());
            estadisticas.put("semestresSimulados", resultado.size());
            estadisticas.put("limiteCombinaciones", limiteCombinaciones);
            
            return estadisticas;
            
        } finally {
            // Restaurar salida original
            System.setOut(originalOut);
        }
    }

    // ALGORITMO A* CON LÍMITE PERSONALIZABLE
    public Map<Integer, Simulacion> generarSimulacionMultiSemestreAStarConLimite(Progreso progreso, Proyeccion proyeccionBase,
            int semestreObjetivo, List<Materia> materiasPensum, boolean[] prioridades, int limiteCombinaciones, boolean practicaProfesional, String correo) {

        // Resetear contadores
        contadorCombinaciones = 0;
        contadorNodosCreados = 0;
        
        /*contadorIdNodos = 0;
        mapaNodosIds.clear();*/

        System.out.println("================ INICIO SIMULACIÓN A* (Límite: " + limiteCombinaciones + ") ================");
        System.out.println("Semestre actual: " + progreso.getSemestre());
        System.out.println("Semestre objetivo: " + semestreObjetivo);

        int maxNodos = 25000;
        
        PriorityQueue<NodoA> frontera = new PriorityQueue<>(Comparator.comparingDouble(NodoA::getCostoTotal));
        Set<String> visitados = new HashSet<>();

        Map<Integer, Simulacion> rutaInicial = new HashMap<>();
        double heuristicaInicial = calcularHeuristica(progreso, semestreObjetivo, proyeccionBase, materiasPensum, prioridades, progreso.getSemestre());

        NodoA nodoInicial = new NodoA(rutaInicial, progreso.getSemestre(), heuristicaInicial, progreso);
        contadorNodosCreados++; // Contar el nodo inicial

        frontera.offer(nodoInicial);

        int nodosExplorados = 0;
        long tiempoInicio = System.currentTimeMillis();

        while (!frontera.isEmpty() && nodosExplorados < maxNodos) {
            NodoA nodoActual = frontera.poll();
            nodosExplorados++;

            if (haCompletadoTodasLasMaterias(nodoActual.getProgresoActual())) {
                long tiempoTotal = System.currentTimeMillis() - tiempoInicio;
                System.out.println("SOLUCION OPTIMA A* ENCONTRADA (Termino antes)");
                System.out.println("Semestre de finalización: " + nodoActual.getSemestreActual());
                System.out.println("Semestre objetivo original: " + semestreObjetivo);
                System.out.println("Semestres ahorrados: " + (semestreObjetivo - nodoActual.getSemestreActual()));
                System.out.println("Nodos explorados: " + nodosExplorados);
                System.out.println("Nodos creados: " + contadorNodosCreados);
                System.out.println("Combinaciones generadas: " + contadorCombinaciones);
                System.out.println("Tiempo total: " + tiempoTotal + "ms");
                System.out.println("Heurística inicial: " + heuristicaInicial);

                Map<Integer, Simulacion> rutaCompleta = ordenarRuta(nodoActual.getRutaParcial());
                rutaCompleta = convertirSimulaciones(rutaCompleta, proyeccionBase, correo);
                mostrarResultados(rutaCompleta);
                return rutaCompleta;
            }

            if (nodoActual.getSemestreActual() == semestreObjetivo) {
                double heuristicaActual = calcularHeuristica(nodoActual.getProgresoActual(), semestreObjetivo, proyeccionBase, materiasPensum, prioridades, nodoActual.getSemestreActual());
                
                int nodosMinimosParaComparar = Math.min(50, maxNodos / 10); 
                
                if (nodosExplorados >= nodosMinimosParaComparar) {
                    boolean hayMejorOpcion = false;
                    for (NodoA nodoFrontera : frontera) {
                        double heuristicaFrontera = calcularHeuristica(nodoFrontera.getProgresoActual(), semestreObjetivo, proyeccionBase, materiasPensum, prioridades, nodoFrontera.getSemestreActual());
                        if (heuristicaFrontera < heuristicaActual) {
                            hayMejorOpcion = true;
                            break;
                        }
                    }
                    
                    if (!hayMejorOpcion || nodosExplorados >= maxNodos / 2) {
                        long tiempoTotal = System.currentTimeMillis() - tiempoInicio;
                        System.out.println("SOLUCION A* ENCONTRADA (Semestre objetivo alcanzado)");
                        System.out.println("Heurística del nodo seleccionado: " + heuristicaActual);
                        System.out.println("Nodos explorados: " + nodosExplorados);
                        System.out.println("Nodos creados: " + contadorNodosCreados);
                        System.out.println("Combinaciones generadas: " + contadorCombinaciones);
                        System.out.println("Tiempo total: " + tiempoTotal + "ms");
                        System.out.println("Heurística inicial: " + heuristicaInicial);

                        Map<Integer, Simulacion> rutaCompleta = ordenarRuta(nodoActual.getRutaParcial());
                        rutaCompleta = convertirSimulaciones(rutaCompleta, proyeccionBase, correo);
                        mostrarResultados(rutaCompleta, progreso);
                        return rutaCompleta;
                    }
                }
            }

            String claveEstado = generarClaveEstado(nodoActual.getProgresoActual(), nodoActual.getSemestreActual());
            if (visitados.contains(claveEstado))
                continue;
            visitados.add(claveEstado);

            expandirNodo(nodoActual, frontera, semestreObjetivo, proyeccionBase, materiasPensum,
                    limiteCombinaciones, prioridades, practicaProfesional);
        }

        long tiempoTotal = System.currentTimeMillis() - tiempoInicio;
        System.out.println("A* alcanzó límite de nodos: " + maxNodos);
        System.out.println("Nodos creados: " + contadorNodosCreados);
        System.out.println("Combinaciones generadas: " + contadorCombinaciones);
        System.out.println("Tiempo transcurrido: " + tiempoTotal + "ms");
        System.out.println("A* no pudo encontrar ninguna solución completa");
        return new HashMap<>();
    }

    // Método para probar actualizarProgresoTemporal de forma aislada
    public void probarActualizacionProgreso(Progreso progreso, Simulacion simulacion, int semestre) {
        System.out.println("=== PRUEBA DE ACTUALIZACIÓN DE PROGRESO ===");
        System.out.println("Semestre: " + semestre);
        
        // Estado inicial
        System.out.println("\n--- ESTADO INICIAL ---");
        System.out.println("Materias faltantes: " + progreso.getListaMateriasFaltantes().size());
        System.out.println("Faltan electivas: " + progreso.getFaltanElectiva());
        System.out.println("Faltan complementarias: " + progreso.getFaltanComplementaria());
        System.out.println("Faltan énfasis: " + progreso.getFaltanEnfasis());
        System.out.println("Faltan electivas CB: " + progreso.getFaltanElectivaBasicas());
        
        if (progreso.getCursosElectivas() != null) {
            System.out.println("Electivas cursadas: " + progreso.getCursosElectivas().size());
        }
        if (progreso.getCursosEnfasis() != null) {
            System.out.println("Énfasis cursados: " + progreso.getCursosEnfasis().size());
        }
        
        // Materias a simular
        System.out.println("\n--- MATERIAS A SIMULAR ---");
        for (Materia m : simulacion.getMaterias()) {
            System.out.println("- " + m.getNombre() + " (" + m.getCodigo() + ") - " + m.getCreditos() + " créditos");
        }
        
        // Hacer copia y actualizar
        Progreso progresoActualizado = progreso.copy();
        progresoActualizado = actualizarProgresoTemporal(progresoActualizado, simulacion, semestre);
        
        // Estado final
        System.out.println("\n--- ESTADO DESPUÉS DE ACTUALIZAR ---");
        System.out.println("Materias faltantes: " + progresoActualizado.getListaMateriasFaltantes().size());
        System.out.println("Faltan electivas: " + progresoActualizado.getFaltanElectiva());
        System.out.println("Faltan complementarias: " + progresoActualizado.getFaltanComplementaria());
        System.out.println("Faltan énfasis: " + progresoActualizado.getFaltanEnfasis());
        System.out.println("Faltan electivas CB: " + progresoActualizado.getFaltanElectivaBasicas());
        
        if (progresoActualizado.getCursosElectivas() != null) {
            System.out.println("Electivas cursadas: " + progresoActualizado.getCursosElectivas().size());
        }
        if (progresoActualizado.getCursosEnfasis() != null) {
            System.out.println("Énfasis cursados: " + progresoActualizado.getCursosEnfasis().size());
        }
        
        System.out.println("===========================================\n");
    }

    // Función para analizar materias y créditos pendientes después de una solución
    public void analizarMateriasPendientes(Map<Integer, Simulacion> solucion, Progreso progresoInicial) {
        System.out.println("\n================ ANÁLISIS DE MATERIAS PENDIENTES ================");
        
        // Crear una copia del progreso inicial para simular toda la ruta
        Progreso progresoFinal = progresoInicial.copy();
        
        // Aplicar todas las simulaciones de la solución
        for (Map.Entry<Integer, Simulacion> entry : solucion.entrySet()) {
            int semestre = entry.getKey();
            Simulacion sim = entry.getValue();
            progresoFinal = actualizarProgresoTemporal(progresoFinal, sim, semestre);
        }
        
        // Contar materias de núcleo faltantes
        int materiasNucleoFaltantes = contarMateriasNucleoFaltantes(progresoFinal);
        int creditosNucleoFaltantes = contarCreditosNucleoFaltantes(progresoFinal);
        
        // Contar créditos faltantes por tipo
        int creditosElectivasFaltantes = Math.max((int)progresoFinal.getFaltanElectiva(), 0);
        int creditosComplementariasFaltantes = Math.max((int)progresoFinal.getFaltanComplementaria(), 0);
        int creditosEnfasisFaltantes = Math.max((int)progresoFinal.getFaltanEnfasis(), 0);
        int creditosElectivasCBFaltantes = Math.max((int)progresoFinal.getFaltanElectivaBasicas(), 0);
        
        // Mostrar resumen
        System.out.println("--- MATERIAS DE NÚCLEO PENDIENTES ---");
        System.out.println("Materias faltantes: " + materiasNucleoFaltantes);
        System.out.println("Créditos faltantes: " + creditosNucleoFaltantes);
        
        if (materiasNucleoFaltantes > 0) {
            System.out.println("Materias específicas pendientes:");
            int contador = 1;
            for (Materia materia : progresoFinal.getListaMateriasFaltantes()) {
                if (esMateriaNucleo(materia)) {
                    System.out.printf("  %d. %s (%s) - %d créditos - Sem %d%n",
                            contador, materia.getNombre(), materia.getCodigo(), 
                            materia.getCreditos(), materia.getSemestre());
                    contador++;
                }
            }
        }
        
        System.out.println("\n--- CRÉDITOS ELECTIVOS PENDIENTES ---");
        System.out.println("Electivas generales: " + creditosElectivasFaltantes + " créditos");
        System.out.println("Complementarias: " + creditosComplementariasFaltantes + " créditos");
        System.out.println("Énfasis: " + creditosEnfasisFaltantes + " créditos");
        System.out.println("Electivas Ciencias Básicas: " + creditosElectivasCBFaltantes + " créditos");
        
        // Calcular total pendiente
        int totalCreditosPendientes = creditosNucleoFaltantes + creditosElectivasFaltantes + 
                                     creditosComplementariasFaltantes + creditosEnfasisFaltantes + 
                                     creditosElectivasCBFaltantes;
        
        System.out.println("\n--- RESUMEN TOTAL ---");
        System.out.println("Total créditos pendientes: " + totalCreditosPendientes);
        System.out.println("Total materias de núcleo pendientes: " + materiasNucleoFaltantes);
        
        // Verificar si se completó el programa
        if (haCompletadoTodasLasMaterias(progresoFinal)) {
            System.out.println("¡PROGRAMA ACADÉMICO COMPLETADO!");
        } else {
            System.out.println("Aún faltan requisitos por cumplir");
            
            // Estimar semestres adicionales necesarios
            if (totalCreditosPendientes > 0) {
                int semestresPendientes = (int) Math.ceil(totalCreditosPendientes / 15.0); // Asumiendo 15 créditos por semestre
                System.out.println("Estimado de semestres adicionales: " + semestresPendientes);
            }
        }
        
        System.out.println("================================================================\n");
    }
    
    // Función sobrecargada que también acepta el progreso final calculado
    public void analizarMateriasPendientes(Progreso progresoFinal) {
        System.out.println("\n================ ANÁLISIS DE MATERIAS PENDIENTES ================");
        
        // Contar materias de núcleo faltantes
        int materiasNucleoFaltantes = contarMateriasNucleoFaltantes(progresoFinal);
        int creditosNucleoFaltantes = contarCreditosNucleoFaltantes(progresoFinal);
        
        // Contar créditos faltantes por tipo
        int creditosElectivasFaltantes = Math.max((int)progresoFinal.getFaltanElectiva(), 0);
        int creditosComplementariasFaltantes = Math.max((int)progresoFinal.getFaltanComplementaria(), 0);
        int creditosEnfasisFaltantes = Math.max((int)progresoFinal.getFaltanEnfasis(), 0);
        int creditosElectivasCBFaltantes = Math.max((int)progresoFinal.getFaltanElectivaBasicas(), 0);
        
        // Mostrar resumen
        System.out.println("--- MATERIAS DE NÚCLEO PENDIENTES ---");
        System.out.println("Materias faltantes: " + materiasNucleoFaltantes);
        System.out.println("Créditos faltantes: " + creditosNucleoFaltantes);
        
        System.out.println("\n--- CRÉDITOS ELECTIVOS PENDIENTES ---");
        System.out.println("Electivas generales: " + creditosElectivasFaltantes + " créditos");
        System.out.println("Complementarias: " + creditosComplementariasFaltantes + " créditos");
        System.out.println("Énfasis: " + creditosEnfasisFaltantes + " créditos");
        System.out.println("Electivas Ciencias Básicas: " + creditosElectivasCBFaltantes + " créditos");
        
        // Calcular total pendiente
        int totalCreditosPendientes = creditosNucleoFaltantes + creditosElectivasFaltantes + 
                                     creditosComplementariasFaltantes + creditosEnfasisFaltantes + 
                                     creditosElectivasCBFaltantes;
        
        System.out.println("\n--- RESUMEN TOTAL ---");
        System.out.println("Total créditos pendientes: " + totalCreditosPendientes);
        System.out.println("Total materias de núcleo pendientes: " + materiasNucleoFaltantes);
        
        if (haCompletadoTodasLasMaterias(progresoFinal)) {
            System.out.println("¡PROGRAMA ACADÉMICO COMPLETADO!");
        } else {
            System.out.println("Aún faltan requisitos por cumplir");
        }
        
        System.out.println("================================================================\n");
    }
    
    // Función que devuelve los datos estructurados en lugar de solo imprimirlos
    public Map<String, Object> calcularMateriasPendientes(Map<Integer, Simulacion> solucion, Progreso progresoInicial) {
        // Crear una copia del progreso inicial para simular toda la ruta
        Progreso progresoFinal = progresoInicial.copy();
        
        // Aplicar todas las simulaciones de la solución
        for (Map.Entry<Integer, Simulacion> entry : solucion.entrySet()) {
            int semestre = entry.getKey();
            Simulacion sim = entry.getValue();
            progresoFinal = actualizarProgresoTemporal(progresoFinal, sim, semestre);
        }

        return calcularMateriasPendientes(progresoFinal);
    }
    
    // Función sobrecargada que devuelve datos estructurados del progreso final
    public Map<String, Object> calcularMateriasPendientes(Progreso progresoFinal) {
        Map<String, Object> resultado = new HashMap<>();
        
        // Contar materias de núcleo faltantes
        int materiasNucleoFaltantes = contarMateriasNucleoFaltantes(progresoFinal);
        int creditosNucleoFaltantes = contarCreditosNucleoFaltantes(progresoFinal);
        
        // Contar créditos faltantes por tipo
        int creditosElectivasFaltantes = Math.max((int)progresoFinal.getFaltanElectiva(), 0);
        int creditosComplementariasFaltantes = Math.max((int)progresoFinal.getFaltanComplementaria(), 0);
        int creditosEnfasisFaltantes = Math.max((int)progresoFinal.getFaltanEnfasis(), 0);
        int creditosElectivasCBFaltantes = Math.max((int)progresoFinal.getFaltanElectivaBasicas(), 0);
        
        // Obtener lista de materias de núcleo pendientes
        List<Materia> materiasNucleoPendientes = new ArrayList<>();
        for (Materia materia : progresoFinal.getListaMateriasFaltantes()) {
            if (esMateriaNucleo(materia)) {
                materiasNucleoPendientes.add(materia);
            }
        }
        
        // Calcular total pendiente
        int totalCreditosPendientes = creditosNucleoFaltantes + creditosElectivasFaltantes + 
                                     creditosComplementariasFaltantes + creditosEnfasisFaltantes + 
                                     creditosElectivasCBFaltantes;
        
        // Llenar el mapa de resultados
        resultado.put("materiasNucleoFaltantes", materiasNucleoFaltantes);
        resultado.put("creditosNucleoFaltantes", creditosNucleoFaltantes);
        resultado.put("creditosElectivasFaltantes", creditosElectivasFaltantes);
        resultado.put("creditosComplementariasFaltantes", creditosComplementariasFaltantes);
        resultado.put("creditosEnfasisFaltantes", creditosEnfasisFaltantes);
        resultado.put("creditosElectivasCBFaltantes", creditosElectivasCBFaltantes);
        resultado.put("totalCreditosPendientes", totalCreditosPendientes);
        resultado.put("materiasNucleoPendientes", materiasNucleoPendientes);
        resultado.put("programaCompleto", haCompletadoTodasLasMaterias(progresoFinal));
        
        // Estimar semestres adicionales
        int semestresPendientes = totalCreditosPendientes > 0 ? (int) Math.ceil(totalCreditosPendientes / 15.0) : 0;
        resultado.put("semestresPendientesEstimados", semestresPendientes);
        
        return resultado;
    }

    //Para archivo txt
    /* 
    private void inicializarLog(int semestreInicial, int semestreObjetivo) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            logFileName = "simulacion_astar_" + timestamp + ".txt";
            logWriter = new FileWriter(logFileName, false);
            
            logWriter.write("===============================================\n");
            logWriter.write("ANÁLISIS DE SIMULACIÓN A*\n");
            logWriter.write("===============================================\n");
            logWriter.write("Fecha: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
            logWriter.write("Semestre inicial: " + semestreInicial + "\n");
            logWriter.write("Semestre objetivo: " + semestreObjetivo + "\n");
            logWriter.write("===============================================\n\n");
            
            // Encabezados de las columnas
            logWriter.write(String.format("%-8s %-10s %-8s %-10s %-12s %-12s %-12s %-8s %-8s %-60s\n", 
                "ID_NODO", "TIPO_NODO", "ID_PADRE", "SEMESTRE", "F(n)=G+H", "G(n)", "H(n)", "CREDITOS", "N_MATS", "MATERIAS_SIMULACION"));
            logWriter.write("=".repeat(160) + "\n");
            
            logWriter.flush();
            System.out.println("Log de simulación iniciado: " + logFileName);
        } catch (IOException e) {
            System.err.println("Error al inicializar el log: " + e.getMessage());
        }
    } */

    /*
    
    private void logNodoDetallado(NodoA nodo, double funcionG, double heuristica, String tipo, Simulacion simulacion, int idNodo, int idPadre) {
        if (logWriter == null) return;
        
        try {
            String materiasSimulacionStr = "";
            int creditosSimulacion = 0;
            
            // Construir string de materias de la simulación actual
            if (simulacion != null && simulacion.getMaterias() != null && !simulacion.getMaterias().isEmpty()) {
                List<String> nombresMaterias = new ArrayList<>();
                for (Materia materia : simulacion.getMaterias()) {
                    String nombre = materia.getNombre();
                    if (nombre.length() > 20) {
                        nombre = nombre.substring(0, 17) + "...";
                    }
                    nombresMaterias.add(nombre + "(" + materia.getCreditos() + "c)");
                    creditosSimulacion += materia.getCreditos();
                }
                materiasSimulacionStr = String.join(", ", nombresMaterias);
            } else if ("INICIAL".equals(tipo)) {
                materiasSimulacionStr = "NODO_INICIAL";
            } else {
                materiasSimulacionStr = "SIN_MATERIAS";
            }
            
            if (materiasSimulacionStr.length() > 55) {
                materiasSimulacionStr = materiasSimulacionStr.substring(0, 52) + "...";
            }
            
            double funcionF = funcionG + heuristica;
            String idPadreStr = (idPadre == -1) ? "ROOT" : String.valueOf(idPadre);
            
            int numeroMaterias = 0;
            if (simulacion != null && simulacion.getMaterias() != null) {
                numeroMaterias = simulacion.getMaterias().size();
            }
            
            logWriter.write(String.format("%-8d %-10s %-8s %-10d %-12.2f %-12.2f %-12.2f %-8d %-8d %-60s\n",
                idNodo,
                tipo,
                idPadreStr,
                nodo.getSemestreActual(),
                funcionF,
                funcionG,
                heuristica,
                creditosSimulacion,
                numeroMaterias,
                materiasSimulacionStr
            ));
            
            if ("EXPLORADO".equals(tipo) || "INICIAL".equals(tipo)) {
                logWriter.write("  PROGRESO ACTUAL:\n");
                
                List<Materia> materiasFaltantes = nodo.getProgresoActual().getListaMateriasFaltantes();
                logWriter.write("    Materias núcleo faltantes (" + materiasFaltantes.size() + "): ");
                if (materiasFaltantes.size() <= 5) {
                    List<String> nombresFaltantes = new ArrayList<>();
                    for (Materia m : materiasFaltantes) {
                        String nombre = m.getNombre();
                        if (nombre.length() > 15) {
                            nombre = nombre.substring(0, 12) + "...";
                        }
                        nombresFaltantes.add(nombre + "(" + m.getCreditos() + "c)");
                    }
                    logWriter.write(String.join(", ", nombresFaltantes) + "\n");
                } else {
                    logWriter.write(materiasFaltantes.size() + " materias (mostrando primeras 3): ");
                    for (int i = 0; i < Math.min(3, materiasFaltantes.size()); i++) {
                        Materia m = materiasFaltantes.get(i);
                        String nombre = m.getNombre();
                        if (nombre.length() > 15) {
                            nombre = nombre.substring(0, 12) + "...";
                        }
                        logWriter.write(nombre + "(" + m.getCreditos() + "c)");
                        if (i < 2 && i < materiasFaltantes.size() - 1) logWriter.write(", ");
                    }
                    logWriter.write("...\n");
                }
                
                logWriter.write("    Créditos faltantes - Electivas: " + (int)nodo.getProgresoActual().getFaltanElectiva() + 
                               ", Complementarias: " + (int)nodo.getProgresoActual().getFaltanComplementaria() + 
                               ", Énfasis: " + (int)nodo.getProgresoActual().getFaltanEnfasis() + 
                               ", ElectivasCB: " + (int)nodo.getProgresoActual().getFaltanElectivaBasicas() + "\n");
                
                List<MateriaDTO> materiasCursadas = nodo.getProgresoActual().getMaterias();
                if (materiasCursadas != null && !materiasCursadas.isEmpty()) {
                    logWriter.write("    Total materias cursadas: " + materiasCursadas.size());
                    if (materiasCursadas.size() <= 3) {
                        logWriter.write(" - ");
                        List<String> nombresUltimas = new ArrayList<>();
                        for (MateriaDTO m : materiasCursadas) {
                            String nombre = m.getTitulo();
                            if (nombre.length() > 15) {
                                nombre = nombre.substring(0, 12) + "...";
                            }
                            nombresUltimas.add(nombre);
                        }
                        logWriter.write(String.join(", ", nombresUltimas));
                    }
                    logWriter.write("\n");

    */

    
    /*/
    private void cerrarLog() {
        if (logWriter != null) {
            try {
                logWriter.write("\n===============================================\n");
                logWriter.write("ESTADÍSTICAS FINALES\n");
                logWriter.write("===============================================\n");
                logWriter.write("Total nodos creados: " + contadorNodosCreados + "\n");
                logWriter.write("Total IDs asignados: " + contadorIdNodos + "\n");
                logWriter.write("Total combinaciones generadas: " + contadorCombinaciones + "\n");
                logWriter.write("Relaciones padre-hijo registradas en el log\n");
                logWriter.write("Archivo de log guardado en: " + logFileName + "\n");
                logWriter.write("===============================================\n");
                logWriter.write("\nINSTRUCCIONES DE ANÁLISIS:\n");
                logWriter.write("- ID_NODO: Identificador único de cada nodo\n");
                logWriter.write("- ID_PADRE: ID del nodo padre (ROOT para nodo inicial)\n");
                logWriter.write("- F(n): Función de evaluación total (G + H)\n");
                logWriter.write("- G(n): Costo acumulado desde el inicio\n");
                logWriter.write("- H(n): Heurística (estimación del costo restante)\n");
                logWriter.write("- CREDITOS: Total de créditos de las materias simuladas\n");
                logWriter.write("- N_MATS: Número de materias en la simulación actual\n");
                logWriter.write("- MATERIAS_SIMULACION: Materias de la simulación actual del nodo\n");
                logWriter.write("- PROGRESO ACTUAL: Estado detallado del progreso en nodos explorados\n");
                logWriter.write("===============================================\n");
                
                logWriter.close();
                logWriter = null;
                mapaNodosIds.clear();
                System.out.println("Log cerrado exitosamente: " + logFileName);
            } catch (IOException e) {
                System.err.println("Error al cerrar el log: " + e.getMessage());
            }
        }
    }*/

    // Verifica si una materia existe en la BD y la guarda si no existe
    private Materia verificarYGuardarMateria(Materia materia) {
        // Si la materia ya tiene ID, es que ya existe en la BD
        if (materia.getId() != null) {
            return materia;
        }

        // Para materias especiales (electivas, complementarias, etc.) se crea una nueva materia simulada
        if (materia.getCodigo().equals("0") || materia.getCodigo().equals("1") ||
                materia.getCodigo().equals("5") || materia.getCodigo().equals("6") ||
                materia.getCodigo().equals("Practica")) {

            // Estas son materias simuladas, se guardan como nuevas entidades
            return materiaRepository.save(materia);
        }

        // Para las materias que ya existen en el pensum, se busca por el código
        Optional<Materia> materiaExistente = materiaRepository.findByCodigo(materia.getCodigo());

        if (materiaExistente.isPresent()) {
            return materiaExistente.get();
        }

        // Si no existe, se guarda
        return materiaRepository.save(materia);
    }

    // Convierte las simulaciones al modelo Simulacion para guardar en la BD
    private Map<Integer, Simulacion> convertirSimulaciones(Map<Integer, Simulacion> rutaOriginal,
            Proyeccion proyeccionBase, String correo) {
        Map<Integer, Simulacion> rutaConvertida = new HashMap<>();

        if (rutaOriginal.isEmpty()) {
            return rutaConvertida;
        }

        // Se obtiene el estudiante por su correo

        Estudiante estudiante = estudianteService.obtenerEstudiantePorCorreo(correo);

        // Se crea el objeto de la proyección en la tabla de la BD
        Proyeccion proyeccion = new Proyeccion();
        proyeccion.setSemestre(
                rutaOriginal.keySet().stream().min(Integer::compareTo).orElse(proyeccionBase.getSemestre()));
        proyeccion.setNumMaxCreditos(proyeccionBase.getNumMaxCreditos());
        proyeccion.setNumMaxMaterias(proyeccionBase.getNumMaxMaterias());
        proyeccion.setEstudianteId(estudiante);

        // Se almacena en la BD
        proyeccionRepository.save(proyeccion);

        for (Map.Entry<Integer, Simulacion> entry : rutaOriginal.entrySet()) {
            Integer semestre = entry.getKey();
            Simulacion simulacionOriginal = entry.getValue();

            // Se crea una nueva simulación con los campos de la entidad Simulación
            Simulacion simulacionConvertida = new Simulacion();

            simulacionConvertida.setSemestre(semestre.longValue());
            simulacionConvertida.setPuntajeTotal(simulacionOriginal.getPuntajeTotal());
            simulacionConvertida.setProyeccionId(proyeccion);

            int creditosTotales = 0;

            // Guardar la simulación primero para obtener el ID
            simulacionConvertida.setCreditosTotales(0L);
            Simulacion simulacionGuardada = simulacionRepository.save(simulacionConvertida);

            // Se crean y guardan las asociaciones en la tabla intermedia SimulacionMateria
            Set<SimulacionMateria> materiasAsociadas = new HashSet<>();
            if (simulacionOriginal.getMaterias() != null && !simulacionOriginal.getMaterias().isEmpty()) {
                for (Materia materia : simulacionOriginal.getMaterias()) {
                    creditosTotales += materia.getCreditos();

                    // Se verifica y guarda la materia en la BD si no existe
                    Materia materiaGuardada = verificarYGuardarMateria(materia);

                    // Se agrega la asociación SimulacionMateria
                    SimulacionMateria asociacion = new SimulacionMateria();
                    asociacion.setSimulacion(simulacionGuardada);
                    asociacion.setMateria(materiaGuardada);

                    SimulacionMateria asociacionGuardada = simulacionMateriaRepository.save(asociacion);
                    materiasAsociadas.add(asociacionGuardada);
                }
            }

            // Se actualiza la simulación con los créditos totales finales
            simulacionGuardada.setCreditosTotales((long) creditosTotales);
            simulacionGuardada.setMateriasAsociadas(materiasAsociadas);
            simulacionGuardada = simulacionRepository.save(simulacionGuardada);

            rutaConvertida.put(semestre, simulacionGuardada);
        }

        return rutaConvertida;
    }

    /* CRUD DE SIMULACIÓN */

    public List<Simulacion> obtenerTodasSimulaciones() {
        return simulacionRepository.findAll();
    }

    public Optional<Simulacion> obtenerSimulacionPorId(Long id) {
        return simulacionRepository.findById(id);
    }

    public Simulacion crearSimulacion(Simulacion simulacion) {
        return simulacionRepository.save(simulacion);
    }

    public Simulacion actualizarSimulacion(Long id, Simulacion simulacion) {
        simulacion.setId(id);
        return simulacionRepository.save(simulacion);
    }

    public void eliminarSimulacion(Long id) {
        simulacionRepository.deleteById(id);
    }

    
}
