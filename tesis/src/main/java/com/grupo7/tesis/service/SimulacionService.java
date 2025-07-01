package com.grupo7.tesis.service;

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
        int electivas = 0;
        int complementarias = 0;
        int enfasis = 0;
        int electivasCB = 0;
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

        electivas = validarElectivas(progreso, materiasPensum, proyeccion.getSemestre());
        complementarias = validarComplementarias(progreso, materiasPensum, proyeccion.getSemestre());
        enfasis = validarEnfasis(progreso, materiasPensum, proyeccion.getSemestre());
        electivasCB = validarElectivasCB(progreso, materiasPensum, proyeccion.getSemestre());

        MateriaJson enfasisMateria = verificarMateria(enfasis, creditos - contadorCreditosSimulacion, materias - contadorMateriasSimulacion, "5", "Enfasis", proyeccion.getSemestre());
        
        if(enfasisMateria != null){
            simulacion.agregarMateria(enfasisMateria);
            contadorCreditosSimulacion += enfasisMateria.getCreditos();
            contadorMateriasSimulacion++;
        }

        MateriaJson complementaria = verificarMateria(complementarias, creditos - contadorCreditosSimulacion, materias - contadorMateriasSimulacion, "1", "Complementarias", proyeccion.getSemestre());

        if(complementaria != null){
            simulacion.agregarMateria(complementaria);
            contadorCreditosSimulacion += complementaria.getCreditos();
            contadorMateriasSimulacion++;
        }
        
        MateriaJson electiva = verificarMateria(electivas, creditos - contadorCreditosSimulacion, materias - contadorMateriasSimulacion, "0", "Electiva", proyeccion.getSemestre());
        
        if(electiva != null){
            simulacion.agregarMateria(electiva);
            contadorCreditosSimulacion += electiva.getCreditos();
            contadorMateriasSimulacion++;
        }
        
        MateriaJson electivaCB = verificarMateria(electivasCB, creditos - contadorCreditosSimulacion, materias - contadorMateriasSimulacion, "6", "Electiva CB", proyeccion.getSemestre());

        if(electivaCB != null){
            simulacion.agregarMateria(electivaCB);
            contadorCreditosSimulacion += electivaCB.getCreditos();
            contadorMateriasSimulacion++;
        }

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
}
