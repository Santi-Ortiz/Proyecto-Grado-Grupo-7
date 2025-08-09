package com.grupo7.tesis.models;

public class Proyeccion {

    private int semestre;
    private int creditos;
    private int materias;

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
