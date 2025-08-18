package com.grupo7.tesis.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "estudiante")
public class Estudiante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "estudiante_id")
    private Long id;

    private String codigo;

    private String correo;

    private String contrasenia;

    private String nombre;

    private String carrera;

    private Long anioIngreso;

    @ManyToOne
    @JoinColumn(name = "pensum_id")
    private Pensum pensumId;

    @ManyToOne
    @JoinColumn(name = "facultad_id")
    private Facultad facultadId;


    public Estudiante() {
    }

    public Estudiante(String codigo, String correo, String contrasenia, String nombre, String carrera, Long anioIngreso,
            Pensum pensumId, Facultad facultadId) {
        this.codigo = codigo;
        this.correo = correo;
        this.contrasenia = contrasenia;
        this.nombre = nombre;
        this.carrera = carrera;
        this.anioIngreso = anioIngreso;
        this.pensumId = pensumId;
        this.facultadId = facultadId;
    }

    public Estudiante(Long id, String codigo, String correo, String contrasenia, String nombre, String carrera,
            Long anioIngreso, Pensum pensumId, Facultad facultadId) {
        this.id = id;
        this.codigo = codigo;
        this.correo = correo;
        this.contrasenia = contrasenia;
        this.nombre = nombre;
        this.carrera = carrera;
        this.anioIngreso = anioIngreso;
        this.pensumId = pensumId;
        this.facultadId = facultadId;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasenia() {
        return contrasenia;
    }

    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCarrera() {
        return carrera;
    }

    public void setCarrera(String carrera) {
        this.carrera = carrera;
    }

    public Long getAnioIngreso() {
        return anioIngreso;
    }

    public void setAnioIngreso(Long anioIngreso) {
        this.anioIngreso = anioIngreso;
    }

    public Pensum getPensumId() {
        return pensumId;
    }

    public void setPensumId(Pensum pensumId) {
        this.pensumId = pensumId;
    }

    public Facultad getFacultadId() {
        return facultadId;
    }

    public void setFacultadId(Facultad facultadId) {
        this.facultadId = facultadId;
    }

}
