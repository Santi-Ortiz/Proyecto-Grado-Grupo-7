// En un nuevo archivo: PensumMateriaId.java
package com.grupo7.tesis.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PensumMateriaId implements Serializable {

    @Column(name = "pensum_id")
    private Long pensumId;

    @Column(name = "materia_id")
    private Long materiaId;

    public PensumMateriaId() {
    }

    public PensumMateriaId(Long pensumId, Long materiaId) {
        this.pensumId = pensumId;
        this.materiaId = materiaId;
    }

    public Long getPensumId() {
        return pensumId;
    }

    public void setPensumId(Long pensumId) {
        this.pensumId = pensumId;
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
        PensumMateriaId that = (PensumMateriaId) o;
        return Objects.equals(pensumId, that.pensumId) &&
               Objects.equals(materiaId, that.materiaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pensumId, materiaId);
    }
}