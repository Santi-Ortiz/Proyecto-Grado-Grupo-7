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
    private int totalCreditos;
    private int faltanElectiva;
    private int faltanComplementaria;
    private int faltanEnfasis;
    private int faltanElectivaBasicas;

    public Progreso(double promedio, int materiasCursadas, int materiasFaltantes, List<MateriaJson> listaMateriasFaltantes, int totalMaterias, int totalFaltantes, int totalCursantes, int totalCreditos, int faltanElectiva, int faltanComplementaria, int faltanEnfasis, int faltanElectivaBasicas) {
        this.promedio = promedio;
        this.materiasCursadas = materiasCursadas;
        this.materiasFaltantes = materiasFaltantes;
        this.listaMateriasFaltantes = listaMateriasFaltantes;
        this.totalMaterias = totalMaterias;
        this.totalFaltantes = totalFaltantes;
        this.totalCursando = totalCursantes;
        this.totalCreditos = totalCreditos;
        this.faltanElectiva = faltanElectiva;
        this.faltanComplementaria = faltanComplementaria;
        this.faltanEnfasis = faltanEnfasis;
        this.faltanElectivaBasicas = faltanElectivaBasicas;
    }

    public double getPromedio() { return promedio; }
    public int getMateriasCursadas() { return materiasCursadas; }
    public int getMateriasFaltantes() { return materiasFaltantes; }
    public List<MateriaJson> getListaMateriasFaltantes() { return listaMateriasFaltantes; }
    public int getTotalMaterias() { return totalMaterias; }
    public int getTotalFaltantes() { return totalFaltantes;};
    public int getTotalCursando() { return totalCursando; }
    public int getTotalCreditos() { return totalCreditos; }
    public int getFaltanElectiva() { return faltanElectiva; }
    public int getFaltanComplementaria() { return faltanComplementaria; }
    public int getFaltanEnfasis() { return faltanEnfasis; }
    public int getFaltanElectivaBasicas() { return faltanElectivaBasicas; }
}

