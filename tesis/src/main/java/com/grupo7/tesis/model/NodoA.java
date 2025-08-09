package com.grupo7.tesis.model;

import java.util.HashMap;
import java.util.Map;

public class NodoA {

    private Map<Integer, Simulacion> rutaParcial;
    private int semestreActual;
    private double costoAcumulado;
    private double heuristica;
    private double costoTotal;
    private Progreso progresoActual;
    
    public NodoA(Map<Integer, Simulacion> rutaParcial, int semestre, double costo, double heuristica, Progreso progreso) {
        this.rutaParcial = new HashMap<>(rutaParcial);
        this.semestreActual = semestre;
        this.costoAcumulado = costo;
        this.heuristica = heuristica;
        this.costoTotal = costo + heuristica;
        this.progresoActual = progreso.copy();
    }
    
    public Map<Integer, Simulacion> getRutaParcial() { return rutaParcial; }
    public int getSemestreActual() { return semestreActual; }
    public double getCostoAcumulado() { return costoAcumulado; }
    public double getHeuristica() { return heuristica; }
    public double getCostoTotal() { return costoTotal; }
    public Progreso getProgresoActual() { return progresoActual; }
}
