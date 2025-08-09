package com.grupo7.tesis.models;

import java.util.ArrayList;
import java.util.List;

public class Simulacion {

    private List<Materia> materias;

    public Simulacion() {
        this.materias = new ArrayList<>();
    }

    public List<Materia> getMaterias() {
        return materias;
    }

    public void setMaterias(List<Materia> materias) {
        this.materias = materias;
    }

    public void agregarMateria(Materia materia) {
        if (this.materias == null) {
            this.materias = new ArrayList<>();
        }
        this.materias.add(materia);
    }

}
