package com.grupo7.tesis.models;

import java.util.HashSet;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;
import java.util.Objects;

import jakarta.persistence.CascadeType;
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
    @Transient
    @JsonIgnore
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
        // Si el campo @Transient tiene materias, usarlo (caso del algoritmo A*)
        if (this.materias != null && !this.materias.isEmpty()) {
            return this.materias;
        }

        // Si no, construir desde materiasAsociadas (caso de entidad desde BD)
        Set<Materia> materiasFromAsociadas = new HashSet<>();
        if (this.materiasAsociadas != null) {
            for (SimulacionMateria sm : this.materiasAsociadas) {
                materiasFromAsociadas.add(sm.getMateria());
            }
        }
        return materiasFromAsociadas;
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

        // Agregar al campo @Transient (usado por el algoritmo A*)
        this.materias.add(materia);

        // Agregar a la asociación JPA (para persistencia en BD)
        SimulacionMateria asociacion = new SimulacionMateria(this, materia);
        this.materiasAsociadas.add(asociacion);
    }

    public int getTotalCreditos() {
        int total = 0;

        // Usar el campo @Transient si está disponible (algoritmo A*)
        if (this.materias != null && !this.materias.isEmpty()) {
            for (Materia materia : this.materias) {
                total += materia.getCreditos();
            }
            return total;
        }

        // Si no, usar materiasAsociadas (entidad desde BD)
        if (materiasAsociadas != null) {
            for (SimulacionMateria sm : materiasAsociadas) {
                total += sm.getMateria().getCreditos();
            }
        }
        return total;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        Simulacion that = (Simulacion) obj;

        if (this.materias == null && that.materias == null)
            return true;
        if (this.materias == null || that.materias == null)
            return false;
        if (this.materias.size() != that.materias.size())
            return false;

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
        if (materias == null)
            return 0;

        Set<String> codigos = materias.stream()
                .map(Materia::getCodigo)
                .collect(Collectors.toSet());

        return Objects.hash(codigos);
    }

}
