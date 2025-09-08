package com.grupo7.tesis.models;

import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.Objects;

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
import jakarta.persistence.Transient;

@Entity
@Table(name = "simulacion")
public class Simulacion {
    private Set<Materia> materias;

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

    @Transient
    private double puntajeTotal;

    public Simulacion() {
    }

    public Simulacion(Set<Materia> materias, double puntaje) {
        this.materias = new HashSet<>(materias);
        this.puntajeTotal = puntaje;
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

    public Set<Materia> getMaterias() {
        Set<Materia> materias = new HashSet<>();
        if (this.materiasAsociadas != null) {
            for (SimulacionMateria sm : this.materiasAsociadas) {
                materias.add(sm.getMateria());
            }
        }
        return materias;
    }


    public double getPuntajeTotal() {
        return puntajeTotal;
    }

    public void setMaterias(Set<Materia> materias) {
        this.materias = materias;
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

    public void setPuntajeTotal(double puntajeTotal) {
        this.puntajeTotal = puntajeTotal;
    }

    public void agregarMateria(Materia materia) {
        if (this.materias == null) {
            this.materias = new HashSet<>();
        }
        if (this.materiasAsociadas == null) {
            this.materiasAsociadas = new HashSet<>();
        }
        SimulacionMateria asociacion = new SimulacionMateria(this, materia);
        this.materiasAsociadas.add(asociacion);
    }

    public int getTotalCreditos() {
        int total = 0;
        if (materiasAsociadas != null) {
            for (SimulacionMateria sm : materiasAsociadas) {
                total += sm.getMateria().getCreditos();
            }
        }
        return total;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Simulacion that = (Simulacion) obj;

        if (this.materias == null && that.materias == null) return true;
        if (this.materias == null || that.materias == null) return false;
        if (this.materias.size() != that.materias.size()) return false;
        
        Set<String> thisCodigos = this.materias.stream()
                .map(Materia::getCodigo)
                .collect(Collectors.toSet());
        Set<String> thatCodigos = that.materias.stream()
                .map(Materia::getCodigo)
                .collect(Collectors.toSet());
        
        return thisCodigos.equals(thatCodigos);
    }

    @Override
    public int hashCode() {
        if (materias == null) return 0;

        Set<String> codigos = materias.stream()
                .map(Materia::getCodigo)
                .collect(Collectors.toSet());
        
        return Objects.hash(codigos);
    }

}
