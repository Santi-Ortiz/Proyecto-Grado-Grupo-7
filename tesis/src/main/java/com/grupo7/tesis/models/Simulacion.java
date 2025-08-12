package com.grupo7.tesis.models;

import java.util.ArrayList;
import java.util.List;

public class Simulacion {
    private List<Materia> materias;
    private double puntajeTotal;

    public Simulacion() {
    }

    public Simulacion(List<Materia> materias, double puntaje) {
        this.materias = new ArrayList<>(materias);
        this.puntajeTotal = puntaje;
    }

    public List<Materia> getMaterias() {
        return materias;
    }

    public double getPuntajeTotal() {
        return puntajeTotal;
    }

    public void setMaterias(List<Materia> materias) {
        this.materias = materias;
    }

    public void setPuntajeTotal(double puntajeTotal) {
        this.puntajeTotal = puntajeTotal;
    }

    public void agregarMateria(Materia materia) {
        if (this.materias == null) {
            this.materias = new ArrayList<>();
        }
        this.materias.add(materia);
    }

    public int getTotalCreditos() {
        int total = 0;
        if (materias != null) {
            for (Materia materia : materias) {
                total += materia.getCreditos();
            }
        }
        return total;
    }

}
