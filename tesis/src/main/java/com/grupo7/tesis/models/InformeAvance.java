package com.grupo7.tesis.models;

import java.io.File;

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
import jakarta.persistence.Table;

@Entity
@Table(name = "InformeAvance")
public class InformeAvance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "informe_avance_id")
    private Long id;

    private String nombreArchivo;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] archivo;

    @ManyToOne
    @JoinColumn(name = "estudiante_id")
    private Long estudianteId;

    @ManyToOne
    @JoinColumn(name = "pensum_id")
    private Long pensumId;

    public InformeAvance() {
    }

    public InformeAvance(String nombreArchivo, byte[] archivo, Long estudianteId, Long pensumId) {
        this.nombreArchivo = nombreArchivo;
        this.archivo = archivo;
        this.estudianteId = estudianteId;
        this.pensumId = pensumId;
    }

    public InformeAvance(Long id, String nombreArchivo, byte[] archivo, Long estudianteId, Long pensumId) {
        this.id = id;
        this.nombreArchivo = nombreArchivo;
        this.archivo = archivo;
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

    public Long getEstudianteId() {
        return estudianteId;
    }

    public void setEstudianteId(Long estudianteId) {
        this.estudianteId = estudianteId;
    }

    public Long getPensumId() {
        return pensumId;
    }

    public void setPensumId(Long pensumId) {
        this.pensumId = pensumId;
    }

}
