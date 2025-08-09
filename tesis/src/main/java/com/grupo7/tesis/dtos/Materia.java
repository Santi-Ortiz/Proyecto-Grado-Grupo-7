package com.grupo7.tesis.dtos;

public class Materia {
    private String cicloLectivo;
    private String materia;
    private String numeroCat;
    private String curso;
    private String titulo;
    private String calif;
    private String cred;
    private String tipo;

    public Materia(String cicloLectivo, String materia, String numeroCat, String curso, String titulo, String calif,
            String cred, String tipo) {
        this.cicloLectivo = cicloLectivo;
        this.materia = materia;
        this.numeroCat = numeroCat;
        this.curso = curso;
        this.titulo = titulo;
        this.calif = calif;
        this.cred = cred;
        this.tipo = tipo;
    }

    public String getCicloLectivo() {
        return cicloLectivo;
    }

    public String getMateria() {
        return materia;
    }

    public String getNumeroCat() {
        return numeroCat;
    }

    public String getCurso() {
        return curso;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getCalif() {
        return calif;
    }

    public String getCred() {
        return cred;
    }

    public String getTipo() {
        return tipo;
    }
}
