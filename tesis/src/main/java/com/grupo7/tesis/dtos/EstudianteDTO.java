package com.grupo7.tesis.dtos;

public class EstudianteDTO {
    private String codigo;
    private String correo;
    private String contrasenia;
    private String primerNombre;
    private String segundoNombre;
    private String primerApellido;
    private String segundoApellido;
    private String carrera;
    private Long anioIngreso;
    private Long pensumId;
    private Long facultadId;

    public EstudianteDTO() {
    }

    public EstudianteDTO(String codigo, String correo, String contrasenia, String primerNombre, String segundoNombre,
            String primerApellido, String segundoApellido, String carrera, Long anioIngreso) {
        this.codigo = codigo;
        this.correo = correo;
        this.contrasenia = contrasenia;
        this.primerNombre = primerNombre;
        this.segundoNombre = segundoNombre;
        this.primerApellido = primerApellido;
        this.segundoApellido = segundoApellido;
        this.carrera = carrera;
        this.anioIngreso = anioIngreso;
    }

    public EstudianteDTO(String codigo, String correo, String contrasenia, String primerNombre, String primerApellido,
            String segundoApellido, String carrera, Long anioIngreso) {
        this.codigo = codigo;
        this.correo = correo;
        this.contrasenia = contrasenia;
        this.primerNombre = primerNombre;
        this.primerApellido = primerApellido;
        this.segundoApellido = segundoApellido;
        this.carrera = carrera;
        this.anioIngreso = anioIngreso;
    }

    public EstudianteDTO(String codigo, String correo, String contrasenia, String primerNombre,
            String segundoNombre, String primerApellido, String segundoApellido, String carrera, Long anioIngreso,
            Long pensumId, Long facultadId) {
        this.codigo = codigo;
        this.correo = correo;
        this.contrasenia = contrasenia;
        this.primerNombre = primerNombre;
        this.segundoNombre = segundoNombre;
        this.primerApellido = primerApellido;
        this.segundoApellido = segundoApellido;
        this.carrera = carrera;
        this.anioIngreso = anioIngreso;
        this.pensumId = pensumId;
        this.facultadId = facultadId;
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

    public Long getPensumId() {
        return pensumId;
    }

    public void setPensumId(Long pensumId) {
        this.pensumId = pensumId;
    }

    public Long getFacultadId() {
        return facultadId;
    }

    public void setFacultadId(Long facultadId) {
        this.facultadId = facultadId;
    }
}
