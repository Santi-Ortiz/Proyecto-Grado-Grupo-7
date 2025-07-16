package com.grupo7.tesis.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.grupo7.tesis.model.MateriaJson;
import com.grupo7.tesis.model.Progreso;
import com.grupo7.tesis.model.Proyeccion;
import com.grupo7.tesis.model.Simulacion;
import com.grupo7.tesis.model.MateriaConPuntaje;
import com.grupo7.tesis.model.Combinacion;

@Service
public class SimulacionService {

    public Simulacion generarSimulacion(Progreso progreso, Proyeccion proyeccion, List<MateriaJson> materiasPensum) {

        Simulacion simulacion = new Simulacion();
        int contadorCreditosSimulacion = 0; // Se aumentará a medida que se agreguen créditos a la simulación
        int contadorMateriasSimulacion = 0; // Se aumentará a medida que se agreguen materias a la simulación
        int creditos = proyeccion.getCreditos();
        int materias = proyeccion.getMaterias();

        for (MateriaJson p : progreso.getListaMateriasFaltantes()) {
            if (p.getSemestre() <= proyeccion.getSemestre() &&
                    contadorCreditosSimulacion + p.getCreditos() <= proyeccion.getCreditos() &&
                    contadorMateriasSimulacion + 1 <= proyeccion.getMaterias()) {

                if (validarPrerequisito(progreso, materiasPensum, p.getRequisitos())) {
                    // Se añade una materia en la simulación priorizando las materias de semestres
                    // anteriores al actual no vistas
                    simulacion.agregarMateria(p);
                    contadorCreditosSimulacion += p.getCreditos();
                    contadorMateriasSimulacion++;
                }
            }
        }

        // Revisar enfasis, complementarias y electivas de semestres pasados y actual
        List<MateriaJson> electivasActuales = generarMateriasElectivas(progreso, materiasPensum,
                proyeccion.getSemestre(), creditos - contadorCreditosSimulacion, materias - contadorMateriasSimulacion,
                simulacion);

        boolean yaEstaEnSimulacion = false;

        for (MateriaJson materia : electivasActuales) {
            simulacion.agregarMateria(materia);
            contadorCreditosSimulacion += materia.getCreditos();
            contadorMateriasSimulacion++;
        }

        // Revisa materias de semestres superiores que no se han cursado
        if (creditos - contadorCreditosSimulacion > 0 && materias - contadorMateriasSimulacion > 0) {
            for (MateriaJson p : progreso.getListaMateriasFaltantes()) {
                if (p.getSemestre() <= proyeccion.getSemestre() + 1 &&
                        contadorCreditosSimulacion + p.getCreditos() <= creditos &&
                        contadorMateriasSimulacion + 1 <= materias) {
                    if (validarPrerequisito(progreso, materiasPensum, p.getRequisitos())) {
                        yaEstaEnSimulacion = false;
                        for (MateriaJson materiaEnSimulacion : simulacion.getMaterias()) {
                            if (materiaEnSimulacion.getCodigo().equals(p.getCodigo())) {
                                yaEstaEnSimulacion = true;
                                break;
                            }
                        }

                        if (!yaEstaEnSimulacion) {
                            simulacion.agregarMateria(p);
                            contadorCreditosSimulacion += p.getCreditos();
                            contadorMateriasSimulacion++;
                            if (creditos - contadorCreditosSimulacion <= 0
                                    || materias - contadorMateriasSimulacion <= 0) {
                                break;
                            }
                        }
                    }
                }
            }
        }

        // Revisar electivas, complementarias y énfasis de semestres superiores, en caso
        // de que no se hayan alcanzado los créditos o materias
        if (creditos - contadorCreditosSimulacion > 0 && materias - contadorMateriasSimulacion > 0) {
            List<MateriaJson> electivasSuperiores = generarMateriasElectivas(progreso, materiasPensum,
                    proyeccion.getSemestre() + 1, creditos - contadorCreditosSimulacion,
                    materias - contadorMateriasSimulacion, simulacion);

            for (MateriaJson materia : electivasSuperiores) {
                simulacion.agregarMateria(materia);
                contadorCreditosSimulacion += materia.getCreditos();
                contadorMateriasSimulacion++;
            }
        }

        return simulacion;
    }

    // Esto valida si una materia puede ser añadida en la simulación a partir de los
    // prerequisitos
    public Boolean validarPrerequisito(Progreso progreso, List<MateriaJson> materiasPensum,
            List<String> prerequisitos) {

        if (prerequisitos == null || prerequisitos.isEmpty())
            return true; // Si no hay prerequisitos, se puede añadir la materia

        for (String pr : prerequisitos) {
            for (MateriaJson materia : progreso.getListaMateriasFaltantes()) { // Aca si añadir la lista de materias
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
    public int validarElectivas(Progreso progreso, List<MateriaJson> materiasPensum, int semestre) {
        int numCreditosElectivas = 0;
        int numCreditosElectivasVistas = Math.max(8 - progreso.getFaltanElectiva(), 0);

        for (MateriaJson materia : materiasPensum) {
            if (materia.getCodigo().equals("0") &&
                    materia.getSemestre() <= semestre) {
                numCreditosElectivas += materia.getCreditos();
            }
        }

        return Math.max(numCreditosElectivas - numCreditosElectivasVistas, 0);
    }

    // Devuelve la cantidad de créditos que faltan por cursar en complementarias
    // para ese semestre
    public int validarComplementarias(Progreso progreso, List<MateriaJson> materiasPensum, int semestre) {
        int numCreditosComplementarias = 0;
        int numCreditosComplementariasVistas = Math.max(6 - progreso.getFaltanComplementaria(), 0);

        for (MateriaJson materia : materiasPensum) {
            if (materia.getCodigo().equals("1") &&
                    materia.getSemestre() <= semestre) {
                numCreditosComplementarias += materia.getCreditos();
            }
        }

        return Math.max(numCreditosComplementarias - numCreditosComplementariasVistas, 0);
    }

    // Devuelve la cantidad de créditos que faltan por cursar en énfasis para ese
    // semestre
    public int validarEnfasis(Progreso progreso, List<MateriaJson> materiasPensum, int semestre) {
        int numCreditosEnfasis = 0;
        int numCreditosEnfasisVistas = Math.max(6 - progreso.getFaltanEnfasis(), 0);

        for (MateriaJson materia : materiasPensum) {
            if (materia.getCodigo().equals("5") &&
                    materia.getSemestre() <= semestre) {
                numCreditosEnfasis += materia.getCreditos();
            }
        }

        return Math.max(numCreditosEnfasis - numCreditosEnfasisVistas, 0);
    }

    // Devuelve la cantidad de créditos que faltan por cursar en electivas de
    // cs.básicas para ese semestre
    public int validarElectivasCB(Progreso progreso, List<MateriaJson> materiasPensum, int semestre) {
        int numCreditosElectivasCB = 0;
        int numCreditosElectivasCBVistas = 3 - progreso.getFaltanElectivaBasicas();

        for (MateriaJson materia : materiasPensum) {
            if (materia.getCodigo().equals("6") &&
                    materia.getSemestre() <= semestre) {
                numCreditosElectivasCB += materia.getCreditos();
            }
        }

        return Math.max(numCreditosElectivasCB - numCreditosElectivasCBVistas, 0);
    }

    public MateriaJson verificarMateria(int creditosRestantes, int creditosRestantesGeneral,
            int materiasRestantesGeneral, String codigo, String nombre, int semestre) {
        if (creditosRestantes > 0) {

            if (creditosRestantesGeneral > 0 && materiasRestantesGeneral > 0) {
                MateriaJson materiaSugerida = new MateriaJson();
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

                return materiaSugerida;
            }
        }
        return null;
    }

    public List<MateriaJson> generarMateriasElectivas(Progreso progreso, List<MateriaJson> materiasPensum, int semestre,
            int creditosDisponibles, int materiasDisponibles, Simulacion simulacionActual) {
        List<MateriaJson> materiasGeneradas = new ArrayList<>();

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

        MateriaJson enfasisMateria = verificarMateria(enfasis, creditosRestantes, materiasRestantes, "5", "Enfasis",
                semestre);
        if (enfasisMateria != null) {
            materiasGeneradas.add(enfasisMateria);
            creditosRestantes -= enfasisMateria.getCreditos();
            materiasRestantes--;
        }

        MateriaJson complementaria = verificarMateria(complementarias, creditosRestantes, materiasRestantes, "1",
                "Complementarias", semestre);
        if (complementaria != null) {
            materiasGeneradas.add(complementaria);
            creditosRestantes -= complementaria.getCreditos();
            materiasRestantes--;
        }

        MateriaJson electiva = verificarMateria(electivas, creditosRestantes, materiasRestantes, "0", "Electiva",
                semestre);
        if (electiva != null) {
            materiasGeneradas.add(electiva);
            creditosRestantes -= electiva.getCreditos();
            materiasRestantes--;
        }

        MateriaJson electivaCB = verificarMateria(electivasCB, creditosRestantes, materiasRestantes, "6", "Electiva CB",
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
        for (MateriaJson materia : simulacion.getMaterias()) {
            if (materia.getCodigo().equals(tipoCodigo)) {
                creditosUsados += materia.getCreditos();
            }
        }
        return creditosUsados;
    }

    // -------Combinaciones (De aquí en adelante es el código de
    // combinaciones)-------

    public Simulacion generarSimulacionCombinatorias(Progreso progreso, Proyeccion proyeccion,
            List<MateriaJson> materiasPensum) {
        // System.out.println("------------------ INICIO DE SIMULACIÓN CON COMBINATORIAS
        // ------------------");
        System.out.println("Créditos disponibles: " + proyeccion.getCreditos());
        System.out.println("Materias máximas: " + proyeccion.getMaterias());
        System.out.println("Semestre a proyectar: " + proyeccion.getSemestre());

        // Filtrar materias disponibles
        List<MateriaJson> materiasDisponibles = filtrarMateriasDisponibles(progreso, materiasPensum, proyeccion);
        System.out.println("\nMaterias disponibles encontradas: " + materiasDisponibles.size());

        // Calcular puntajes para cada materia
        List<MateriaConPuntaje> materiasConPuntaje = calcularPuntajes(materiasDisponibles, progreso, proyeccion);

        // Mostrar materias con sus puntajes
        mostrarMateriasPuntajes(materiasConPuntaje);

        // Combinaciones
        List<Combinacion> mejoresCombinaciones = generarMejoresCombinaciones(materiasConPuntaje,
                proyeccion.getCreditos(), proyeccion.getMaterias());

        // Resultados
        mostrarResultadosCombinaciones(mejoresCombinaciones);

        // Retornar
        if (!mejoresCombinaciones.isEmpty()) {
            Simulacion mejorSimulacion = new Simulacion();
            for (MateriaJson materia : mejoresCombinaciones.get(0).getMaterias()) {
                mejorSimulacion.agregarMateria(materia);
            }
            return mejorSimulacion;
        }

        return new Simulacion();
    }

    // Buscar materias faltantes y que pueda cursar
    public List<MateriaJson> filtrarMateriasDisponibles(Progreso progreso, List<MateriaJson> materiasPensum,
            Proyeccion proyeccion) {
        List<MateriaJson> materiasDisponibles = new ArrayList<>();

        // Agregar materias nucleo faltantes
        for (MateriaJson materia : progreso.getListaMateriasFaltantes()) {
            if (materia.getSemestre() <= proyeccion.getSemestre() + 1 &&
                    validarPrerequisito(progreso, materiasPensum, materia.getRequisitos())) {
                materiasDisponibles.add(materia);
            }
        }

        agregarMateriasAdicionalesDisponibles(materiasDisponibles, progreso, materiasPensum, proyeccion);

        return materiasDisponibles;
    }

    // Agrega electivas, complementarias y de énfasis a las materias disponibles
    public void agregarMateriasAdicionalesDisponibles(List<MateriaJson> materiasDisponibles, Progreso progreso, List<MateriaJson> materiasPensum, Proyeccion proyeccion) {

        int creditosDisponibles = proyeccion.getCreditos();
        int materiasDisponiblesNum = proyeccion.getMaterias();
        int semestre = proyeccion.getSemestre();

        // ELECTIVAS
        int usadosElectivas = 0;
        usadosElectivas += agregarElectivasPorSemestre(materiasDisponibles, validarElectivas(progreso, materiasPensum, semestre - 1), creditosDisponibles, materiasDisponiblesNum, semestre - 1, "Electiva Atrasada");
        usadosElectivas += agregarElectivasPorSemestre(materiasDisponibles, validarElectivas(progreso, materiasPensum, semestre) - usadosElectivas, creditosDisponibles, materiasDisponiblesNum, semestre, "Electiva Actual");
        agregarElectivasPorSemestre(materiasDisponibles, validarElectivas(progreso, materiasPensum, semestre + 1) - usadosElectivas, creditosDisponibles, materiasDisponiblesNum, semestre + 1, "Electiva Futura");

        // COMPLEMENTARIAS
        int usadosComp = 0;
        usadosComp += agregarMateriasGenericas(materiasDisponibles, validarComplementarias(progreso, materiasPensum, semestre - 1), creditosDisponibles, materiasDisponiblesNum, "1", "Complementaria Atrasada", semestre - 1);
        usadosComp += agregarMateriasGenericas(materiasDisponibles, validarComplementarias(progreso, materiasPensum, semestre) - usadosComp, creditosDisponibles, materiasDisponiblesNum, "1", "Complementaria Actual", semestre);
        agregarMateriasGenericas(materiasDisponibles, validarComplementarias(progreso, materiasPensum, semestre + 1) - usadosComp, creditosDisponibles, materiasDisponiblesNum, "1", "Complementaria Futura", semestre + 1);

        // ÉNFASIS
        int usadosEnf = 0;
        usadosEnf += agregarMateriasGenericas(materiasDisponibles, validarEnfasis(progreso, materiasPensum, semestre - 1), creditosDisponibles, materiasDisponiblesNum, "5", "Énfasis Atrasado", semestre - 1);
        usadosEnf += agregarMateriasGenericas(materiasDisponibles, validarEnfasis(progreso, materiasPensum, semestre) - usadosEnf, creditosDisponibles, materiasDisponiblesNum, "5", "Énfasis Actual", semestre);
        agregarMateriasGenericas(materiasDisponibles, validarEnfasis(progreso, materiasPensum, semestre + 1) - usadosEnf, creditosDisponibles, materiasDisponiblesNum, "5", "Énfasis Futuro", semestre + 1);

        // ELECTIVAS CIENCIAS BÁSICAS
        int usadosCB = 0;
        usadosCB += agregarMateriasGenericas(materiasDisponibles, validarElectivasCB(progreso, materiasPensum, semestre - 1), creditosDisponibles, materiasDisponiblesNum, "6", "Electiva CB Atrasada", semestre - 1);
        usadosCB += agregarMateriasGenericas(materiasDisponibles, validarElectivasCB(progreso, materiasPensum, semestre) - usadosCB, creditosDisponibles, materiasDisponiblesNum, "6", "Electiva CB Actual", semestre);
        agregarMateriasGenericas(materiasDisponibles, validarElectivasCB(progreso, materiasPensum, semestre + 1) - usadosCB, creditosDisponibles, materiasDisponiblesNum, "6", "Electiva CB Futura", semestre + 1);
    }

    private int agregarElectivasPorSemestre(List<MateriaJson> materiasDisponibles, int creditosRequeridos, int creditosDisponibles, int materiasDisponiblesNum, int semestre, String descripcionBase) {

        int usados = 0;
        for (int credito = 3; credito >= 1; credito--) {
            while (creditosRequeridos >= credito) {
                MateriaJson electiva = verificarMateria(
                    credito, creditosDisponibles, materiasDisponiblesNum, "0",
                    descripcionBase + " " + credito + "C", semestre
                );
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

    private int agregarMateriasGenericas(List<MateriaJson> materiasDisponibles, int creditosRequeridos, int creditosDisponibles, int materiasDisponiblesNum, String codigo, String descripcion, int semestre) {

        int usados = 0;
        MateriaJson m1 = verificarMateria(creditosRequeridos, creditosDisponibles, materiasDisponiblesNum, codigo, descripcion, semestre);
        if (m1 != null) {
            materiasDisponibles.add(m1);
            usados += m1.getCreditos();
        }

        if (creditosRequeridos >= 6) {
            MateriaJson m2 = verificarMateria(3, creditosDisponibles, materiasDisponiblesNum, codigo, descripcion + " 2", semestre);
            if (m2 != null) {
                materiasDisponibles.add(m2);
                usados += m2.getCreditos();
            }
        }

        return usados;
    }

    // Darle valor a las materias
    public List<MateriaConPuntaje> calcularPuntajes(List<MateriaJson> materias, Progreso progreso,
            Proyeccion proyeccion) {
        List<MateriaConPuntaje> materiasConPuntaje = new ArrayList<>();

        for (MateriaJson materia : materias) {
            double puntaje = calcularPuntajeMateria(materia, progreso, proyeccion);
            materiasConPuntaje.add(new MateriaConPuntaje(materia, puntaje));
        }

        // Ordenar de mayor a menor para dar prioridad
        materiasConPuntaje.sort((a, b) -> Double.compare(b.getPuntaje(), a.getPuntaje()));

        return materiasConPuntaje;
    }

    // Se le asigna un puntaje(la heurística) de cada materia dependiendo de su
    // prioridad
    public double calcularPuntajeMateria(MateriaJson materia, Progreso progreso, Proyeccion proyeccion) {
        double puntaje = 0;
        String codigo = materia.getCodigo();

        // Para un futuro estos valores se ajustarán a las preferencias del estudiante
        // para que logre elegir qué materias desea priorizar

        // Materias de semestres ANTERIORES que no ha visto
        if (materia.getSemestre() < proyeccion.getSemestre()) {
            switch (codigo) {
                case "0": // Materia electiva atrasada
                    puntaje += 120;
                    break;
                case "1": // Materia complementaria atrasada
                    puntaje += 150;
                    break;
                case "5": // Materia énfasis atrasada
                    puntaje += 150;
                    break;
                case "6": // Materia electivaCB atrasada
                    puntaje += 120;
                    break;
                default: // Materia núcleo atrasada
                    puntaje += 200;
                    break;
            }
        } else if (materia.getSemestre() == proyeccion.getSemestre()) {
            // Materias del semestre ACTUAL que no ha visto
            switch (codigo) {
                case "0": // Materia electiva
                    puntaje += 100;
                    break;
                case "1": // Materia complementaria
                    puntaje += 130;
                    break;
                case "5": // Materia énfasis
                    puntaje += 130;
                    break;
                case "6": // Materia electivaCB
                    puntaje += 100;
                    break;
                default: // Materia núcleo
                    puntaje += 180;
                    break;
            }

        } else if (materia.getSemestre() == proyeccion.getSemestre() + 1) {
            // Materias del SIGUIENTE semestre que no ha visto
            switch (codigo) {
                case "0": // Materia electiva
                    puntaje += 80;
                    break;
                case "1": // Materia complementaria
                    puntaje += 110;
                    break;
                case "5": // Materia énfasis
                    puntaje += 110;
                    break;
                case "6": // Materia electivaCB
                    puntaje += 80;
                    break;
                default: // Materia núcleo
                    puntaje += 160;
                    break;
            }
        } else if (materia.getSemestre() > proyeccion.getSemestre() + 1) {
            // Materias de semestres FUTUROS que no ha visto
            switch (codigo) {
                case "0": // Materia electiva
                    puntaje += 30;
                    break;
                case "1": // Materia complementaria
                    puntaje += 50;
                    break;
                case "5": // Materia énfasis
                    puntaje += 50;
                    break;
                case "6": // Materia electivaCB
                    puntaje += 30;
                    break;
                default: // Materia núcleo
                    puntaje += 70;
                    break;
            }
        }

        return Math.max(puntaje, 1);
    }

    // Combinaciones
    public List<Combinacion> generarMejoresCombinaciones(List<MateriaConPuntaje> materiasConPuntaje, int creditosMax,
            int materiasMax) {
        List<Combinacion> todasLasCombinaciones = new ArrayList<>();
        List<MateriaJson> combinacionActual = new ArrayList<>();

        backtrackCombinaciones(materiasConPuntaje, combinacionActual, todasLasCombinaciones, 0, creditosMax,
                materiasMax, 0, 0);

        // Ordenar por puntaje de mayor a menor
        todasLasCombinaciones.sort((a, b) -> Double.compare(b.getPuntajeTotal(), a.getPuntajeTotal()));

        return todasLasCombinaciones.stream().limit(5).collect(Collectors.toList());
    }

    // Backtracking para generar combinaciones de materias
    public void backtrackCombinaciones(List<MateriaConPuntaje> materias, List<MateriaJson> combinacionActual,
            List<Combinacion> resultado, int indice, int creditosMax, int materiasMax, int creditosActuales,
            int materiasActuales) {

        // Si se alcanzan los límites se guarda la combinación actual
        if (indice == materias.size() || materiasActuales == materiasMax || creditosActuales >= creditosMax) {
            if (!combinacionActual.isEmpty()) {
                double puntajeTotal = calcularPuntajeCombinacion(combinacionActual, materias);
                resultado.add(new Combinacion(combinacionActual, puntajeTotal, creditosActuales));
            }
            return;
        }

        MateriaJson materiaActual = materias.get(indice).getMateria();

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
    public double calcularPuntajeCombinacion(List<MateriaJson> combinacion,
            List<MateriaConPuntaje> materiasConPuntaje) {
        double puntajeTotal = 0;

        for (MateriaJson materia : combinacion) {
            for (MateriaConPuntaje mp : materiasConPuntaje) {
                if (mp.getMateria().getCodigo().equals(materia.getCodigo())) {
                    puntajeTotal += mp.getPuntaje();
                    break;
                }
            }
        }

        return puntajeTotal;
    }

    // Materias con puntajes
    public void mostrarMateriasPuntajes(List<MateriaConPuntaje> materiasConPuntaje) {
        System.out.println("\nMATERIAS DISPONIBLES CON PUNTAJES");
        for (int i = 0; i < materiasConPuntaje.size(); i++) {
            MateriaConPuntaje mp = materiasConPuntaje.get(i);
            System.out.printf("%d. %s (%s) - %d créditos - Puntaje: %.1f%n",
                    i + 1,
                    mp.getMateria().getNombre(),
                    mp.getMateria().getCodigo(),
                    mp.getMateria().getCreditos(),
                    mp.getPuntaje());
        }
    }

    // Resultados
    public void mostrarResultadosCombinaciones(List<Combinacion> combinaciones) {
        System.out.println("\nMEJORES COMBINACIONES ENCONTRADAS");

        for (int i = 0; i < combinaciones.size(); i++) {
            Combinacion comb = combinaciones.get(i);
            System.out.printf("\n--- COMBINACIÓN %d (Puntaje: %.1f, Créditos: %d) ---%n",
                    i + 1, comb.getPuntajeTotal(), comb.getCreditosTotales());

            for (int j = 0; j < comb.getMaterias().size(); j++) {
                MateriaJson materia = comb.getMaterias().get(j);
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

    /*
     * ---------- Acá va el código para simulaciones de más de un semestre
     */

    public Map<Integer, Simulacion> generarSimulacionMultiSemestre(
            Progreso progreso,
            Proyeccion proyeccionBase,
            int semestreObjetivo,
            List<MateriaJson> materiasPensum) {

        System.out.println("================ INICIO SIMULACIÓN MULTI SEMESTRE ================");
        System.out.println("Semestre actual: " + progreso.getSemestre());
        System.out.println("Semestre objetivo: " + semestreObjetivo);
        System.out.println("Créditos por semestre: " + proyeccionBase.getCreditos());
        System.out.println("Materias por semestre: " + proyeccionBase.getMaterias());

        Map<Integer, Simulacion> simulacionesPorSemestre = new HashMap<>();

        Progreso progresoTemporal = progreso.copy();

        // Se itera desde el semestre actual hasta el semestre objetivo
        for (int i = progreso.getSemestre() + 1; i <= semestreObjetivo; i++) {
            System.out.println("\n--- SIMULACIÓN SEMESTRE " + i + " ---");

            // Se crea una proyección para este semestre específico usando los parámetros
            // iniciales
            Proyeccion proyeccionSemestre = new Proyeccion();
            proyeccionSemestre.setSemestre(i);
            proyeccionSemestre.setCreditos(proyeccionBase.getCreditos());
            proyeccionSemestre.setMaterias(proyeccionBase.getMaterias());

            // Se genera la simulación para este semestre usando el progreso temporal
            // actualizado
            Simulacion simulacionSemestre = generarSimulacionCombinatorias(
                    progresoTemporal, proyeccionSemestre, materiasPensum);

            // Se agrega la simulación al mapa de las simulaciones
            simulacionesPorSemestre.put(i, simulacionSemestre);

            // Se actualiza el progreso temporal, es decir, las materias simuladas ahora se
            // consideran "vistas"
            progresoTemporal = actualizarProgresoTemporal(progresoTemporal, simulacionSemestre, i);

            System.out.println("Materias simuladas para semestre " + i + ": " +
                    simulacionSemestre.getMaterias().size());
            System.out.println("Créditos simulados: " + simulacionSemestre.getMaterias()
                    .stream().mapToInt(MateriaJson::getCreditos).sum());
        }

        return simulacionesPorSemestre;
    }

    /**
     * Actualiza el progreso temporal eliminando las materias que fueron simuladas
     * y actualizando los contadores
     */
    private Progreso actualizarProgresoTemporal(Progreso progreso, Simulacion simulacion, int semestreSimulado) {
        System.out.println("Se actualizó el progreso temporal para semestre " + semestreSimulado);

        // Se eliminan las materias de núcleo que fueron simuladas (códigos que NO son
        // 0, 1, 5, 6)
        List<MateriaJson> materiasARemover = new ArrayList<>();
        for (MateriaJson materiaSimulada : simulacion.getMaterias()) {
            // Solo elimina las materias de núcleo
            if (!materiaSimulada.getCodigo().equals("0") &&
                    !materiaSimulada.getCodigo().equals("1") &&
                    !materiaSimulada.getCodigo().equals("5") &&
                    !materiaSimulada.getCodigo().equals("6")) {

                for (MateriaJson materiaFaltante : progreso.getListaMateriasFaltantes()) {
                    if (materiaFaltante.getCodigo().equals(materiaSimulada.getCodigo()) ||
                            (materiaFaltante.getNombre().equals(materiaSimulada.getNombre()) &&
                                    materiaFaltante.getSemestre() == materiaSimulada.getSemestre())) {
                        materiasARemover.add(materiaFaltante);
                        break;
                    }
                }
            }
        }

        // Eliminar el resto de las materias
        progreso.getListaMateriasFaltantes().removeAll(materiasARemover);

        // Actualiza los contadores de materias específicas
        for (MateriaJson materia : simulacion.getMaterias()) {
            switch (materia.getCodigo()) {
                case "0": // Electivas
                    int creditosElectiva = materia.getCreditos();
                    progreso.setFaltanElectiva(Math.max(0, progreso.getFaltanElectiva() - creditosElectiva));
                    break;
                case "1": // Complementarias
                    progreso.setFaltanComplementaria(Math.max(0, progreso.getFaltanComplementaria() - 3));
                    break;
                case "5": // Énfasis
                    progreso.setFaltanEnfasis(Math.max(0, progreso.getFaltanEnfasis() - 3));
                    break;
                case "6": // Electivas Ciencias Básicas
                    progreso.setFaltanElectivaBasicas(Math.max(0, progreso.getFaltanElectivaBasicas() - 3));
                    break;
            }
        }

        // Actualiza los contadores generales de las materias faltantes en el progreso
        progreso.setMateriasCursadas(progreso.getMateriasCursadas() + materiasARemover.size());
        progreso.setMateriasFaltantes(progreso.getMateriasFaltantes() - materiasARemover.size());
        progreso.setTotalFaltantes(progreso.getListaMateriasFaltantes().size());

        // Actualiza créditos de las materias de núcleo eliminadas
        int creditosNucleoSimulados = 0;
        for (MateriaJson materia : materiasARemover) {
            creditosNucleoSimulados += materia.getCreditos();
        }
        progreso.setCreditosPensum(progreso.getCreditosPensum() + creditosNucleoSimulados);
        progreso.setTotalCreditos(progreso.getTotalCreditos() + creditosNucleoSimulados);

        // Avanzar en la iteración de los semestres
        progreso.setSemestre(semestreSimulado);

        System.out.println("Materias núcleo agregadas al progreso temporal: " + materiasARemover.size());
        System.out.println("Créditos núcleo agregados al progreso temporal: " + creditosNucleoSimulados);
        System.out.println("Nuevo semestre: " + progreso.getSemestre());

        return progreso;
    }
}
