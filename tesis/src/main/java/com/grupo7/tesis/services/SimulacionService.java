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
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.grupo7.tesis.dtos.MateriaConPuntajeDTO;
import com.grupo7.tesis.models.Materia;
import com.grupo7.tesis.models.NodoA;
import com.grupo7.tesis.models.Simulacion;
import com.grupo7.tesis.models.Progreso;
import com.grupo7.tesis.models.Proyeccion;
import com.grupo7.tesis.repositories.SimulacionRepository;

@Service
public class SimulacionService {

    @Autowired
    private SimulacionRepository simulacionRepository;

    private int contadorCombinaciones = 0;
    private int contadorNodosCreados = 0;

    @Autowired
    private PensumService pensumService;

    // ALGORITMO A*
    public Map<Integer, Simulacion> generarSimulacionMultiSemestreAStar(Progreso progreso, Proyeccion proyeccionBase,
            int semestreObjetivo, List<Materia> materiasPensum, boolean[] prioridades) {

        contadorCombinaciones = 0;
        contadorNodosCreados = 0;

        System.out.println("================ INICIO SIMULACIÓN A*  ================");
        System.out.println("Semestre actual: " + progreso.getSemestre());
        System.out.println("Semestre objetivo: " + semestreObjetivo);

        int maxNodos = 25000;
        int maxCombinacionesPorNodo = 3;

        PriorityQueue<NodoA> frontera = new PriorityQueue<>(Comparator.comparingDouble(NodoA::getCostoTotal));

        Set<String> visitados = new HashSet<>();

        Map<Integer, Simulacion> rutaInicial = new HashMap<>();
        double heuristicaInicial = calcularHeuristica(progreso, semestreObjetivo, proyeccionBase, materiasPensum);

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
                long tiempoTotal = System.currentTimeMillis() - tiempoInicio;
                System.out.println("SOLUCION A* ENCONTRADA");
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

        List<Simulacion> combinaciones = generarCombinaciones(nodoActual.getProgresoActual(), proyeccionSemestre,
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
            double nuevaHeuristica = calcularHeuristica(nuevoProgreso, semestreObjetivo, proyeccionBase,materiasPensum);
            
            //Funcion f(n)=h(n)+g(n)
            double costoTotal = nuevoCosto + nuevaHeuristica;

            NodoA nuevoNodo = new NodoA(nuevaRuta, siguienteSemestre, costoTotal, nuevoProgreso);
            contadorNodosCreados++; // Contar cada nuevo nodo creado
            frontera.offer(nuevoNodo);
        }
    }

    // Nueva versión de actualizar progreso temporal para que sirva con A*
    public Progreso actualizarProgresoTemporal(Progreso progreso, Simulacion simulacion, int semestreSimulado) {

        List<Materia> materiasARemover = new ArrayList<>();
        for (Materia materiaSimulada : simulacion.getMaterias()) {

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

        progreso.getListaMateriasFaltantes().removeAll(materiasARemover);

        for (Materia materia : simulacion.getMaterias()) {
            switch (materia.getCodigo()) {
                case "0":
                    int creditosElectiva = materia.getCreditos();
                    progreso.setFaltanElectiva(Math.max(0, progreso.getFaltanElectiva() - creditosElectiva));
                    break;
                case "1":
                    progreso.setFaltanComplementaria(Math.max(0, progreso.getFaltanComplementaria() - 3));
                    break;
                case "5":
                    progreso.setFaltanEnfasis(Math.max(0, progreso.getFaltanEnfasis() - 3));
                    break;
                case "6":
                    progreso.setFaltanElectivaBasicas(Math.max(0, progreso.getFaltanElectivaBasicas() - 3));
                    break;
            }
        }

        progreso.setMateriasCursadas(progreso.getMateriasCursadas() + materiasARemover.size());
        progreso.setMateriasFaltantes(progreso.getMateriasFaltantes() - materiasARemover.size());
        progreso.setTotalFaltantes(progreso.getListaMateriasFaltantes().size());

        int creditosNucleoSimulados = materiasARemover.stream().mapToInt(Materia::getCreditos).sum();
        progreso.setCreditosPensum(progreso.getCreditosPensum() + creditosNucleoSimulados);
        progreso.setTotalCreditos(progreso.getTotalCreditos() + creditosNucleoSimulados);

        progreso.setSemestre(semestreSimulado);

        return progreso;
    }

    // Heuristica
    public double calcularHeuristica(Progreso progreso, int semestreObjetivo, Proyeccion proyeccionBase,
            List<Materia> materiasPensum) {

        if (haCompletadoTodasLasMaterias(progreso)) {
            return 0.0;
        }

        double heuristica = 0.0;

        int materiasNucleoFaltantes = contarCreditosNucleoFaltantes(progreso);
        double electivasFaltantes = progreso.getFaltanElectiva();
        double complementariasFaltantes = progreso.getFaltanComplementaria();
        double enfasisFaltantes = progreso.getFaltanEnfasis();
        double electivasCBFaltantes = progreso.getFaltanElectivaBasicas();

        //heuristica = materiasNucleoFaltantes * (1.0) + electivasFaltantes * (0.6) + complementariasFaltantes * (0.8) + enfasisFaltantes * (0.8) + electivasCBFaltantes * (1.0);
        heuristica = materiasNucleoFaltantes * (1.2) + electivasFaltantes * (1.0) + complementariasFaltantes * (1.1) + enfasisFaltantes * (1.1) + electivasCBFaltantes * (1.2);

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
            coeficienteDistancia = 0.3; // Materia de semestres posteriores
        } else if (distanciaSemestral == 1) {
            coeficienteDistancia = 0.7; // Materia de un semestre adelante
        } else if (distanciaSemestral == 0) {
            coeficienteDistancia = 1.0; // Materia del semestre actual
        } else {
            coeficienteDistancia = 1.3; // Materia de semestres anteriores
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

    // Contar creditos de núcleo faltantes
    public int contarCreditosNucleoFaltantes(Progreso progreso) {
        int count = 0;
        for (Materia materia : progreso.getListaMateriasFaltantes()) {
            if (esMateriaNucleo(materia)) {
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
    public List<Simulacion> generarMejoresCombinaciones(List<MateriaConPuntajeDTO> materiasConPuntaje,
            int creditosMax,int materiasMax, int iteraciones) {
        List<Simulacion> todasLasCombinaciones = new ArrayList<>();
        List<Materia> combinacionActual = new ArrayList<>();

        backtrackCombinaciones(materiasConPuntaje, combinacionActual, todasLasCombinaciones, 0, creditosMax,
                materiasMax, 0, 0);

        // Ordenar por puntaje de mayor a menor
        todasLasCombinaciones.sort((a, b) -> Double.compare(b.getPuntajeTotal(), a.getPuntajeTotal()));

        return todasLasCombinaciones.stream().limit(iteraciones).collect(Collectors.toList());
        //return todasLasCombinaciones;
    }

    // Backtracking para generar combinaciones de materias
    public void backtrackCombinaciones(List<MateriaConPuntajeDTO> materias, List<Materia> combinacionActual,
            List<Simulacion> resultado, int indice, int creditosMax, int materiasMax, int creditosActuales,
            int materiasActuales) {

        // Si se alcanzan los límites se guarda la combinación actual
        if (indice == materias.size() || materiasActuales == materiasMax || creditosActuales >= creditosMax) {
            if (!combinacionActual.isEmpty()) {
                contadorCombinaciones++;
                double puntajeTotal = calcularPuntajeCombinacion(combinacionActual, materias);
                resultado.add(new Simulacion(combinacionActual, puntajeTotal));
            }
            return;
        }

        Materia materiaActual = materias.get(indice).getMateria();

        // Incluir la materia si es posible
        if (creditosActuales + materiaActual.getCreditos() <= creditosMax && materiasActuales + 1 <= materiasMax) {

            combinacionActual.add(materiaActual);
            backtrackCombinaciones(materias, combinacionActual, resultado, indice + 1, creditosMax, materiasMax,
                    creditosActuales + materiaActual.getCreditos(), materiasActuales + 1);
            combinacionActual.remove(combinacionActual.size() - 1);
        }

        // No incluir la materia
        backtrackCombinaciones(materias, combinacionActual, resultado, indice + 1, creditosMax, materiasMax,
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
            System.out.printf("%d. %s (%s) - %d créditos - Puntaje: %.1f%n",
                    i + 1,
                    mp.getMateria().getNombre(),
                    mp.getMateria().getCodigo(),
                    mp.getMateria().getCreditos(),
                    mp.getPuntaje());
        }
    }

    // Resultados
    public void mostrarResultadosCombinaciones(List<Simulacion> combinaciones) {
        System.out.println("\nMEJORES COMBINACIONES ENCONTRADAS");

        for (int i = 0; i < combinaciones.size(); i++) {
            Simulacion comb = combinaciones.get(i);
            System.out.printf("\n--- COMBINACIÓN %d (Puntaje: %.1f, Créditos: %d) ---%n",
                    i + 1, comb.getPuntajeTotal(), comb.getTotalCreditos());

            for (int j = 0; j < comb.getMaterias().size(); j++) {
                Materia materia = comb.getMaterias().get(j);
                System.out.printf("  %d. %s (%s) - %d créditos - Semestre %d%n",
                        j + 1,
                        materia.getNombre(),
                        materia.getCodigo(),
                        materia.getCreditos(),
                        materia.getSemestre());
            }
        }

        if (combinaciones.isEmpty()) {
            System.out.println("\nNo se encontraron combinaciones válidas");
        }
    }

    public List<Simulacion> generarCombinaciones(Progreso progreso, Proyeccion proyeccion,
            List<Materia> materiasPensum, int numCombinaciones, boolean[] prioridades) {
        List<Materia> materiasDisponibles = filtrarMateriasDisponibles(progreso, materiasPensum, proyeccion);
        List<MateriaConPuntajeDTO> materiasConPuntaje = calcularPuntajes(materiasDisponibles, progreso, proyeccion,
                prioridades);
        mostrarMateriasPuntajes(materiasConPuntaje);
        List<Simulacion> mejoresCombinaciones = generarMejoresCombinaciones(materiasConPuntaje,
                proyeccion.getNumMaxCreditos(), proyeccion.getNumMaxMaterias(), numCombinaciones).stream().limit(numCombinaciones)
                .collect(Collectors.toList());
        mostrarResultadosCombinaciones(mejoresCombinaciones);

        return mejoresCombinaciones;
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

            for (int i = 0; i < sim.getMaterias().size(); i++) {
                Materia materia = sim.getMaterias().get(i);
                System.out.printf("  %d. %s (%s) - %d créditos%n",
                        i + 1, materia.getNombre(), materia.getCodigo(), materia.getCreditos());
            }
        }
        System.out.println("\nPuntaje total de la ruta: " + puntajeTotal);
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
        double heuristicaInicial = calcularHeuristica(progreso, semestreObjetivo, proyeccionBase, materiasPensum);

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
                long tiempoTotal = System.currentTimeMillis() - tiempoInicio;
                System.out.println("SOLUCION A* ENCONTRADA");
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
