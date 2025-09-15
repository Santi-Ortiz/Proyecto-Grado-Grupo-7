package com.grupo7.tesis.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "requisito_materia")
public class RequisitoMateria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "materia_id")
    private Materia materia;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "requisito_materia_id")
    private Materia materiaRequisito;

    public RequisitoMateria() {}
    
    public RequisitoMateria(Long id, Materia materia, Materia materiaRequisito) {
        this.id = id;
        this.materia = materia;
        this.materiaRequisito = materiaRequisito;
    }

    public RequisitoMateria(Materia materia, Materia materiaRequisito) {
        this.materia = materia;
        this.materiaRequisito = materiaRequisito;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Materia getMateria() {
        return materia;
    }

    public void setMateria(Materia materia) {
        this.materia = materia;
    }

    public Materia getMateriaRequisito() {
        return materiaRequisito;
    }

    public void setMateriaRequisito(Materia materiaRequisito) {
        this.materiaRequisito = materiaRequisito;
    }
  
}
