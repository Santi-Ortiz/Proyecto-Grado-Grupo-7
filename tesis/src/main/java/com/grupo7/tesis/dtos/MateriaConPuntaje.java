package com.grupo7.tesis.dtos;

import com.grupo7.tesis.models.MateriaJson;

public class MateriaConPuntaje {
    private MateriaJson materia;
    private double puntaje;

    public MateriaConPuntaje(MateriaJson materia, double puntaje) {
        this.materia = materia;
        this.puntaje = puntaje;
    }

    public MateriaJson getMateria() {
        return materia;
    }

    public double getPuntaje() {
        return puntaje;
    }
}