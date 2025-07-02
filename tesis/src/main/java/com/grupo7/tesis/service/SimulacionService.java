package com.grupo7.tesis.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.grupo7.tesis.model.MateriaJson;
import com.grupo7.tesis.model.Progreso;
import com.grupo7.tesis.model.Proyeccion;
import com.grupo7.tesis.model.Simulacion;

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
                    // Se añade una materia en la simulación priorizando las materias de semestres anteriores al actual no vistas
                        simulacion.agregarMateria(p);
                        contadorCreditosSimulacion += p.getCreditos();
                        contadorMateriasSimulacion++;
                }
            }
        }
        
        //Revisar enfasis, complementarias y electivas de semestres pasados y actual
        List<MateriaJson> electivasActuales = generarMateriasElectivas(progreso, materiasPensum, proyeccion.getSemestre(),creditos - contadorCreditosSimulacion, materias - contadorMateriasSimulacion, simulacion);
    
        for(MateriaJson materia : electivasActuales) {
            simulacion.agregarMateria(materia);
            contadorCreditosSimulacion += materia.getCreditos();
            contadorMateriasSimulacion++;
        }

        //Revisa materias de semestres superiores que no se han cursado
        if (creditos - contadorCreditosSimulacion > 0 &&  materias - contadorMateriasSimulacion > 0) {
            for (MateriaJson p : progreso.getListaMateriasFaltantes()) {
                if (p.getSemestre() <= proyeccion.getSemestre() + 1 &&
                        contadorCreditosSimulacion + p.getCreditos() <= creditos &&
                        contadorMateriasSimulacion + 1 <= materias) {
                    if (validarPrerequisito(progreso, materiasPensum, p.getRequisitos())) {
                        boolean yaEstaEnSimulacion = false;
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
                            if (creditos - contadorCreditosSimulacion <= 0 ||  materias - contadorMateriasSimulacion <= 0) {
                                break;
                            }
                        }
                    }
                }
            }
        }

        // Revisar electivas, complementarias y énfasis de semestres superiores, en caso de que no se hayan alcanzado los créditos o materias
        if (creditos - contadorCreditosSimulacion > 0 && materias - contadorMateriasSimulacion > 0) {
            List<MateriaJson> electivasSuperiores = generarMateriasElectivas(progreso, materiasPensum, proyeccion.getSemestre() + 1, creditos - contadorCreditosSimulacion, materias - contadorMateriasSimulacion, simulacion);
            
            for(MateriaJson materia : electivasSuperiores) {
                simulacion.agregarMateria(materia);
                contadorCreditosSimulacion += materia.getCreditos();
                contadorMateriasSimulacion++;
            }
        }

        return simulacion;
    }

    // Esto valida si una materia puede ser añadida en la simulación a partir de los prerequisitos
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

    // Devuelve la cantidad de créditos que faltan por cursar en electivas para ese semestre
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

    // Devuelve la cantidad de créditos que faltan por cursar en complementarias para ese semestre
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

    // Devuelve la cantidad de créditos que faltan por cursar en énfasis para ese semestre
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

    // Devuelve la cantidad de créditos que faltan por cursar en electivas de cs.básicas para ese semestre
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


    public MateriaJson verificarMateria (int creditosRestantes, int creditosRestantesGeneral, int materiasRestantesGeneral, String codigo, String nombre, int semestre){
        if (creditosRestantes > 0) {

            if(creditosRestantesGeneral > 0 && materiasRestantesGeneral > 0) {
                MateriaJson materiaSugerida = new MateriaJson();
                materiaSugerida.setCodigo(codigo);
                materiaSugerida.setNombre(nombre);
                materiaSugerida.setSemestre(semestre);

                if(codigo.equals("5")){
                    if(creditosRestantesGeneral >= 6 && creditosRestantes >= 6) {
                        materiaSugerida.setCreditos(6);
                    } else if(creditosRestantesGeneral >= 3 && creditosRestantes >= 3) {
                        materiaSugerida.setCreditos(3);
                    } else {
                        return null;
                    }
                } else if (codigo.equals("6") || codigo.equals("1")) {
                    if(creditosRestantesGeneral >= 3 && creditosRestantes >= 3) {
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

    private List<MateriaJson> generarMateriasElectivas(Progreso progreso, List<MateriaJson> materiasPensum, int semestre, int creditosDisponibles, int materiasDisponibles, Simulacion simulacionActual) {
        List<MateriaJson> materiasGeneradas = new ArrayList<>();
        
        // Calcular cuántos créditos de cada tipo ya están en la simulación
        int enfasisYaUsado = calcularCreditosUsadosEnSimulacion(simulacionActual, "5");
        int complementariasYaUsado = calcularCreditosUsadosEnSimulacion(simulacionActual, "1");
        int electivasYaUsado = calcularCreditosUsadosEnSimulacion(simulacionActual, "0");
        int electivasCBYaUsado = calcularCreditosUsadosEnSimulacion(simulacionActual, "6");
        
        // Calcular créditos faltantes considerando lo ya añadido
        int electivas = Math.max(validarElectivas(progreso, materiasPensum, semestre) - electivasYaUsado, 0);
        int complementarias = Math.max(validarComplementarias(progreso, materiasPensum, semestre) - complementariasYaUsado, 0);
        int enfasis = Math.max(validarEnfasis(progreso, materiasPensum, semestre) - enfasisYaUsado, 0);
        int electivasCB = Math.max(validarElectivasCB(progreso, materiasPensum, semestre) - electivasCBYaUsado, 0);
        
        int creditosRestantes = creditosDisponibles;
        int materiasRestantes = materiasDisponibles;

        MateriaJson enfasisMateria = verificarMateria(enfasis, creditosRestantes, materiasRestantes, "5", "Enfasis", semestre);
        if(enfasisMateria != null) {
            materiasGeneradas.add(enfasisMateria);
            creditosRestantes -= enfasisMateria.getCreditos();
            materiasRestantes--;
        }
        
        MateriaJson complementaria = verificarMateria(complementarias, creditosRestantes, materiasRestantes, "1", "Complementarias", semestre);
        if(complementaria != null) {
            materiasGeneradas.add(complementaria);
            creditosRestantes -= complementaria.getCreditos();
            materiasRestantes--;
        }
        
        MateriaJson electiva = verificarMateria(electivas, creditosRestantes, materiasRestantes, "0", "Electiva", semestre);
        if(electiva != null) {
            materiasGeneradas.add(electiva);
            creditosRestantes -= electiva.getCreditos();
            materiasRestantes--;
        }
        
        MateriaJson electivaCB = verificarMateria(electivasCB, creditosRestantes, materiasRestantes, "6", "Electiva CB", semestre);
        if(electivaCB != null) {
            materiasGeneradas.add(electivaCB);
            creditosRestantes -= electivaCB.getCreditos();
            materiasRestantes--;
        }
        
        return materiasGeneradas;
    }

    // Helper para calcular cuántos créditos de un tipo específico ya están en la simulación
    private int calcularCreditosUsadosEnSimulacion(Simulacion simulacion, String tipoCodigo) {
        int creditosUsados = 0;
        for (MateriaJson materia : simulacion.getMaterias()) {
            if (materia.getCodigo().equals(tipoCodigo)) {
                creditosUsados += materia.getCreditos();
            }
        }
        return creditosUsados;
    }

}
