package com.grupo7.tesis.models;

import java.util.HashMap;
import java.util.Map;

public class NodoA {

    private Map<Integer, Simulacion> rutaParcial;
    private int semestreActual;
    private double costoTotal;
    private Progreso progresoActual;

    public NodoA(Map<Integer, Simulacion> rutaParcial, int semestre, double total, 
            Progreso progreso) {
        this.rutaParcial = new HashMap<>(rutaParcial);
        this.semestreActual = semestre;
        this.costoTotal = total;
        this.progresoActual = progreso.copy();
    }

    public Map<Integer, Simulacion> getRutaParcial() {
        return rutaParcial;
    }

    public int getSemestreActual() {
        return semestreActual;
    }

    public double getCostoTotal() {
        return costoTotal;
    }

    public Progreso getProgresoActual() {
        return progresoActual;
    }

    public int getTotalCreditos() {
        return rutaParcial.values().stream()
                .mapToInt(Simulacion::getTotalCreditos)
                .sum();
    }


}
