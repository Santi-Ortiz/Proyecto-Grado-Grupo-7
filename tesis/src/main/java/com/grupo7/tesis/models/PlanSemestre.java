package com.grupo7.tesis.models;

import java.util.ArrayList;
import java.util.List;

public class PlanSemestre {
    private List<Materia> materias;
    private double puntajeTotal;
    private int creditosTotales;

    public PlanSemestre(List<Materia> materias, double puntaje, int creditos) {
        this.materias = new ArrayList<>(materias);
        this.puntajeTotal = puntaje;
        this.creditosTotales = creditos;
    }

    public List<Materia> getMaterias() {
        return materias;
    }

    public double getPuntajeTotal() {
        return puntajeTotal;
    }

    public int getCreditosTotales() {
        return creditosTotales;
    }
}
