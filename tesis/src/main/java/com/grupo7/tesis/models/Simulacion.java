package com.grupo7.tesis.models;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Simulacion")
public class Simulacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "simulacion_id")
    private Long id;

    private List<Materia> materias;

    @ManyToOne
    @JoinColumn(name = "proyeccion_id")
    private Long proyeccionId;

    
    public Simulacion() {
        this.materias = new ArrayList<>();
    }
    
    public Simulacion(List<Materia> materias, Long proyeccionId) {
        this.materias = materias;
        this.proyeccionId = proyeccionId;
    }

    public Simulacion(Long id, List<Materia> materias, Long proyeccionId) {
        this.id = id;
        this.materias = materias;
        this.proyeccionId = proyeccionId;
    }

    public List<Materia> getMaterias() {
        return materias;
    }

    public void setMaterias(List<Materia> materias) {
        this.materias = materias;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProyeccionId() {
        return proyeccionId;
    }

    public void setProyeccionId(Long proyeccionId) {
        this.proyeccionId = proyeccionId;
    }

    public void agregarMateria(Materia materia) {
        if (this.materias == null) {
            this.materias = new ArrayList<>();
        }
        this.materias.add(materia);
    }

}
