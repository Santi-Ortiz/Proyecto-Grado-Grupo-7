package com.grupo7.tesis.models;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

// ---- Nombre Anterior: MateriaJson ----
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "materia")
public class Materia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "materia_id", unique = true)
    private Long id;

    private String codigo;

    private String nombre;

    private Integer creditos;

    private int semestre;

    @Transient
    private List<String> requisitos;

    @OneToMany(mappedBy = "materia")
    @JsonIgnore
    private Set<RequisitoMateria> requisitosMateria; // Se almacenan los ids de la materias que son requisitos de otras 

    @Transient
    @JsonIgnore
    private String requisitosJson;

    private String tipo;

    @OneToMany(mappedBy = "materia")
    @JsonIgnore
    private Set<PensumMateria> pensumsAsociados;

    @OneToMany(mappedBy = "materia")
    @JsonIgnore
    private Set<SimulacionMateria> simulacionesAsociadas;

    @OneToMany(mappedBy = "materia")
    @JsonIgnore
    private Set<InformeAvanceMateria> informesAsociados;

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

    public Set<SimulacionMateria> getSimulacionesAsociadas() {
        return simulacionesAsociadas;
    }

    public void setSimulacionesAsociadas(Set<SimulacionMateria> simulacionesAsociadas) {
        this.simulacionesAsociadas = simulacionesAsociadas;
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

    public Set<InformeAvanceMateria> getInformesAsociados() {
        return informesAsociados;
    }

    public void setInformesAsociados(Set<InformeAvanceMateria> informesAsociados) {
        this.informesAsociados = informesAsociados;
    }

    public Set<RequisitoMateria> getRequisitosMateria() {
        return requisitosMateria;
    }

    public void setRequisitosMateria(Set<RequisitoMateria> requisitosMateria) {
        this.requisitosMateria = requisitosMateria;
    }

    @JsonProperty("requisitos")
    public List<Long> getRequisitosIds() {
        if (this.requisitosMateria == null || this.requisitosMateria.isEmpty()) {
            return List.of();
        }
        
        return this.requisitosMateria.stream()
            .map(requisitoMateria -> requisitoMateria.getMateriaRequisito().getId())
            .collect(Collectors.toList());
    }

    @JsonProperty("pensumsAsociados")
    public List<Long> getPensumsIds() {
        if (this.pensumsAsociados == null || this.pensumsAsociados.isEmpty()) {
            return List.of();
        }

        return this.pensumsAsociados.stream()
            .map(pensumMateria -> pensumMateria.getPensum().getId())
            .collect(Collectors.toList());
    }

}