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

    @Column(name = "codigo", unique = true)
    private String codigo;

    @Column(name = "correo", unique = true)
    private String correo;

    private String contrasenia;

    private String primerNombre;

    private String segundoNombre;

    private String primerApellido;

    private String segundoApellido;

    private String carrera;

    private Long anioIngreso;

    @ManyToOne
    @JoinColumn(name = "pensum_id")
    private Pensum pensum;

    @ManyToOne
    @JoinColumn(name = "facultad_id")
    private Facultad facultad;


    public Estudiante() {
    }

    public Estudiante(String codigo, String correo, String contrasenia, String primerNombre, String segundoNombre,
            String primerApellido, String segundoApellido, String carrera, Long anioIngreso, Pensum pensum,
            Facultad facultad) {
        this.codigo = codigo;
        this.correo = correo;
        this.contrasenia = contrasenia;
        this.primerNombre = primerNombre;
        this.segundoNombre = segundoNombre;
        this.primerApellido = primerApellido;
        this.segundoApellido = segundoApellido;
        this.carrera = carrera;
        this.anioIngreso = anioIngreso;
        this.pensum = pensum;
        this.facultad = facultad;
    }

    public Estudiante(Long id, String codigo, String correo, String contrasenia, String primerNombre,
            String segundoNombre, String primerApellido, String segundoApellido, String carrera, Long anioIngreso,
            Pensum pensum, Facultad facultad) {
        this.id = id;
        this.codigo = codigo;
        this.correo = correo;
        this.contrasenia = contrasenia;
        this.primerNombre = primerNombre;
        this.segundoNombre = segundoNombre;
        this.primerApellido = primerApellido;
        this.segundoApellido = segundoApellido;
        this.carrera = carrera;
        this.anioIngreso = anioIngreso;
        this.pensum = pensum;
        this.facultad = facultad;
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

    public String getPrimerNombre() {
        return primerNombre;
    }

    public void setPrimerNombre(String primerNombre) {
        this.primerNombre = primerNombre;
    }

    public String getSegundoNombre() {
        return segundoNombre;
    }

    public void setSegundoNombre(String segundoNombre) {
        this.segundoNombre = segundoNombre;
    }

    public String getPrimerApellido() {
        return primerApellido;
    }

    public void setPrimerApellido(String primerApellido) {
        this.primerApellido = primerApellido;
    }

    public String getSegundoApellido() {
        return segundoApellido;
    }

    public void setSegundoApellido(String segundoApellido) {
        this.segundoApellido = segundoApellido;
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

    public Pensum getPensum() {
        return pensum;
    }

    public void setPensum(Pensum pensum) {
        this.pensum = pensum;
    }

    public Facultad getFacultad() {
        return facultad;
    }

    public void setFacultad(Facultad facultad) {
        this.facultad = facultad;
    }

}
