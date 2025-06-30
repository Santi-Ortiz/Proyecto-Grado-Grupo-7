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
        String tipoMateria = "";

        for (MateriaJson p : progreso.getListaMateriasFaltantes()) {
            tipoMateria = determinarTipoMateria(p.getCodigo());
            if (p.getSemestre() <= proyeccion.getSemestre() &&
                    contadorCreditosSimulacion + p.getCreditos() <= proyeccion.getCreditos() &&
                    contadorMateriasSimulacion + 1 <= proyeccion.getMaterias()) {

                if (validarPrerequisito(progreso, materiasPensum, p.getRequisitos())) {
                    // Se añade una materia en la simulación priorizando las materias de semestres
                    // anteriores al actual no vistas

                    if (tipoMateria.equals("Nucleo")) {
                        simulacion.agregarMateria(p);
                        contadorCreditosSimulacion += p.getCreditos();
                        contadorMateriasSimulacion++;
                    } else if (tipoMateria.equals("Electiva")
                            && validarElectivas(progreso, materiasPensum, p.getSemestre()) > 0) {
                        simulacion.agregarMateria(p);
                        contadorCreditosSimulacion += p.getCreditos();
                        contadorMateriasSimulacion++;
                    } else if (tipoMateria.equals("Complementaria")
                            && validarComplementarias(progreso, materiasPensum, p.getSemestre()) > 0) {
                        simulacion.agregarMateria(p);
                        contadorCreditosSimulacion += p.getCreditos();
                        contadorMateriasSimulacion++;
                    } else if (tipoMateria.equals("Enfasis")
                            && validarEnfasis(progreso, materiasPensum, p.getSemestre()) > 0) {
                        simulacion.agregarMateria(p);
                        contadorCreditosSimulacion += p.getCreditos();
                        contadorMateriasSimulacion++;
                    } else if (tipoMateria.equals("ElectivaCB")
                            && validarElectivasCB(progreso, materiasPensum, p.getSemestre()) > 0) {
                        simulacion.agregarMateria(p);
                        contadorCreditosSimulacion += p.getCreditos();
                        contadorMateriasSimulacion++;
                    }
                }
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

    // Devuelve la cantidad de créditos que faltan por cursar en electivas de cs.
    // básicas para ese semestre
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

    // Retorna el tipo de materia según su código
    public String determinarTipoMateria(String codigo) {
        switch (codigo) {
            case "0":
                return "Electiva";
            case "1":
                return "Complementaria";
            case "5":
                return "Enfasis";
            case "6":
                return "ElectivaCB";
            default:
                return "Nucleo";
        }
    }

}
