package com.grupo7.tesis.dtos;

public class LoginDTO {
    private String correo;
    private String contrasenia;

    public LoginDTO() {
    }

    public LoginDTO(String correo, String contrasenia) {
        this.correo = correo;
        this.contrasenia = contrasenia;
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
}
