package com.grupo7.tesis.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class SimulacionMateriaId implements Serializable {

    @Column(name = "simulacion_id")
    private Long simulacionId;

    @Column(name = "materia_id")
    private Long materiaId;

    public SimulacionMateriaId() {
    }

    public SimulacionMateriaId(Long simulacionId, Long materiaId) {
        this.simulacionId = simulacionId;
        this.materiaId = materiaId;
    }

    public Long getSimulacionId() {
        return simulacionId;
    }

    public void setSimulacionId(Long simulacionId) {
        this.simulacionId = simulacionId;
    }

    public Long getMateriaId() {
        return materiaId;
    }

    public void setMateriaId(Long materiaId) {
        this.materiaId = materiaId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SimulacionMateriaId that = (SimulacionMateriaId) o;
        return Objects.equals(simulacionId, that.simulacionId) &&
                Objects.equals(materiaId, that.materiaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(simulacionId, materiaId);
    }
}
