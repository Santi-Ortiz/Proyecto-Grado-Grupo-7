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

            // TODO: hacer validacion para electivas
            // TODO: hacer validacion para complementarias
            // TODO: hacer validacion para énfasis
            // TODO: hacer validacion para electivas de ciencias básicas

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

}
