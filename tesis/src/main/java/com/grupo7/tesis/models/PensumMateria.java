// En un nuevo archivo: PensumMateria.java
package com.grupo7.tesis.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/* Tabla intermedia entre Pensum y materia para almacenar las materias de un pensum */
@Entity
@Table(name = "pensum_materia")
public class PensumMateria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pensum_id")
    @JsonIgnore
    private Pensum pensum;

    @ManyToOne
    @JoinColumn(name = "materia_id")
    private Materia materia;

    private int semestreEsperado;

    public PensumMateria() {
    }

    public PensumMateria(Pensum pensum, Materia materia, int semestreEsperado) {
        this.pensum = pensum;
        this.materia = materia;
        this.semestreEsperado = semestreEsperado;
    }

    public PensumMateria(Long id, Pensum pensum, Materia materia, int semestreEsperado) {
        this.id = id;
        this.pensum = pensum;
        this.materia = materia;
        this.semestreEsperado = semestreEsperado;
    }

    public Pensum getPensum() {
        return pensum;
    }

    public void setPensum(Pensum pensum) {
        this.pensum = pensum;
    }

    public Materia getMateria() {
        return materia;
    }

    public void setMateria(Materia materia) {
        this.materia = materia;
    }

    public int getSemestreEsperado() {
        return semestreEsperado;
    }

    public void setSemestreEsperado(int semestreEsperado) {
        this.semestreEsperado = semestreEsperado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}