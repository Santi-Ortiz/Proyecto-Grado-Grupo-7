package com.grupo7.tesis.services;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.grupo7.tesis.dtos.MateriaConPuntajeDTO;
import com.grupo7.tesis.dtos.MateriaDTO;
import com.grupo7.tesis.models.Materia;
import com.grupo7.tesis.models.NodoA;
import com.grupo7.tesis.models.Simulacion;
import com.grupo7.tesis.models.Progreso;
import com.grupo7.tesis.models.Proyeccion;

@Service
public class SimulacionService {

    private int contadorCombinaciones = 0;
    private int contadorNodosCreados = 0;

    @Autowired
    private pensumService pensumService; 

    // ALGORITMO A*
    public Map<Integer, Simulacion> generarSimulacionMultiSemestreAStar(Progreso progreso, Proyeccion proyeccionBase,
            int semestreObjetivo, List<Materia> materiasPensum, boolean[] prioridades) {

        contadorCombinaciones = 0;
        contadorNodosCreados = 0;

        System.out.println("================ INICIO SIMULACIÓN A*  ================");
        System.out.println("Semestre actual: " + progreso.getSemestre());
        System.out.println("Semestre objetivo: " + semestreObjetivo);

        int maxNodos = 25000;
        int maxCombinacionesPorNodo = 65;

        PriorityQueue<NodoA> frontera = new PriorityQueue<>(Comparator.comparingDouble(NodoA::getCostoTotal));

        Set<String> visitados = new HashSet<>();

        Map<Integer, Simulacion> rutaInicial = new HashMap<>();
        double heuristicaInicial = calcularHeuristica(progreso, semestreObjetivo, proyeccionBase, materiasPensum, prioridades, progreso.getSemestre());

        NodoA nodoInicial = new NodoA(rutaInicial, progreso.getSemestre(), heuristicaInicial, progreso);
        contadorNodosCreados++;

        frontera.offer(nodoInicial);

        int nodosExplorados = 0;
        long tiempoInicio = System.currentTimeMillis();

        while (!frontera.isEmpty() && nodosExplorados < maxNodos) {
            NodoA nodoActual = frontera.poll();
            nodosExplorados++;

            if (haCompletadoTodasLasMaterias(nodoActual.getProgresoActual())) {
                long tiempoTotal = System.currentTimeMillis() - tiempoInicio;
                System.out.println("SOLUCION OPTIMA A* ENCONTRADA (Todas las materias completadas)");
                System.out.println("Semestre de finalización: " + nodoActual.getSemestreActual());
                System.out.println("Semestre objetivo original: " + semestreObjetivo);
                System.out.println("Semestres ahorrados: " + (semestreObjetivo - nodoActual.getSemestreActual()));
                System.out.println("Nodos explorados: " + nodosExplorados);
                System.out.println("Nodos creados: " + contadorNodosCreados);
                System.out.println("Combinaciones generadas: " + contadorCombinaciones);
                System.out.println("Tiempo total: " + tiempoTotal + "ms");
                System.out.println("Heurística inicial: " + heuristicaInicial);

                Map<Integer, Simulacion> rutaCompleta = ordenarRuta(nodoActual.getRutaParcial());
                double puntajeTotal = calcularPuntajeRuta(rutaCompleta, progreso, prioridades);
                mostrarResultados(rutaCompleta, puntajeTotal, progreso);
                return rutaCompleta;
            }

            // NUEVO: Solo parar si llegamos al objetivo Y hemos explorado suficientes alternativas
            if (nodoActual.getSemestreActual() == semestreObjetivo) {
                double heuristicaActual = calcularHeuristica(nodoActual.getProgresoActual(), semestreObjetivo, proyeccionBase, materiasPensum, prioridades, nodoActual.getSemestreActual());
                
                int nodosMinimosParaComparar = Math.min(50, maxNodos / 10); 
                
                if (nodosExplorados >= nodosMinimosParaComparar) {
                    boolean hayMejorOpcion = false;
                    for (NodoA nodoFrontera : frontera) {
                        double heuristicaFrontera = calcularHeuristica(nodoFrontera.getProgresoActual(), semestreObjetivo, proyeccionBase, materiasPensum, prioridades);
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
                        double puntajeTotal = calcularPuntajeRuta(rutaCompleta, progreso, prioridades);
                        mostrarResultados(rutaCompleta, puntajeTotal, progreso);
                        return rutaCompleta;
                    }
                }
            }

            String claveEstado = generarClaveEstado(nodoActual.getProgresoActual(), nodoActual.getSemestreActual());
            if (visitados.contains(claveEstado))
                continue;
            visitados.add(claveEstado);

            expandirNodo(nodoActual, frontera, semestreObjetivo, proyeccionBase, materiasPensum,
                    maxCombinacionesPorNodo, prioridades);
        }

        long tiempoTotal = System.currentTimeMillis() - tiempoInicio;
        System.out.println("A* alcanzó límite de nodos: " + maxNodos);
        System.out.println("Nodos creados: " + contadorNodosCreados);
        System.out.println("Combinaciones generadas: " + contadorCombinaciones);
        System.out.println("Tiempo transcurrido: " + tiempoTotal + "ms");
        System.out.println("A* no pudo encontrar ninguna solución completa");
        return new HashMap<>();
    }

    public void expandirNodo(NodoA nodoActual, PriorityQueue<NodoA> frontera, int semestreObjetivo,
            Proyeccion proyeccionBase, List<Materia> materiasPensum, int maxCombinaciones, boolean[] prioridades) {

        int siguienteSemestre = nodoActual.getSemestreActual() + 1;
        if (siguienteSemestre > semestreObjetivo)
            return;

        Proyeccion proyeccionSemestre = crearProyeccionParaSemestre(proyeccionBase, siguienteSemestre);

        Set<Simulacion> combinaciones = generarCombinaciones(nodoActual.getProgresoActual(), proyeccionSemestre,
                materiasPensum, maxCombinaciones, prioridades);

        for (Simulacion combinacion : combinaciones) {
            Map<Integer, Simulacion> nuevaRuta = new HashMap<>(nodoActual.getRutaParcial());

            Simulacion simulacionSemestre = new Simulacion();
            for (Materia materia : combinacion.getMaterias()) {
                simulacionSemestre.agregarMateria(materia);
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
            contadorNodosCreados++; // Contar cada nuevo nodo creado
            frontera.offer(nuevoNodo);
        }
    }

    // Nueva versión de actualizar progreso temporal para que sirva con A*
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
                        "SimSem" + semestreSimulado, // cicloLectivo
                        "ELEC", // materia
                        "000", // numeroCat
                        "ELEC" + System.nanoTime(), // curso - ID único temporal
                        materia.getNombre(), // titulo
                        "3.0", // calif - Calificación aprobatoria
                        String.valueOf(materia.getCreditos()), // cred
                        "" // tipo
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
                        String.valueOf(materia.getCreditos()), // Usar créditos reales de la materia
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
                        String.valueOf(materia.getCreditos()), // Usar créditos reales de la materia
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
                        String.valueOf(materia.getCreditos()), // Usar créditos reales de la materia
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
                "SimSem" + semestreSimulado, // cicloLectivo - Marcar como simulado
                materiaRemovida.getCodigo(), // materia
                "000", // numeroCat
                materiaRemovida.getCodigo(), // curso
                materiaRemovida.getNombre(), // titulo
                "3.0", // calif - Calificación aprobatoria
                String.valueOf(materiaRemovida.getCreditos()), // cred
                "" // tipo
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

        int creditosNucleoCBFaltantes = contarCreditosNucleoCBFaltantes(progreso);
        int creditosNucleoIngenieria = contarCreditosNucleoIngeFaltantes(progreso);
        int creditosNucleoSociohumanisticas = contarCreditosNucleoSocioFaltantes(progreso);

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

        //heuristica = creditosNucleoCBFaltantes * ( (1.1) + cbPrioridad) + creditosNucleoIngenieria * ( (1.1) + ingenieriaPrioridad) + creditosNucleoSociohumanisticas * ( (1.1) + sociohumanisticasPrioridad) + electivasFaltantes * ((1.1) + electivasPrioridad) + complementariasFaltantes * ((1.1) + complementariasPrioridad) + enfasisFaltantes * ((1.1) + enfasisPrioridad) + electivasCBFaltantes * ((1.1) + cbPrioridad);
        //heuristica = creditosNucleoCBFaltantes * ( (1.0) + cbPrioridad) + creditosNucleoIngenieria * ( (1.0) + ingenieriaPrioridad) + creditosNucleoSociohumanisticas * ( (1.0) + sociohumanisticasPrioridad) + electivasFaltantes * ((1.0) + electivasPrioridad) + complementariasFaltantes * ((1.0) + complementariasPrioridad) + enfasisFaltantes * ((1.0) + enfasisPrioridad) + electivasCBFaltantes * ((1.0) + cbPrioridad);
        heuristica = creditosNucleoCBFaltantes * ( (2.0) + cbPrioridad) + creditosNucleoIngenieria * ( (2.0) + ingenieriaPrioridad) + creditosNucleoSociohumanisticas * ( (2.0) + sociohumanisticasPrioridad) + electivasFaltantes * ((2.0) + electivasPrioridad) + complementariasFaltantes * ((2.0) + complementariasPrioridad) + enfasisFaltantes * ((2.0) + enfasisPrioridad) + electivasCBFaltantes * ((2.0) + cbPrioridad);

        return heuristica;
    }

    // Método de compatibilidad para mantener las llamadas existentes
    public double calcularHeuristica(Progreso progreso, int semestreObjetivo, Proyeccion proyeccionBase,
            List<Materia> materiasPensum, boolean[] prioridades) {
        // Usar el semestre calculado del progreso como fallback
        return calcularHeuristica(progreso, semestreObjetivo, proyeccionBase, materiasPensum, prioridades, progreso.getSemestre());
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

    public int contarCreditosNucleoFaltantes(Progreso progreso) {
        int count = 0;
        for (Materia materia : progreso.getListaMateriasFaltantes()) {
            if (esMateriaNucleo(materia)) {
                count += materia.getCreditos();
            }
        }
        return count;
    }

    public int contarCreditosNucleoCBFaltantes(Progreso progreso) {
        int count = 0;
        for (Materia materia : progreso.getListaMateriasFaltantes()) {
            if (materia.getTipo().equals("nucleoCienciasBasicas")) {
                count += materia.getCreditos();
            }
        }
        return count;
    }

    public int contarCreditosNucleoIngeFaltantes(Progreso progreso) {
        int count = 0;
        for (Materia materia : progreso.getListaMateriasFaltantes()) {
            if (materia.getTipo().equals("nucleoIngenieria")) {
                count += materia.getCreditos();
            }
        }
        return count;
    }

    public int contarCreditosNucleoSocioFaltantes(Progreso progreso) {
        int count = 0;
        for (Materia materia : progreso.getListaMateriasFaltantes()) {
            if (materia.getTipo().equals("nucleoSociohumanisticas")) {
                count += materia.getCreditos();
            }
        }
        return count;
    }

    public int contarCreditosAtrasados(Progreso progreso, int semestreActual) {
        int count = 0;
        for (Materia materia : progreso.getListaMateriasFaltantes()) {
            if (materia.getSemestre() <= semestreActual) {
                count += materia.getCreditos();
            }
        }
        return count;
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

    public List<Materia> generarMateriasElectivas(Progreso progreso, List<Materia> materiasPensum, int semestre,
            int creditosDisponibles, int materiasDisponibles, Simulacion simulacionActual) {
        List<Materia> materiasGeneradas = new ArrayList<>();

        // Calcular cuántos créditos de cada tipo ya están en la simulación
        int enfasisYaUsado = calcularCreditosUsadosEnSimulacion(simulacionActual, "5");
        int complementariasYaUsado = calcularCreditosUsadosEnSimulacion(simulacionActual, "1");
        int electivasYaUsado = calcularCreditosUsadosEnSimulacion(simulacionActual, "0");
        int electivasCBYaUsado = calcularCreditosUsadosEnSimulacion(simulacionActual, "6");

        // Calcular créditos faltantes considerando lo ya añadido
        int electivas = Math.max(validarElectivas(progreso, materiasPensum, semestre) - electivasYaUsado, 0);
        int complementarias = Math
                .max(validarComplementarias(progreso, materiasPensum, semestre) - complementariasYaUsado, 0);
        int enfasis = Math.max(validarEnfasis(progreso, materiasPensum, semestre) - enfasisYaUsado, 0);
        int electivasCB = Math.max(validarElectivasCB(progreso, materiasPensum, semestre) - electivasCBYaUsado, 0);

        int creditosRestantes = creditosDisponibles;
        int materiasRestantes = materiasDisponibles;

        Materia enfasisMateria = verificarMateria(enfasis, creditosRestantes, materiasRestantes, "5", "Enfasis",
                semestre);
        if (enfasisMateria != null) {
            materiasGeneradas.add(enfasisMateria);
            creditosRestantes -= enfasisMateria.getCreditos();
            materiasRestantes--;
        }

        Materia complementaria = verificarMateria(complementarias, creditosRestantes, materiasRestantes, "1",
                "Complementarias", semestre);
        if (complementaria != null) {
            materiasGeneradas.add(complementaria);
            creditosRestantes -= complementaria.getCreditos();
            materiasRestantes--;
        }

        Materia electiva = verificarMateria(electivas, creditosRestantes, materiasRestantes, "0", "Electiva",
                semestre);
        if (electiva != null) {
            materiasGeneradas.add(electiva);
            creditosRestantes -= electiva.getCreditos();
            materiasRestantes--;
        }

        Materia electivaCB = verificarMateria(electivasCB, creditosRestantes, materiasRestantes, "6", "Electiva CB",
                semestre);
        if (electivaCB != null) {
            materiasGeneradas.add(electivaCB);
            creditosRestantes -= electivaCB.getCreditos();
            materiasRestantes--;
        }

        return materiasGeneradas;
    }

    // Calcular cuántos créditos de un tipo específico ya están en la simulación
    public int calcularCreditosUsadosEnSimulacion(Simulacion simulacion, String tipoCodigo) {
        int creditosUsados = 0;
        for (Materia materia : simulacion.getMaterias()) {
            if (materia.getCodigo().equals(tipoCodigo)) {
                creditosUsados += materia.getCreditos();
            }
        }
        return creditosUsados;
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

        int creditosDisponibles = proyeccion.getCreditos();
        int materiasDisponiblesNum = proyeccion.getMaterias();
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

        //Ordenar las combinaciones de mayor puntaje a menor
        todasLasCombinaciones = todasLasCombinaciones.stream()
                .sorted((a, b) -> Double.compare(b.getPuntajeTotal(), a.getPuntajeTotal()))
                .collect(Collectors.toSet());

        return todasLasCombinaciones.stream().limit(iteraciones).collect(Collectors.toSet());
    }

    // Backtracking para generar combinaciones de materias únicas
    public void backtrackCombinacionesUnicas(List<MateriaConPuntajeDTO> materias, Set<Materia> combinacionActual,
            Set<Simulacion> resultado, int indice, int creditosMax, int materiasMax, int creditosActuales,
            int materiasActuales) {

        // Si se alcanzan los límites se guarda la combinación actual
        if (indice == materias.size() || materiasActuales == materiasMax || creditosActuales >= creditosMax) {
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

        // Incluir la materia si es posible
        if (creditosActuales + materiaActual.getCreditos() <= creditosMax && materiasActuales + 1 <= materiasMax) {
            combinacionActual.add(materiaActual);
            backtrackCombinacionesUnicas(materias, combinacionActual, resultado, indice + 1, creditosMax, materiasMax,
                    creditosActuales + materiaActual.getCreditos(), materiasActuales + 1);
            combinacionActual.remove(materiaActual);
        }

        // No incluir la materia
        backtrackCombinacionesUnicas(materias, combinacionActual, resultado, indice + 1, creditosMax, materiasMax,
                creditosActuales, materiasActuales);
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

    public Set<Simulacion> generarCombinaciones(Progreso progreso, Proyeccion proyeccion, List<Materia> materiasPensum, int numCombinaciones, boolean[] prioridades) {
        
        List<Materia> materiasDisponibles = filtrarMateriasDisponibles(progreso, materiasPensum, proyeccion);
        List<MateriaConPuntajeDTO> materiasConPuntaje = calcularPuntajes(materiasDisponibles, progreso, proyeccion,prioridades);
        //mostrarMateriasPuntajes(materiasConPuntaje);
        Set<Simulacion> mejoresCombinacionesUnicas = generarMejoresCombinacionesUnicas(materiasConPuntaje, proyeccion.getCreditos(), proyeccion.getMaterias(), numCombinaciones);
        //mostrarResultadosCombinaciones(mejoresCombinacionesUnicas);

        return mejoresCombinacionesUnicas;
    }

    public double calcularPuntajeRuta(Map<Integer, Simulacion> ruta, Progreso progresoInicial,
            boolean[] prioridades) {
        double puntajeTotal = 0.0;
        Progreso progresoTemporal = progresoInicial.copy();

        List<Integer> semestresOrdenados = ruta.keySet().stream()
                .sorted()
                .collect(Collectors.toList());

        for (Integer semestre : semestresOrdenados) {
            Simulacion sim = ruta.get(semestre);

            Proyeccion proyTemp = crearProyeccionParaSemestre(new Proyeccion(), semestre);

            for (Materia materia : sim.getMaterias()) {
                puntajeTotal += calcularPuntajeMateria(materia, progresoTemporal, proyTemp, prioridades);
            }

            progresoTemporal = actualizarProgresoTemporal(progresoTemporal, sim, semestre);
        }

        return puntajeTotal;
    }

    public void mostrarResultados(Map<Integer, Simulacion> ruta, double puntajeTotal) {
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
        System.out.println("\nPuntaje total de la ruta: " + puntajeTotal);
    }
    
    // Versión sobrecargada que incluye análisis de pendientes
    public void mostrarResultados(Map<Integer, Simulacion> ruta, double puntajeTotal, Progreso progresoInicial) {
        // Mostrar los resultados normales
        mostrarResultados(ruta, puntajeTotal);
        
        // Agregar análisis de materias pendientes
        analizarMateriasPendientes(ruta, progresoInicial);
    }

    public Proyeccion crearProyeccionParaSemestre(Proyeccion base, int semestre) {
        Proyeccion proyeccion = new Proyeccion();
        proyeccion.setSemestre(semestre);
        proyeccion.setCreditos(base.getCreditos());
        proyeccion.setMaterias(base.getMaterias());
        return proyeccion;
    }

    //Métodos para test
    public Map<String, Object> generarSimulacionConEstadisticas(Progreso progreso, Proyeccion proyeccionBase,
            int semestreObjetivo, boolean[] prioridades, int limiteCombinaciones) throws Exception {

        // Guardar configuración original de salida
        PrintStream originalOut = System.out;

        List<Materia> materiasPensum = pensumService.obtenerPensum();
        
        try {
            // Redirigir salida para evitar spam en tests
            System.setOut(new PrintStream(new ByteArrayOutputStream()));
            
            // Resetear contadores
            contadorCombinaciones = 0;
            contadorNodosCreados = 0;
            
            long tiempoInicio = System.currentTimeMillis();
            
            // Usar la versión con límite personalizable
            Map<Integer, Simulacion> resultado = generarSimulacionMultiSemestreAStarConLimite(
                progreso, proyeccionBase, semestreObjetivo, materiasPensum, prioridades, limiteCombinaciones);
            
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
            int semestreObjetivo, List<Materia> materiasPensum, boolean[] prioridades, int limiteCombinaciones) {

        // Resetear contadores
        contadorCombinaciones = 0;
        contadorNodosCreados = 0;

        System.out.println("================ INICIO SIMULACIÓN A* (Límite: " + limiteCombinaciones + ") ================");
        System.out.println("Semestre actual: " + progreso.getSemestre());
        System.out.println("Semestre objetivo: " + semestreObjetivo);

        int maxNodos = 25000;
        
        PriorityQueue<NodoA> frontera = new PriorityQueue<>(Comparator.comparingDouble(NodoA::getCostoTotal));
        Set<String> visitados = new HashSet<>();

        Map<Integer, Simulacion> rutaInicial = new HashMap<>();
        double heuristicaInicial = calcularHeuristica(progreso, semestreObjetivo, proyeccionBase, materiasPensum, prioridades);

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
                double puntajeTotal = calcularPuntajeRuta(rutaCompleta, progreso, prioridades);
                mostrarResultados(rutaCompleta, puntajeTotal);
                return rutaCompleta;
            }

            if (nodoActual.getSemestreActual() == semestreObjetivo) {
                double heuristicaActual = calcularHeuristica(nodoActual.getProgresoActual(), semestreObjetivo, proyeccionBase, materiasPensum, prioridades);
                
                int nodosMinimosParaComparar = Math.min(50, maxNodos / 10); 
                
                if (nodosExplorados >= nodosMinimosParaComparar) {
                    boolean hayMejorOpcion = false;
                    for (NodoA nodoFrontera : frontera) {
                        double heuristicaFrontera = calcularHeuristica(nodoFrontera.getProgresoActual(), semestreObjetivo, proyeccionBase, materiasPensum, prioridades);
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
                        double puntajeTotal = calcularPuntajeRuta(rutaCompleta, progreso, prioridades);
                        mostrarResultados(rutaCompleta, puntajeTotal, progreso);
                        return rutaCompleta;
                    }
                }
            }

            String claveEstado = generarClaveEstado(nodoActual.getProgresoActual(), nodoActual.getSemestreActual());
            if (visitados.contains(claveEstado))
                continue;
            visitados.add(claveEstado);

            expandirNodo(nodoActual, frontera, semestreObjetivo, proyeccionBase, materiasPensum,
                    limiteCombinaciones, prioridades);
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
            System.out.println("🎉 ¡PROGRAMA ACADÉMICO COMPLETADO!");
        } else {
            System.out.println("⚠️  Aún faltan requisitos por cumplir");
            
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
            System.out.println("🎉 ¡PROGRAMA ACADÉMICO COMPLETADO!");
        } else {
            System.out.println("⚠️  Aún faltan requisitos por cumplir");
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

}
