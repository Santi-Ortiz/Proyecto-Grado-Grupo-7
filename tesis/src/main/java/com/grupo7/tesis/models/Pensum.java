package com.grupo7.tesis.models;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "pensum")
public class Pensum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pensum_id", unique = true)
    private Long id;

    private String carrera;

    private Long creditosTotales;

    private Long numeroSemestres;

    @OneToMany(mappedBy = "pensum")
    private Set<PensumMateria> materiasAsociadas;

    public Pensum() {
    }

    public Pensum(String carrera, Long creditosTotales, Long numeroSemestres) {
        this.carrera = carrera;
        this.creditosTotales = creditosTotales;
        this.numeroSemestres = numeroSemestres;
    }

    public Pensum(Long id, String carrera, Long creditosTotales, Long numeroSemestres) {
        this.id = id;
        this.carrera = carrera;
        this.creditosTotales = creditosTotales;
        this.numeroSemestres = numeroSemestres;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCarrera() {
        return carrera;
    }

    public void setCarrera(String carrera) {
        this.carrera = carrera;
    }

    public Long getCreditosTotales() {
        return creditosTotales;
    }

    public void setCreditosTotales(Long creditosTotales) {
        this.creditosTotales = creditosTotales;
    }

    public Long getNumeroSemestres() {
        return numeroSemestres;
    }

    public void setNumeroSemestres(Long numeroSemestres) {
        this.numeroSemestres = numeroSemestres;
    }

    public Set<PensumMateria> getMateriasAsociadas() {
        return materiasAsociadas;
    }

    public void setMateriasAsociadas(Set<PensumMateria> materiasAsociadas) {
        this.materiasAsociadas = materiasAsociadas;
    }

}
