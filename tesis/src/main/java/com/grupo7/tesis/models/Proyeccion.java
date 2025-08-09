package com.grupo7.tesis.models;

public class Proyeccion {

    private int semestre; // Representa la cantidad de semestres a simular
    private int creditos; // Representa la cantidad de créditos MÁXIMA que tendrán las simulaciones
    private int materias; // Representa la cantidad de materias MÁXIMA que tendrán las simulaciones

    public Proyeccion() {
    }

    public Proyeccion(int semestre, int creditos, int materias, int tipoMatricula, int doblePrograma) {
        this.semestre = semestre;
        this.creditos = creditos;
        this.materias = materias;
    }

    public int getSemestre() {
        return semestre;
    }

    public void setSemestre(int semestre) {
        this.semestre = semestre;
    }

    public int getCreditos() {
        return creditos;
    }

    public void setCreditos(int creditos) {
        this.creditos = creditos;
    }

    public int getMaterias() {
        return materias;
    }

    public void setMaterias(int materias) {
        this.materias = materias;
    }

    @Override
    public String toString() {
        return "Proyeccion [semestre: " + semestre + ", creditos: " + creditos + ", materias: " + materias + "]";
    }

}
