package com.grupo7.tesis.dtos;

public class RegisterDTO {
    private String codigo;
    private String correo;
    private String carrera;
    private String contrasenia;
    private Long anioIngreso;
    private String primerNombre;
    private String segundoNombre;
    private String primerApellido;
    private String segundoApellido;

    public RegisterDTO() {
    }

    public RegisterDTO(String codigo,String correo, String carrera, String contrasenia, Long anioIngreso, String primerNombre, String segundoNombre,
            String primerApellido, String segundoApellido) {
        this.codigo = codigo;
        this.correo = correo;
        this.carrera = carrera;
        this.contrasenia = contrasenia;
        this.anioIngreso = anioIngreso;
        this.primerNombre = primerNombre;
        this.segundoNombre = segundoNombre;
        this.primerApellido = primerApellido;
        this.segundoApellido = segundoApellido;
    }

    public RegisterDTO(String codigo,String correo, String carrera, String contrasenia, Long anioIngreso, String primerNombre, String primerApellido,
            String segundoApellido) {
        this.codigo = codigo;
        this.correo = correo;
        this.carrera = carrera;
        this.contrasenia = contrasenia;
        this.anioIngreso = anioIngreso;
        this.primerNombre = primerNombre;
        this.primerApellido = primerApellido;
        this.segundoApellido = segundoApellido;
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

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
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

}
