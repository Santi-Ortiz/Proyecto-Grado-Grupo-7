package com.grupo7.tesis.models;

import java.util.ArrayList;
import java.util.List;

public class Simulacion {

    private List<MateriaJson> materias;

    public Simulacion() {
        this.materias = new ArrayList<>();
    }

    public List<MateriaJson> getMaterias() {
        return materias;
    }

    public void setMaterias(List<MateriaJson> materias) {
        this.materias = materias;
    }

    public void agregarMateria(MateriaJson materia) {
        if (this.materias == null) {
            this.materias = new ArrayList<>();
        }
        this.materias.add(materia);
    }

}
