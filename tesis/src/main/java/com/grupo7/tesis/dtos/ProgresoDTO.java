package com.grupo7.tesis.dtos;

import com.grupo7.tesis.models.Materia;
import com.grupo7.tesis.models.Progreso;

import java.util.List;

public class ProgresoDTO {

    private double promedio;
    private List<Materia> listaMateriasFaltantes;
    private int materiasCursadas;
    private int totalMaterias;
    private int totalFaltantes;
    private int totalCursando;
    private int totalCreditos;
    private int creditosCursados;
    private int creditosCursando;
    private int creditosFaltantes;
    private int creditosPensum;
    private int creditosExtra;
    private int faltanElectiva;
    private int faltanComplementaria;
    private int faltanEnfasis;
    private int faltanEnfasisTesis;
    private int faltanElectivaBasicas;
    private int semestre;
    private double porcentaje;
    private List<MateriaDTO> materias;
    private List<String> lineasRequisitosGrado;
    private List<MateriaDTO> cursosElectivas;
    private List<MateriaDTO> cursosEnfasis;
    private List<MateriaDTO> cursosComplementariaLenguas;
    private List<MateriaDTO> cursosIA;
    private List<MateriaDTO> cursosDesarrolloComputacion;
    private List<MateriaDTO> cursosDesarrolloGestion;
    private List<MateriaDTO> cursosComputacionVisual;
    private List<MateriaDTO> cursosCVtoIA;
    private List<MateriaDTO> cursosSIGtoIA;
    private List<MateriaDTO> cursosElectivaBasicas;
    private List<MateriaDTO> cursosSeguridad;

    public ProgresoDTO(Progreso progreso) {
        this.materias = progreso.getMaterias();
        this.lineasRequisitosGrado = progreso.getLineasRequisitosGrado();
        this.cursosElectivas = progreso.getCursosElectivas();
        this.cursosEnfasis = progreso.getCursosEnfasis();
        this.cursosComplementariaLenguas = progreso.getCursosComplementariaLenguas();
        this.cursosIA = progreso.getCursosIA();
        this.cursosDesarrolloComputacion = progreso.getCursosDesarrolloComputacion();
        this.cursosDesarrolloGestion = progreso.getCursosDesarrolloGestion();
        this.cursosComputacionVisual = progreso.getCursosComputacionVisual();
        this.cursosCVtoIA = progreso.getCursosCVtoIA();
        this.cursosSIGtoIA = progreso.getCursosSIGtoIA();
        this.cursosElectivaBasicas = progreso.getCursosElectivaBasicas();
        this.cursosSeguridad = progreso.getCursosSeguridad();
        this.listaMateriasFaltantes = progreso.getMateriasFaltantes();

        this.promedio = progreso.getPromedio();
        this.materiasCursadas = progreso.getMateriasCursadas().size();
        this.totalMaterias = progreso.getTotalMaterias().size();
        this.totalFaltantes = progreso.getListaMateriasFaltantes().size();
        this.totalCursando = progreso.getTotalCursando();
        this.totalCreditos = progreso.getTotalCreditos();
        this.creditosCursados = progreso.getCreditosCursados();
        this.creditosCursando = progreso.getCreditosCursando();
        this.creditosFaltantes = progreso.getCreditosFaltantes() + progreso.getFaltanElectiva() + progreso.getFaltanComplementaria() + progreso.getFaltanEnfasis() + progreso.getFaltanElectivaBasicas() + progreso.getMateriasNucleoCursando() + progreso.getMateriasEnfasisCursando() + progreso.getFaltanEnfasisTesis();
        this.creditosPensum = progreso.getCreditosPensum();
        this.creditosExtra = progreso.getCreditosExtra();
        this.faltanElectiva = progreso.getFaltanElectiva();
        this.faltanComplementaria = progreso.getFaltanComplementaria();
        this.faltanEnfasis = progreso.getFaltanEnfasis() + progreso.getMateriasEnfasisCursando() + progreso.getFaltanEnfasisTesis();
        this.faltanElectivaBasicas = progreso.getFaltanElectivaBasicas();
        this.semestre = progreso.getSemestre();
        this.porcentaje = progreso.getPorcentaje();
    }

    public List<MateriaDTO> getMaterias() {
        return materias;
    }

    public void setMaterias(List<MateriaDTO> materias) {
        this.materias = materias;
    }

    public double getPromedio() {
        return promedio;
    }

    public int getMateriasCursadas() {
        return materiasCursadas;
    }

    public List<Materia> getListaMateriasFaltantes() {
        return listaMateriasFaltantes;
    }

    public int getTotalMaterias() {
        return totalMaterias;
    }

    public int getTotalFaltantes() {
        return totalFaltantes;
    };

    public int getMateriasCursando() {
        return totalCursando;
    }

    public int getTotalCreditos() {
        return totalCreditos;
    }

    public int getCreditosCursados() {
        return creditosCursados;
    }

    public int getCreditosCursando() {
        return creditosCursando;
    }

    public int getCreditosFaltantes() {
        return creditosFaltantes;
    }

    public int getCreditosPensum() {
        return creditosPensum;
    }

    public int getCreditosExtra() {
        return creditosExtra;
    }

    public int getFaltanElectiva() {
        return faltanElectiva;
    }

    public int getFaltanComplementaria() {
        return faltanComplementaria;
    }

    public int getFaltanEnfasis() {
        return faltanEnfasis;
    }

    public int getFaltanElectivaBasicas() {
        return faltanElectivaBasicas;
    }

    public int getFaltanEnfasisTesis() {
        return faltanEnfasisTesis;
    }

    public int getSemestre() {
        return semestre;
    }

    public double getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(double porcentaje) {
        this.porcentaje = porcentaje;
    }

    public List<String> getLineasRequisitosGrado() {
        return lineasRequisitosGrado;
    }

    public void setLineasRequisitosGrado(List<String> lineasRequisitosGrado) {
        this.lineasRequisitosGrado = lineasRequisitosGrado;
    }

    public void setPromedio(double promedio) {
        this.promedio = promedio;
    }

    public void setMateriasCursadas(int materiasCursadas) {
        this.materiasCursadas = materiasCursadas;
    }

    public void setListaMateriasFaltantes(List<Materia> listaMateriasFaltantes) {
        this.listaMateriasFaltantes = listaMateriasFaltantes;
    }

    public void setTotalMaterias(int totalMaterias) {
        this.totalMaterias = totalMaterias;
    }

    public void setTotalFaltantes(int totalFaltantes) {
        this.totalFaltantes = totalFaltantes;
    }

    public void setTotalCursando(int totalCursando) {
        this.totalCursando = totalCursando;
    }

    public void setTotalCreditos(int totalCreditos) {
        this.totalCreditos = totalCreditos;
    }

    public void setCreditosCursados(int creditosCursados) {
        this.creditosCursados = creditosCursados;
    }

    public void setCreditosCursando(int creditosCursando) {
        this.creditosCursando = creditosCursando;
    }

    public void setCreditosFaltantes(int creditosFaltantes) {
        this.creditosFaltantes = creditosFaltantes;
    }

    public void setCreditosPensum(int creditosPensum) {
        this.creditosPensum = creditosPensum;
    }

    public void setCreditosExtra(int creditosExtra) {
        this.creditosExtra = creditosExtra;
    }

    public void setFaltanElectiva(int faltanElectiva) {
        this.faltanElectiva = faltanElectiva;
    }

    public void setFaltanComplementaria(int faltanComplementaria) {
        this.faltanComplementaria = faltanComplementaria;
    }

    public void setFaltanEnfasis(int faltanEnfasis) {
        this.faltanEnfasis = faltanEnfasis;
    }

    public void setFaltanElectivaBasicas(int faltanElectivaBasicas) {
        this.faltanElectivaBasicas = faltanElectivaBasicas;
    }

    public void setFaltanEnfasisTesis(int faltanEnfasisTesis) {
        this.faltanEnfasisTesis = faltanEnfasisTesis;
    }

    public void setSemestre(int semestre) {
        this.semestre = semestre;
    }

    public List<MateriaDTO> getCursosElectivas() {
        return cursosElectivas;
    }

    public void setCursosElectivas(List<MateriaDTO> cursosElectivas) {
        this.cursosElectivas = cursosElectivas;
    }

    public List<MateriaDTO> getCursosEnfasis() {
        return cursosEnfasis;
    }

    public void setCursosEnfasis(List<MateriaDTO> cursosEnfasis) {
        this.cursosEnfasis = cursosEnfasis;
    }

    public List<MateriaDTO> getCursosComplementariaLenguas() {
        return cursosComplementariaLenguas;
    }

    public void setCursosComplementariaLenguas(List<MateriaDTO> cursosComplementariaLenguas) {
        this.cursosComplementariaLenguas = cursosComplementariaLenguas;
    }

    public List<MateriaDTO> getCursosIA() {
        return cursosIA;
    }

    public void setCursosIA(List<MateriaDTO> cursosIA) {
        this.cursosIA = cursosIA;
    }

    public List<MateriaDTO> getCursosDesarrolloComputacion() {
        return cursosDesarrolloComputacion;
    }

    public void setCursosDesarrolloComputacion(List<MateriaDTO> cursosDesarrolloComputacion) {
        this.cursosDesarrolloComputacion = cursosDesarrolloComputacion;
    }

    public List<MateriaDTO> getCursosDesarrolloGestion() {
        return cursosDesarrolloGestion;
    }

    public void setCursosDesarrolloGestion(List<MateriaDTO> cursosDesarrolloGestion) {
        this.cursosDesarrolloGestion = cursosDesarrolloGestion;
    }

    public List<MateriaDTO> getCursosComputacionVisual() {
        return cursosComputacionVisual;
    }

    public void setCursosComputacionVisual(List<MateriaDTO> cursosComputacionVisual) {
        this.cursosComputacionVisual = cursosComputacionVisual;
    }

    public List<MateriaDTO> getCursosCVtoIA() {
        return cursosCVtoIA;
    }

    public void setCursosCVtoIA(List<MateriaDTO> cursosCVtoIA) {
        this.cursosCVtoIA = cursosCVtoIA;
    }

    public List<MateriaDTO> getCursosSIGtoIA() {
        return cursosSIGtoIA;
    }

    public void setCursosSIGtoIA(List<MateriaDTO> cursosSIGtoIA) {
        this.cursosSIGtoIA = cursosSIGtoIA;
    }

    public List<MateriaDTO> getCursosElectivaBasicas() {
        return cursosElectivaBasicas;
    }

    public void setCursosElectivaBasicas(List<MateriaDTO> cursosElectivaBasicas) {
        this.cursosElectivaBasicas = cursosElectivaBasicas;
    }

    public List<MateriaDTO> getCursosSeguridad() {
        return cursosSeguridad;
    }

    public void setCursosSeguridad(List<MateriaDTO> cursosSeguridad) {
        this.cursosSeguridad = cursosSeguridad;
    }

}
