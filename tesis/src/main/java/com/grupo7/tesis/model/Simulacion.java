package com.grupo7.tesis.model;

import java.util.ArrayList;
import java.util.List;

public class Simulacion {

    private List<MateriaJson> materias;

    // Constructor que inicializa la lista
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
