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
    private int creditosPensum; 
    private int creditosExtra;
    private int faltanElectiva;
    private int faltanComplementaria;
    private int faltanEnfasis;
    private int faltanElectivaBasicas;
    private int semestre;

    public Progreso(double promedio, int materiasCursadas, int materiasFaltantes, List<MateriaJson> listaMateriasFaltantes, int totalMaterias, int totalFaltantes, int totalCursantes, int totalCreditos, int creditosPensum, int creditosExtra, int faltanElectiva, int faltanComplementaria, int faltanEnfasis, int faltanElectivaBasicas, int semestre) {
        this.promedio = promedio;
        this.materiasCursadas = materiasCursadas;
        this.materiasFaltantes = materiasFaltantes;
        this.listaMateriasFaltantes = listaMateriasFaltantes;
        this.totalMaterias = totalMaterias;
        this.totalFaltantes = totalFaltantes;
        this.totalCursando = totalCursantes;
        this.totalCreditos = totalCreditos;
        this.creditosPensum = creditosPensum;
        this.creditosExtra = creditosExtra;
        this.faltanElectiva = faltanElectiva;
        this.faltanComplementaria = faltanComplementaria;
        this.faltanEnfasis = faltanEnfasis;
        this.faltanElectivaBasicas = faltanElectivaBasicas;
        this.semestre = semestre;
    }

    public double getPromedio() { return promedio; }
    public int getMateriasCursadas() { return materiasCursadas; }
    public int getMateriasFaltantes() { return materiasFaltantes; }
    public List<MateriaJson> getListaMateriasFaltantes() { return listaMateriasFaltantes; }
    public int getTotalMaterias() { return totalMaterias; }
    public int getTotalFaltantes() { return totalFaltantes;};
    public int getTotalCursando() { return totalCursando; }
    public int getTotalCreditos() { return totalCreditos; }
    public int getCreditosPensum() { return creditosPensum; }
    public int getCreditosExtra() { return creditosExtra; }
    public int getFaltanElectiva() { return faltanElectiva; }
    public int getFaltanComplementaria() { return faltanComplementaria; }
    public int getFaltanEnfasis() { return faltanEnfasis; }
    public int getFaltanElectivaBasicas() { return faltanElectivaBasicas; }
    public int getSemestre() { return semestre;}
}