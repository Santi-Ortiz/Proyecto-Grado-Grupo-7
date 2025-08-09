package com.grupo7.tesis.dtos;

import com.grupo7.tesis.models.Materia;

public class MateriaConPuntajeDTO {
    private Materia materia;
    private double puntaje;

    public MateriaConPuntajeDTO(Materia materia, double puntaje) {
        this.materia = materia;
        this.puntaje = puntaje;
    }

    public Materia getMateria() {
        return materia;
    }

    public double getPuntaje() {
        return puntaje;
    }
}