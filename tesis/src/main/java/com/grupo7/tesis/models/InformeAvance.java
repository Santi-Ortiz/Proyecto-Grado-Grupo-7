package com.grupo7.tesis.models;

import java.util.ArrayList;
import java.util.Date;
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
import jakarta.persistence.Lob;
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

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] archivo;

    private Date fechaPublicacion;

    @OneToMany(mappedBy = "informeAvance")
    private Set<InformeAvanceMateria> materiasAsociadas;

    @ManyToOne
    @JoinColumn(name = "estudiante_id")
    private Estudiante estudianteId;

    @ManyToOne
    @JoinColumn(name = "pensum_id")
    private Pensum pensumId;

    public InformeAvance() {
    }

    public InformeAvance(Long id, String nombreArchivo, byte[] archivo, Date fechaPublicacion,
            Set<InformeAvanceMateria> materiasAsociadas, Estudiante estudianteId, Pensum pensumId) {
        this.id = id;
        this.nombreArchivo = nombreArchivo;
        this.archivo = archivo;
        this.fechaPublicacion = fechaPublicacion;
        this.materiasAsociadas = materiasAsociadas;
        this.estudianteId = estudianteId;
        this.pensumId = pensumId;
    }

    public InformeAvance(String nombreArchivo, byte[] archivo, Date fechaPublicacion,
            Set<InformeAvanceMateria> materiasAsociadas, Estudiante estudianteId, Pensum pensumId) {
        this.nombreArchivo = nombreArchivo;
        this.archivo = archivo;
        this.fechaPublicacion = fechaPublicacion;
        this.materiasAsociadas = materiasAsociadas;
        this.estudianteId = estudianteId;
        this.pensumId = pensumId;
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
    
    public Date getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(Date fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public Estudiante getEstudianteId() {
        return estudianteId;
    }

    public void setEstudianteId(Estudiante estudianteId) {
        this.estudianteId = estudianteId;
    }

    public Pensum getPensumId() {
        return pensumId;
    }

    public void setPensumId(Pensum pensumId) {
        this.pensumId = pensumId;
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
