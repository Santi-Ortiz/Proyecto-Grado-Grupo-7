package com.grupo7.tesis.model;

import java.util.List;

public class Progreso {
    private double promedio;
    private int materiasCursadas;
    private int materiasFaltantes;
    private List<MateriaJson> listaMateriasFaltantes;
    private int totalMaterias;
    private int totalFaltantes;
    private int totalCursando;

    public Progreso(double promedio, int materiasCursadas, int materiasFaltantes, List<MateriaJson> listaMateriasFaltantes, int totalMaterias, int totalFaltantes, int totalCursantes) {
        this.promedio = promedio;
        this.materiasCursadas = materiasCursadas;
        this.materiasFaltantes = materiasFaltantes;
        this.listaMateriasFaltantes = listaMateriasFaltantes;
        this.totalMaterias = totalMaterias;
        this.totalFaltantes = totalFaltantes;
        this.totalCursando = totalCursantes;
    }

    public double getPromedio() { return promedio; }
    public int getMateriasCursadas() { return materiasCursadas; }
    public int getMateriasFaltantes() { return materiasFaltantes; }
    public List<MateriaJson> getListaMateriasFaltantes() { return listaMateriasFaltantes; }
    public int getTotalMaterias() { return totalMaterias; }
    public int getTotalFaltantes() { return totalFaltantes;};
    public int getTotalCursando() { return totalCursando; }
}

