package com.grupo7.tesis.service;

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

import org.springframework.stereotype.Service;

import com.grupo7.tesis.model.MateriaJson;
import com.grupo7.tesis.model.Progreso;
import com.grupo7.tesis.model.Proyeccion;
import com.grupo7.tesis.model.Simulacion;
import com.grupo7.tesis.model.MateriaConPuntaje;
import com.grupo7.tesis.model.Combinacion;
import com.grupo7.tesis.model.NodoA;

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
    public void agregarMateriasAdicionalesDisponibles(List<MateriaJson> materiasDisponibles, Progreso progreso,
            List<MateriaJson> materiasPensum, Proyeccion proyeccion) {

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

    public int agregarElectivasPorSemestre(List<MateriaJson> materiasDisponibles, int creditosRequeridos, int creditosDisponibles, int materiasDisponiblesNum, int semestre, String descripcionBase) {

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

    public int agregarMateriasGenericas(List<MateriaJson> materiasDisponibles, int creditosRequeridos, int creditosDisponibles, int materiasDisponiblesNum, String codigo, String descripcion, int semestre) {

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
        int distanciaSemestral = 0;
        double coeficienteMateria = 0;
        double coeficienteDistancia = 0;
        String codigo = materia.getCodigo();

        int semestresRestantes = Math.max(1, Math.abs(proyeccion.getSemestre() - progreso.getSemestre()));
        double factorPeso;

        if (semestresRestantes <= 2) {
            factorPeso = 1.0; 
        } else if (semestresRestantes <= 4) {
            factorPeso = 0.5;
        } else {
            factorPeso = 0.3;
        }

        switch (codigo) {
            case "0": // Electiva
                coeficienteMateria = 80 * factorPeso;
                break;
            case "1": // Complementaria
                coeficienteMateria = 100 * factorPeso;
                break;
            case "5": // Énfasis
                coeficienteMateria = 100 * factorPeso;
                break;
            case "6": // ElectivaCB
                coeficienteMateria = 90 * factorPeso;
                break;
            default: // Núcleo
                coeficienteMateria = 180 * factorPeso;
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
            coeficienteDistancia = 1.4; // Materia de semestres anteriores
        }

        puntaje = coeficienteMateria * coeficienteDistancia;

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
 
    // COMBINACIONES TENIENDO EN CUENTA TODOS LOS CASOS POSIBLES
    public Map<Integer, Simulacion> generarSimulacionMultiSemestreOptimizada(Progreso progreso,Proyeccion proyeccionBase,int semestreObjetivo,List<MateriaJson> materiasPensum) {
        System.out.println("================ INICIO SIMULACION MULTI SEMESTRE ================");
        System.out.println("Semestre actual: " + progreso.getSemestre());
        System.out.println("Semestre objetivo: " + semestreObjetivo);
        System.out.println("Créditos por semestre: " + proyeccionBase.getCreditos());
        System.out.println("Materias por semestre: " + proyeccionBase.getMaterias());

        List<Map<Integer, Simulacion>> rutasActuales = new ArrayList<>();
        
        Map<Integer, Simulacion> rutaInicial = new HashMap<>();
        rutasActuales.add(rutaInicial);
        
        for (int semestre = progreso.getSemestre() + 1; semestre <= semestreObjetivo; semestre++) {
            System.out.println("\n--- EXPANDIENDO RUTAS PARA SEMESTRE " + semestre + " ---");
            
            List<Map<Integer, Simulacion>> nuevasRutas = new ArrayList<>();
            
            for (Map<Integer, Simulacion> rutaActual : rutasActuales) {
                Progreso progresoTemporal = calcularProgreso(progreso, rutaActual, semestre - 1);
                
                Proyeccion proyeccionSemestre = crearProyeccionParaSemestre(proyeccionBase, semestre);
                
                List<Combinacion> combinaciones = generarCombinaciones(progresoTemporal, proyeccionSemestre, materiasPensum, 3);
                
                // Crear una nueva ruta por cada combinación
                for (Combinacion comb : combinaciones) {
                    Map<Integer, Simulacion> nuevaRuta = new HashMap<>(rutaActual);
                    
                    Simulacion simulacionSemestre = new Simulacion();
                    for (MateriaJson materia : comb.getMaterias()) {
                        simulacionSemestre.agregarMateria(materia);
                    }
                    
                    nuevaRuta.put(semestre, simulacionSemestre);
                    nuevasRutas.add(nuevaRuta);
                }
            }
            
            rutasActuales = seleccionarMejoresRutas(nuevasRutas, progreso, 3);

        }
        
        Map<Integer, Simulacion> mejorRuta = obtenerMejorRuta(rutasActuales, progreso);

        System.out.println("\n--- MEJOR RUTA ENCONTRADA ---");
        
        Map<Integer, Simulacion> rutaOrdenada = mejorRuta.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (oldValue, newValue) -> oldValue,
                LinkedHashMap::new
            ));

        double puntajeTotal = calcularPuntajeRuta(rutaOrdenada, progreso);
        mostrarResultados(rutaOrdenada, puntajeTotal);

        return rutaOrdenada;
    }
    
    public Progreso calcularProgreso(Progreso progresoInicial, Map<Integer, Simulacion> ruta, int semestreHasta) {
        Progreso progresoTemporal = progresoInicial.copy();
        
        for (int sem = progresoInicial.getSemestre() + 1; sem <= semestreHasta; sem++) {
            Simulacion simulacion = ruta.get(sem);
            if (simulacion != null) {
                progresoTemporal = actualizarProgresoTemporal(progresoTemporal, simulacion, sem);
            }
        }
        
        return progresoTemporal;
    }
    
    public List<Combinacion> generarCombinaciones(Progreso progreso, Proyeccion proyeccion, List<MateriaJson> materiasPensum, int numCombinaciones) {
        List<MateriaJson> materiasDisponibles = filtrarMateriasDisponibles(progreso, materiasPensum, proyeccion);
        List<MateriaConPuntaje> materiasConPuntaje = calcularPuntajes(materiasDisponibles, progreso, proyeccion);
        mostrarMateriasPuntajes(materiasConPuntaje);
        List<Combinacion> mejoresCombinaciones = generarMejoresCombinaciones(materiasConPuntaje, proyeccion.getCreditos(), proyeccion.getMaterias()).stream().limit(numCombinaciones).collect(Collectors.toList());
        mostrarResultadosCombinaciones(mejoresCombinaciones);

        return mejoresCombinaciones;
    }
    
    public double calcularPuntajeRuta(Map<Integer, Simulacion> ruta, Progreso progresoInicial) {
        double puntajeTotal = 0.0;
        Progreso progresoTemporal = progresoInicial.copy();
        
        List<Integer> semestresOrdenados = ruta.keySet().stream()
            .sorted()
            .collect(Collectors.toList());
        
        for (Integer semestre : semestresOrdenados) {
            Simulacion sim = ruta.get(semestre);
            
            Proyeccion proyTemp = crearProyeccionParaSemestre(new Proyeccion(), semestre);
            
            for (MateriaJson materia : sim.getMaterias()) {
                puntajeTotal += calcularPuntajeMateria(materia, progresoTemporal, proyTemp);
            }
            
            progresoTemporal = actualizarProgresoTemporal(progresoTemporal, sim, semestre);
        }
        
        return puntajeTotal;
    }
    
    @SuppressWarnings("unchecked")
    public List<Map<Integer, Simulacion>> seleccionarMejoresRutas(List<Map<Integer, Simulacion>> rutas, Progreso progresoInicial, int maxRutas) {
        return rutas.stream()
            .map(ruta -> {
                double puntaje = calcularPuntajeRuta(ruta, progresoInicial);
                return Map.of("ruta", ruta, "puntaje", puntaje);
            })
            .sorted((a, b) -> Double.compare((Double)b.get("puntaje"), (Double)a.get("puntaje")))
            .limit(maxRutas)
            .map(r -> (Map<Integer, Simulacion>)r.get("ruta"))
            .collect(Collectors.toList());
    }
    
    public Map<Integer, Simulacion> obtenerMejorRuta(List<Map<Integer, Simulacion>> rutas, Progreso progresoInicial) {
        List<Map<Integer, Simulacion>> mejores = seleccionarMejoresRutas(rutas, progresoInicial, 1);
        return mejores.isEmpty() ? new HashMap<>() : mejores.get(0);
    }
    
    public void mostrarResultados(Map<Integer, Simulacion> ruta, double puntajeTotal) {
        for (Map.Entry<Integer, Simulacion> entry : ruta.entrySet()) {
            int semestre = entry.getKey();
            Simulacion sim = entry.getValue();
            
            System.out.println("\n--- SEMESTRE " + semestre + " ---");
            System.out.println("Materias: " + sim.getMaterias().size());
            int creditosSemestre = sim.getMaterias().stream().mapToInt(MateriaJson::getCreditos).sum();
            System.out.println("Créditos: " + creditosSemestre);
            
            for (int i = 0; i < sim.getMaterias().size(); i++) {
                MateriaJson materia = sim.getMaterias().get(i);
                System.out.printf("  %d. %s (%s) - %d créditos%n", 
                    i + 1, materia.getNombre(), materia.getCodigo(), materia.getCreditos());
            }
        }
        System.out.println("\nPuntaje total de la ruta: " + puntajeTotal);
    }

    public Proyeccion crearProyeccionParaSemestre(Proyeccion base, int semestre) {
        Proyeccion proyeccion = new Proyeccion();
        proyeccion.setSemestre(semestre);
        proyeccion.setCreditos(base.getCreditos());
        proyeccion.setMaterias(base.getMaterias());
        return proyeccion;
    }

    //ALGORITMO A*
    
    // Genera una simulación multi semestre utilizando el algoritmo A*
    public Map<Integer, Simulacion> generarSimulacionMultiSemestreAStar(Progreso progreso,Proyeccion proyeccionBase,int semestreObjetivo,List<MateriaJson> materiasPensum) {
        
        System.out.println("================ INICIO SIMULACIÓN A* PURO ================");
        System.out.println("Semestre actual: " + progreso.getSemestre());
        System.out.println("Semestre objetivo: " + semestreObjetivo);
        
        int semestresTotal = semestreObjetivo - progreso.getSemestre();
        int maxNodos;
        int maxCombinacionesPorNodo;
        
        if (semestresTotal <= 2) {
            maxNodos = 50000; 
            maxCombinacionesPorNodo = 10; 
        } else if (semestresTotal <= 4) {
            maxNodos = 25000; 
            maxCombinacionesPorNodo = 5; 
        } else {
            maxNodos = 10000; 
            maxCombinacionesPorNodo = 3;
        }
        
        PriorityQueue<NodoA> frontera = new PriorityQueue<>(
            Comparator.comparingDouble(NodoA::getCostoTotal)
        );
        
        Set<String> visitados = new HashSet<>();
        
        Map<Integer, Simulacion> rutaInicial = new HashMap<>();
        double heuristicaInicial = calcularHeuristica(progreso, semestreObjetivo, proyeccionBase, materiasPensum);

        NodoA nodoInicial = new NodoA(rutaInicial, progreso.getSemestre(), 0.0, heuristicaInicial, progreso);
        
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
                System.out.println("Tiempo total: " + tiempoTotal + "ms");
                
                Map<Integer, Simulacion> rutaCompleta = ordenarRuta(nodoActual.getRutaParcial());
                double puntajeTotal = calcularPuntajeRuta(rutaCompleta, progreso);
                mostrarResultados(rutaCompleta, puntajeTotal);
                return rutaCompleta;
            }

            if (nodoActual.getSemestreActual() >= semestreObjetivo) {
                long tiempoTotal = System.currentTimeMillis() - tiempoInicio;
                System.out.println("SOLUCION A* ENCONTRADA");
                System.out.println("Nodos explorados: " + nodosExplorados);
                System.out.println("Tiempo total: " + tiempoTotal + "ms");
                
                Map<Integer, Simulacion> rutaCompleta = ordenarRuta(nodoActual.getRutaParcial());
                double puntajeTotal = calcularPuntajeRuta(rutaCompleta, progreso);
                mostrarResultados(rutaCompleta, puntajeTotal);
                return rutaCompleta;
            }
            
            String claveEstado = generarClaveEstado(nodoActual.getProgresoActual(), nodoActual.getSemestreActual());
            if (visitados.contains(claveEstado)) continue;
            visitados.add(claveEstado);
            
            expandirNodo(nodoActual, frontera, semestreObjetivo, proyeccionBase, materiasPensum, maxCombinacionesPorNodo);
        }
        
        long tiempoTotal = System.currentTimeMillis() - tiempoInicio;
        System.out.println("A* alcanzó límite de nodos: " + maxNodos);
        System.out.println("Tiempo transcurrido: " + tiempoTotal + "ms");
        System.out.println("A* no pudo encontrar ninguna solución completa");
        return new HashMap<>();
    }
    
    // Genera una clave única para el estado del nodo basado en el progreso y semestre
    public void expandirNodo(NodoA nodoActual, PriorityQueue<NodoA> frontera,int semestreObjetivo, Proyeccion proyeccionBase,List<MateriaJson> materiasPensum, int maxCombinaciones) {
        
        int siguienteSemestre = nodoActual.getSemestreActual() + 1;
        if (siguienteSemestre > semestreObjetivo) return;
        
        Proyeccion proyeccionSemestre = crearProyeccionParaSemestre(proyeccionBase, siguienteSemestre);
        
        List<Combinacion> combinaciones = generarCombinaciones(nodoActual.getProgresoActual(), proyeccionSemestre, materiasPensum, maxCombinaciones);
        
        for (Combinacion combinacion : combinaciones) {
            Map<Integer, Simulacion> nuevaRuta = new HashMap<>(nodoActual.getRutaParcial());
            
            Simulacion simulacionSemestre = new Simulacion();
            for (MateriaJson materia : combinacion.getMaterias()) {
                simulacionSemestre.agregarMateria(materia);
            }
            nuevaRuta.put(siguienteSemestre, simulacionSemestre);
            
            Progreso nuevoProgreso = nodoActual.getProgresoActual().copy();
            nuevoProgreso = actualizarProgresoTemporal(nuevoProgreso, simulacionSemestre, siguienteSemestre);
            
            double nuevoCosto = nodoActual.getCostoAcumulado() + calcularCostoTransicion(combinacion);
            double nuevaHeuristica = calcularHeuristica(nuevoProgreso, semestreObjetivo, proyeccionBase, materiasPensum);
            
            NodoA nuevoNodo = new NodoA(nuevaRuta, siguienteSemestre, nuevoCosto, nuevaHeuristica, nuevoProgreso);
            frontera.offer(nuevoNodo);
        }
    }
    
    // Nueva versión de actualizar progreso temporal para que sirva con A*
    public Progreso actualizarProgresoTemporal(Progreso progreso, Simulacion simulacion, int semestreSimulado) {
        
        List<MateriaJson> materiasARemover = new ArrayList<>();
        for (MateriaJson materiaSimulada : simulacion.getMaterias()) {
            
            if (!materiaSimulada.getCodigo().equals("0") &&!materiaSimulada.getCodigo().equals("1") && !materiaSimulada.getCodigo().equals("5") && !materiaSimulada.getCodigo().equals("6")) {
    
                for (MateriaJson materiaFaltante : progreso.getListaMateriasFaltantes()) {
                    if (materiaFaltante.getCodigo().equals(materiaSimulada.getCodigo()) || (materiaFaltante.getNombre().equals(materiaSimulada.getNombre()) && materiaFaltante.getSemestre() == materiaSimulada.getSemestre())) {
                        materiasARemover.add(materiaFaltante);
                        break;
                    }
                }
            }
        }
    
        progreso.getListaMateriasFaltantes().removeAll(materiasARemover);
    
        for (MateriaJson materia : simulacion.getMaterias()) {
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
    
        int creditosNucleoSimulados = materiasARemover.stream().mapToInt(MateriaJson::getCreditos).sum();
        progreso.setCreditosPensum(progreso.getCreditosPensum() + creditosNucleoSimulados);
        progreso.setTotalCreditos(progreso.getTotalCreditos() + creditosNucleoSimulados);
    
        progreso.setSemestre(semestreSimulado);
    
        return progreso;
    }
    
    // Calcular la heurística para A* que se enfoca en el progreso actual y el semestre objetivo
    public double calcularHeuristica(Progreso progreso, int semestreObjetivo, Proyeccion proyeccionBase, List<MateriaJson> materiasPensum) {
        
        int semestresRestantes = semestreObjetivo - progreso.getSemestre();
        double heuristica = 0.0;
        
        double factorPeso;
        if (semestresRestantes <= 2) {
            factorPeso = 1.0; 
        } else if (semestresRestantes <= 4) {
            factorPeso = 0.5;
        } else {
            factorPeso = 0.3;
        }
        
        double heuristicaMaterias = 0.0;
        
        int materiasNucleoFaltantes = contarMateriasNucleoFaltantes(progreso);
        heuristicaMaterias += materiasNucleoFaltantes * (180 * factorPeso);
        
        double electivasFaltantes = progreso.getFaltanElectiva() / 3.0;
        heuristicaMaterias += electivasFaltantes * (80 * factorPeso);
        
        double complementariasFaltantes = progreso.getFaltanComplementaria() / 3.0;
        heuristicaMaterias += complementariasFaltantes * (100 * factorPeso);
        
        double enfasisFaltantes = progreso.getFaltanEnfasis() / 3.0;
        heuristicaMaterias += enfasisFaltantes * (100 * factorPeso);
        
        double electivasCBFaltantes = progreso.getFaltanElectivaBasicas() / 3.0;
        heuristicaMaterias += electivasCBFaltantes * (90 * factorPeso);
       
        heuristica = heuristicaMaterias;
        
        double penalizacionDistancia = semestresRestantes * 25;
        heuristica += penalizacionDistancia;
        
        double factorProgreso = (double) progreso.getMateriasCursadas() / (progreso.getMateriasCursadas() + progreso.getTotalFaltantes());
        double factorReduccion = (1.0 - factorProgreso * 0.3);
        heuristica *= factorReduccion;
        System.out.println("HEURISTICA FINAL: " + String.format("%.2f", Math.max(heuristica, 1.0)));
        
        return Math.max(heuristica, 1.0);
    }

    // Calcular el costo de transición entre combinaciones
    public double calcularCostoTransicion(Combinacion combinacion) {
        return Math.max(1000 - combinacion.getPuntajeTotal(), 100);
    }

    // Contar materias de núcleo faltantes
    public int contarMateriasNucleoFaltantes(Progreso progreso) {
        int count = 0;
        for (MateriaJson materia : progreso.getListaMateriasFaltantes()) {
            if (esMateriaNucleo(materia)) {
                count++;
            }
        }
        return count;
    }

    // Validar si una materia es de núcleo
    public boolean esMateriaNucleo(MateriaJson materia) {
        String codigo = materia.getCodigo();
        return !codigo.equals("0") && !codigo.equals("1") && 
            !codigo.equals("5") && !codigo.equals("6");
    }

    // Generar una clave de estado única para el progreso y semestre
    public String generarClaveEstado(Progreso progreso, int semestre) {
        StringBuilder sb = new StringBuilder();
        sb.append("sem:").append(semestre);
        sb.append("|mat:").append(
            progreso.getListaMateriasFaltantes().stream()
                .map(MateriaJson::getCodigo)
                .sorted()
                .collect(Collectors.joining(","))
            );
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
                LinkedHashMap::new
            ));
    }

    // Verifica si el progreso ha completado todas las materias requeridas
    public boolean haCompletadoTodasLasMaterias(Progreso progreso) {
        boolean nucleoCompleto = contarMateriasNucleoFaltantes(progreso) == 0;
        boolean electivasCompletas = progreso.getFaltanElectiva() <= 0;
        boolean complementariasCompletas = progreso.getFaltanComplementaria() <= 0;
        boolean enfasisCompleto = progreso.getFaltanEnfasis() <= 0;
        boolean electivasCBCompletas = progreso.getFaltanElectivaBasicas() <= 0;
        
        return nucleoCompleto && electivasCompletas && complementariasCompletas && enfasisCompleto && electivasCBCompletas;
    }
    
}
