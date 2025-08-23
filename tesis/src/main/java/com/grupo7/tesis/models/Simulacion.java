package com.grupo7.tesis.models;

import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.Objects;

public class Simulacion {
    private Set<Materia> materias;
    private double puntajeTotal;

    public Simulacion() {
    }

    public Simulacion(Set<Materia> materias, double puntaje) {
        this.materias = new HashSet<>(materias);
        this.puntajeTotal = puntaje;
    }

    public Set<Materia> getMaterias() {
        return materias;
    }

    public double getPuntajeTotal() {
        return puntajeTotal;
    }

    public void setMaterias(Set<Materia> materias) {
        this.materias = materias;
    }

    public void setPuntajeTotal(double puntajeTotal) {
        this.puntajeTotal = puntajeTotal;
    }

    public void agregarMateria(Materia materia) {
        if (this.materias == null) {
            this.materias = new HashSet<>();
        }
        this.materias.add(materia);
    }

    public int getTotalCreditos() {
        int total = 0;
        if (materias != null) {
            for (Materia materia : materias) {
                total += materia.getCreditos();
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
