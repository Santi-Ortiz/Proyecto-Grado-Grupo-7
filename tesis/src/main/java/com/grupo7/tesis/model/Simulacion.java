package com.grupo7.tesis.model;

import java.util.List;

public class Simulacion {

    private List<MateriaJson> materias;

    public List<MateriaJson> getMaterias() {
        return materias;
    }

    public void setMaterias(List<MateriaJson> materias) {
        this.materias = materias;
    }

    public void agregarMateria(MateriaJson materia) {
        this.materias.add(materia);
    }

}
