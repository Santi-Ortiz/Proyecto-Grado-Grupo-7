package com.grupo7.tesis.models;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

// ---- Nombre Anterior: MateriaJson ----
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "Materia")
public class Materia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "materia_id", unique = true)
    private Long id;

    private String codigo;

    private String nombre;

    private Integer creditos;

    private int semestre;

    private List<String> requisitos;

    private String requisitosJson;
    
    private String tipo;

    @OneToMany(mappedBy = "materia")
    private Set<PensumMateria> pensumsAsociados;    

    public Materia() {
    }

    

    public Materia(String codigo, String nombre, Integer creditos, int semestre, List<String> requisitos,
            String requisitosJson, String tipo, Set<PensumMateria> pensumsAsociados) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.creditos = creditos;
        this.semestre = semestre;
        this.requisitos = requisitos;
        this.requisitosJson = requisitosJson;
        this.tipo = tipo;
        this.pensumsAsociados = pensumsAsociados;
    }

    public Materia(Long id, String codigo, String nombre, Integer creditos, int semestre, List<String> requisitos,
            String requisitosJson, String tipo, Set<PensumMateria> pensumsAsociados) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.creditos = creditos;
        this.semestre = semestre;
        this.requisitos = requisitos;
        this.requisitosJson = requisitosJson;
        this.tipo = tipo;
        this.pensumsAsociados = pensumsAsociados;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<PensumMateria> getPensumsAsociados() {
        return pensumsAsociados;
    }

    public void setPensumsAsociados(Set<PensumMateria> pensumsAsociados) {
        this.pensumsAsociados = pensumsAsociados;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getCreditos() {
        return creditos;
    }

    public void setCreditos(int creditos) {
        this.creditos = creditos;
    }

    public int getSemestre() {
        return semestre;
    }

    public void setSemestre(int semestre) {
        this.semestre = semestre;
    }

    public List<String> getRequisitos() {
        return requisitos;
    }

    public void setRequisitos(List<String> requisitos) {
        this.requisitos = requisitos;
    }

    public String getRequisitosJson() {
        return requisitosJson;
    }

    public void setRequisitosJson(String requisitosJson) {
        this.requisitosJson = requisitosJson;
    }

    public void setCreditos(Integer creditos) {
        this.creditos = creditos;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

}