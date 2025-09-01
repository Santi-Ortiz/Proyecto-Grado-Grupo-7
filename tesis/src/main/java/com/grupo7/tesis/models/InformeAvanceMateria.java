package com.grupo7.tesis.models;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

/* Tabla intermedia entre InformeAvance y Materia para relacionar la lista de materias dentro de un informe de avance */
@Entity
@Table(name = "informe_avance_materia")
public class InformeAvanceMateria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "informe_avance_id")
    private InformeAvance informeAvance;

    @ManyToOne
    @JoinColumn(name = "materia_id")
    private Materia materia;

    private Double nota;

    public InformeAvanceMateria() {
    }

    public InformeAvanceMateria(InformeAvance informeAvance, Materia materia, Double nota) {
        this.informeAvance = informeAvance;
        this.materia = materia;
        this.nota = nota;
    }

    public InformeAvanceMateria(Long id, InformeAvance informeAvance, Materia materia, Double nota) {
        this.id = id;
        this.informeAvance = informeAvance;
        this.materia = materia;
        this.nota = nota;
    }

    
    public InformeAvance getInformeAvance() {
        return informeAvance;
    }

    public void setInformeAvance(InformeAvance informeAvance) {
        this.informeAvance = informeAvance;
    }

    public Materia getMateria() {
        return materia;
    }

    public void setMateria(Materia materia) {
        this.materia = materia;
    }

    public Double getNota() {
        return nota;
    }

    public void setNota(Double nota) {
        this.nota = nota;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}