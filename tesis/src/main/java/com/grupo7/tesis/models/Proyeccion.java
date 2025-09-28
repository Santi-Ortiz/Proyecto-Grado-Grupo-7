package com.grupo7.tesis.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Arrays;

@Entity
@Table(name = "proyeccion")
public class Proyeccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "proyeccion_id")
    private Long id;

    private int semestre; // Representa la cantidad de semestres hacia delante que se desean simular

    private int numMaxCreditos; // Representa la cantidad de créditos MÁXIMA que tendrán las simulaciones

    private int numMaxMaterias; // Representa la cantidad de numMaxMaterias MÁXIMA que tendrán las simulaciones

    /*Nuevos atributos para base de datos */
    private String nombreSimulacion;
    private String tipoMatricula;
    private boolean practicaProfesional;
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    private boolean[] priorizaciones;

    @ManyToOne
    @JoinColumn(name = "estudiante_id")
    private Estudiante estudianteId;

    public Proyeccion() {
    }

    public Proyeccion(int semestre, int numMaxCreditos, int numMaxMaterias, String nombreSimulacion, String tipoMatricula, boolean practicaProfesional, boolean[] priorizaciones) {
        this.semestre = semestre;
        this.numMaxCreditos = numMaxCreditos;
        this.numMaxMaterias = numMaxMaterias;
        this.nombreSimulacion = nombreSimulacion;
        this.tipoMatricula = tipoMatricula;
        this.practicaProfesional = practicaProfesional;
        this.fechaCreacion =  LocalDateTime.now();
        this.priorizaciones = priorizaciones;
    }

    public Proyeccion(Long id, int semestre, int numMaxCreditos, int numMaxMaterias, Estudiante estudianteId) {
        this.id = id;
        this.semestre = semestre;
        this.numMaxCreditos = numMaxCreditos;
        this.numMaxMaterias = numMaxMaterias;
        this.estudianteId = estudianteId;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getSemestre() {
        return semestre;
    }

    public void setSemestre(int semestre) {
        this.semestre = semestre;
    }

    public int getNumMaxCreditos() {
        return numMaxCreditos;
    }

    public void setNumMaxCreditos(int numMaxCreditos) {
        this.numMaxCreditos = numMaxCreditos;
    }

    public int getNumMaxMaterias() {
        return numMaxMaterias;
    }

    public void setNumMaxMaterias(int numMaxMaterias) {
        this.numMaxMaterias = numMaxMaterias;
    }

    public Estudiante getEstudianteId() {
        return estudianteId;
    }

    public void setEstudianteId(Estudiante estudianteId) {
        this.estudianteId = estudianteId;
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
        return "Proyeccion [semestre: " + semestre + ", numMaxCreditos: " + numMaxCreditos + ", numMaxMaterias: " + numMaxMaterias  + ", nombreSimulacion: " + nombreSimulacion + ", tipoMatricula: " + tipoMatricula + ", practicaProfesional: " + practicaProfesional + ", fechaCreacion: " + fechaCreacion + ", priorizaciones: " + Arrays.toString(priorizaciones) + "]";
    }

}
