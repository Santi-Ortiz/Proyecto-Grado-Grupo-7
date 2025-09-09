package com.grupo7.tesis.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/* Tabla intermedia entre Simulación y Materia para almacenar las simulaciones con sus respectivas materias */
@Entity
@Table(name = "simulacion_materia")
public class SimulacionMateria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "simulacion_id")
    @JsonIgnore
    private Simulacion simulacion;

    @ManyToOne
    @JoinColumn(name = "materia_id")
    private Materia materia;

    public SimulacionMateria() {
    }

    public SimulacionMateria(Long id, Simulacion simulacion, Materia materia) {
        this.id = id;
        this.simulacion = simulacion;
        this.materia = materia;
    }

    public SimulacionMateria(Simulacion simulacion, Materia materia) {
        this.simulacion = simulacion;
        this.materia = materia;
    }

    @JsonIgnore
    public Simulacion getSimulacion() {
        return simulacion;
    }

    public void setSimulacion(Simulacion simulacion) {
        this.simulacion = simulacion;
    }

    public Materia getMateria() {
        return materia;
    }

    public void setMateria(Materia materia) {
        this.materia = materia;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
