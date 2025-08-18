package com.grupo7.tesis.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class InformeAvanceMateriaId implements Serializable {

    @Column(name = "informe_avance_id")
    private Long informeAvanceId;

    @Column(name = "materia_id")
    private Long materiaId;

    public InformeAvanceMateriaId() {
    }

    public InformeAvanceMateriaId(Long informeAvanceId, Long materiaId) {
        this.informeAvanceId = informeAvanceId;
        this.materiaId = materiaId;
    }

    public Long getInformeAvanceId() {
        return informeAvanceId;
    }

    public void setInformeAvanceId(Long informeAvanceId) {
        this.informeAvanceId = informeAvanceId;
    }

    public Long getMateriaId() {
        return materiaId;
    }

    public void setMateriaId(Long materiaId) {
        this.materiaId = materiaId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InformeAvanceMateriaId that = (InformeAvanceMateriaId) o;
        return Objects.equals(informeAvanceId, that.informeAvanceId) &&
               Objects.equals(materiaId, that.materiaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(informeAvanceId, materiaId);
    }
}