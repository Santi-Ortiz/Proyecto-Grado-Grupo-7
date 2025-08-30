package com.grupo7.tesis.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "informe_avance")
public class InformeAvance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "informe_avance_id")
    private Long id;

    private String nombreArchivo;

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "archivo", columnDefinition = "BYTEA")
    private byte[] archivo;

    private LocalDate fechaPublicacion;

    @OneToMany(mappedBy = "informeAvance")
    private Set<InformeAvanceMateria> materiasAsociadas;

    @ManyToOne
    @JoinColumn(name = "estudiante_id")
    private Estudiante estudiante;

    @ManyToOne
    @JoinColumn(name = "pensum_id")
    private Pensum pensum;

    public InformeAvance() {
    }

    public InformeAvance(Long id, String nombreArchivo, byte[] archivo, LocalDate fechaPublicacion,
            Set<InformeAvanceMateria> materiasAsociadas, Estudiante estudiante, Pensum pensum) {
        this.id = id;
        this.nombreArchivo = nombreArchivo;
        this.archivo = archivo;
        this.fechaPublicacion = fechaPublicacion;
        this.materiasAsociadas = materiasAsociadas;
        this.estudiante = estudiante;
        this.pensum = pensum;
    }

    public InformeAvance(String nombreArchivo, byte[] archivo, LocalDate fechaPublicacion,
            Set<InformeAvanceMateria> materiasAsociadas, Estudiante estudiante, Pensum pensum) {
        this.nombreArchivo = nombreArchivo;
        this.archivo = archivo;
        this.fechaPublicacion = fechaPublicacion;
        this.materiasAsociadas = materiasAsociadas;
        this.estudiante = estudiante;
        this.pensum = pensum;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public byte[] getArchivo() {
        return archivo;
    }

    public void setArchivo(byte[] archivo) {
        this.archivo = archivo;
    }

    public LocalDate getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(LocalDate fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(Estudiante estudiante) {
        this.estudiante = estudiante;
    }

    public Pensum getPensum() {
        return pensum;
    }

    public void setPensum(Pensum pensum) {
        this.pensum = pensum;
    }

    public Set<InformeAvanceMateria> getMateriasAsociadas() {
        return materiasAsociadas;
    }

    public void setMateriasAsociadas(Set<InformeAvanceMateria> materiasAsociadas) {
        this.materiasAsociadas = materiasAsociadas;
    }

    public List<Materia> getMaterias() {
        List<Materia> materias = new ArrayList<>();

        if (this.materiasAsociadas != null) {
            for (InformeAvanceMateria iam : this.materiasAsociadas) {
                materias.add(iam.getMateria());
            }
        }

        return materias;
    }
}
