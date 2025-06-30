package com.grupo7.tesis.model;

public class Proyeccion {

    private int semestre;
    private int creditos;
    private int materias;
    private int tipoMatricula;
    private int doblePrograma;

    public Proyeccion() {
    }

    public Proyeccion(int semestre, int creditos, int materias, int tipoMatricula, int doblePrograma) {
        this.semestre = semestre;
        this.creditos = creditos;
        this.materias = materias;
        this.tipoMatricula = tipoMatricula;
        this.doblePrograma = doblePrograma;
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

    public int getTipoMatricula() {
        return tipoMatricula;
    }

    public void setTipoMatricula(int tipoMatricula) {
        this.tipoMatricula = tipoMatricula;
    }

    public int getDoblePrograma() {
        return doblePrograma;
    }

    public void setDoblePrograma(int doblePrograma) {
        this.doblePrograma = doblePrograma;
    }

}
