package com.grupo7.tesis.models;

import java.time.LocalDateTime;
import java.util.Arrays;

public class Proyeccion {

    private int semestre; // Representa la cantidad de semestres a simular
    private int creditos; // Representa la cantidad de créditos MÁXIMA que tendrán las simulaciones
    private int materias; // Representa la cantidad de materias MÁXIMA que tendrán las simulaciones
    /*Nuevos atributos para base de datos */
    private String nombreSimulacion;
    private String tipoMatricula;
    private boolean practicaProfesional;
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    private boolean[] priorizaciones;

    public Proyeccion() {
    }

    public Proyeccion(int semestre, int creditos, int materias, String nombreSimulacion, String tipoMatricula, boolean practicaProfesional, boolean[] priorizaciones) {
        this.semestre = semestre;
        this.creditos = creditos;
        this.materias = materias;
        this.nombreSimulacion = nombreSimulacion;
        this.tipoMatricula = tipoMatricula;
        this.practicaProfesional = practicaProfesional;
        this.fechaCreacion =  LocalDateTime.now();
        this.priorizaciones = priorizaciones;
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

    public String getNombreSimulacion() {
        return nombreSimulacion;
    }

    public void setNombreSimulacion(String nombreSimulacion) {
        this.nombreSimulacion = nombreSimulacion;
    }

    public String getTipoMatricula() {
        return tipoMatricula;
    }

    public void setTipoMatricula(String tipoMatricula) {
        this.tipoMatricula = tipoMatricula;
    }

    public boolean getPracticaProfesional() {
        return practicaProfesional;
    }

    public void setPracticaProfesional(boolean practicaProfesional) {
        this.practicaProfesional = practicaProfesional;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public boolean[] getPriorizaciones() {
        return priorizaciones;
    }

    public void setPriorizaciones(boolean[] priorizaciones) {
        this.priorizaciones = priorizaciones;
    }

    @Override
    public String toString() {
        return "Proyeccion [semestre: " + semestre + ", creditos: " + creditos + ", materias: " + materias + "]" + ", nombreSimulacion: " + nombreSimulacion + ", tipoMatricula: " + tipoMatricula + ", practicaProfesional: " + practicaProfesional + ", fechaCreacion: " + fechaCreacion + ", priorizaciones: " + Arrays.toString(priorizaciones) + "]";
    }

}
