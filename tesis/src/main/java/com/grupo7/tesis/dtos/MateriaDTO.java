package com.grupo7.tesis.dtos;


// ---- Nombre anterior: Materia ----
public class MateriaDTO {
    private String cicloLectivo;
    private String materia;
    private String numeroCat;
    private String curso;
    private String titulo;
    private String calif;
    private String cred;
    private String tipo;

    public MateriaDTO(String cicloLectivo, String materia, String numeroCat, String curso, String titulo, String calif,
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

    MateriaDTO () {
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

    public void setCicloLectivo(String cicloLectivo) {
        this.cicloLectivo = cicloLectivo;
    }

    public void setMateria(String materia) {
        this.materia = materia;
    }

    public void setNumeroCat(String numeroCat) {
        this.numeroCat = numeroCat;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setCalif(String calif) {
        this.calif = calif;
    }

    public void setCred(String cred) {
        this.cred = cred;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
