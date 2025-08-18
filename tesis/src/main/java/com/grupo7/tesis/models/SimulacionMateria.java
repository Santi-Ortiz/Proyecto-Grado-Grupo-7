package com.grupo7.tesis.models;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

/* Tabla intermedia entre Simulación y Materia para almacenar las simulaciones con sus respectivas materias */
@Entity
@Table(name = "simulacion_materia")
public class SimulacionMateria {

    @EmbeddedId
    private SimulacionMateriaId id;

    @ManyToOne
    @MapsId("simulacionId")
    @JoinColumn(name = "simulacion_id")
    private Simulacion simulacion;

    @ManyToOne
    @MapsId("materiaId")
    @JoinColumn(name = "materia_id")
    private Materia materia;

    public SimulacionMateria() {
    }

    public SimulacionMateria(Simulacion simulacion, Materia materia) {
        this.simulacion = simulacion;
        this.materia = materia;
        this.id = new SimulacionMateriaId(simulacion.getId(), materia.getId());
    }

    public SimulacionMateriaId getId() {
        return id;
    }

    public void setId(SimulacionMateriaId id) {
        this.id = id;
    }

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
}
