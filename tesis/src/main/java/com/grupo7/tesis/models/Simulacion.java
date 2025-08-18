package com.grupo7.tesis.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "simulacion")
public class Simulacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "simulacion_id")
    private Long id;

    @OneToMany(mappedBy = "simulacion")
    private Set<SimulacionMateria> materiasAsociadas;

    private Long semestre; // Representa el semestre al que se está simulando

    private Long creditosTotales; // Representa la cantidad de creditos que contiene la simulación 

    @ManyToOne
    @JoinColumn(name = "proyeccion_id")
    private Proyeccion proyeccionId;

    public Simulacion() {
        this.materiasAsociadas = new HashSet<>();
    }

    public Simulacion(Proyeccion proyeccionId) {
        this();
        this.proyeccionId = proyeccionId;
    }

    public Simulacion(Long id, Proyeccion proyeccionId) {
        this();
        this.id = id;
        this.proyeccionId = proyeccionId;
    }

    public List<Materia> getMaterias() {
        List<Materia> materias = new ArrayList<>();
        if (this.materiasAsociadas != null) {
            for (SimulacionMateria sm : this.materiasAsociadas) {
                materias.add(sm.getMateria());
            }
        }
        return materias;
    }

    public Set<SimulacionMateria> getMateriasAsociadas() {
        return materiasAsociadas;
    }

    public void setMateriasAsociadas(Set<SimulacionMateria> materiasAsociadas) {
        this.materiasAsociadas = materiasAsociadas;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Proyeccion getProyeccionId() {
        return proyeccionId;
    }

    public void setProyeccionId(Proyeccion proyeccionId) {
        this.proyeccionId = proyeccionId;
    }

    public Long getSemestre() {
        return semestre;
    }

    public void setSemestre(Long semestre) {
        this.semestre = semestre;
    }

    public Long getCreditosTotales() {
        return creditosTotales;
    }

    public void setCreditosTotales(Long creditosTotales) {
        this.creditosTotales = creditosTotales;
    }

    public void agregarMateria(Materia materia) {
        if (this.materiasAsociadas == null) {
            this.materiasAsociadas = new HashSet<>();
        }
        SimulacionMateria asociacion = new SimulacionMateria(this, materia);
        this.materiasAsociadas.add(asociacion);
    }

}
