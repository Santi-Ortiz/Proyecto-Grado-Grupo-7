// En un nuevo archivo: PensumMateria.java
package com.grupo7.tesis.models;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "PensumMateria")
public class PensumMateria {

    @EmbeddedId
    private PensumMateriaId id;

    @ManyToOne
    @MapsId("pensumId") // Mapea el atributo 'pensumId' de PensumMateriaId
    @JoinColumn(name = "pensum_id")
    private Pensum pensum;

    @ManyToOne
    @MapsId("materiaId") // Mapea el atributo 'materiaId' de PensumMateriaId
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

    public PensumMateria(PensumMateriaId id, Pensum pensum, Materia materia, int semestreEsperado) {
        this.id = id;
        this.pensum = pensum;
        this.materia = materia;
        this.semestreEsperado = semestreEsperado;
    }

    public PensumMateriaId getId() {
        return id;
    }

    public void setId(PensumMateriaId id) {
        this.id = id;
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

    

}