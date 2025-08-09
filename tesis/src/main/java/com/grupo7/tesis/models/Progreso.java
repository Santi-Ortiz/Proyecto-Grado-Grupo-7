package com.grupo7.tesis.models;

import java.util.List;

import com.grupo7.tesis.dtos.*;

import java.util.ArrayList;

public class Progreso {
    private double promedio;
    private int materiasCursadas;
    private int materiasFaltantes;
    private List<Materia> listaMateriasFaltantes;
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
    private int faltanElectivaBasicas;
    private int semestre;
    private double porcentaje;
    private List<MateriaDTO> materias;
    private List<String> lineasRequisitosGrado;
    private List<MateriaDTO> cursosElectivas;
    private List<MateriaDTO> cursosEnfasis;
    private List<MateriaDTO> cursosComplementariaLenguas;
    private List<MateriaDTO> cursosComplementariaInformacion;
    private List<MateriaDTO> cursosIA;
    private List<MateriaDTO> cursosDesarrolloComputacion;
    private List<MateriaDTO> cursosDesarrolloGestion;
    private List<MateriaDTO> cursosComputacionVisual;
    private List<MateriaDTO> cursosCVtoIA;
    private List<MateriaDTO> cursosSIGtoIA;
    private List<MateriaDTO> cursosElectivaBasicas;

    public Progreso(double promedio,
            int materiasCursadas,
            int materiasFaltantes,
            List<Materia> listaMateriasFaltantes,
            int totalMaterias,
            int totalFaltantes,
            int totalCursantes,
            int totalCreditos,
            int creditosCursados,
            int creditosCursando,
            int creditosFaltantes,
            int creditosPensum,
            int creditosExtra,
            int faltanElectiva,
            int faltanComplementaria,
            int faltanEnfasis,
            int faltanElectivaBasicas,
            int semestre,
            List<MateriaDTO> cursosElectivas,
            List<MateriaDTO> cursosEnfasis,
            List<MateriaDTO> cursosComplementariaLenguas,
            List<MateriaDTO> cursosComplementariaInformacion,
            List<MateriaDTO> cursosIA,
            List<MateriaDTO> cursosDesarrolloComputacion,
            List<MateriaDTO> cursosDesarrolloGestion,
            List<MateriaDTO> cursosComputacionVisual,
            List<MateriaDTO> cursosCVtoIA,
            List<MateriaDTO> cursosSIGtoIA,
            List<MateriaDTO> cursosElectivaBasicas) {
        this.promedio = promedio;
        this.materiasCursadas = materiasCursadas;
        this.materiasFaltantes = materiasFaltantes;
        this.listaMateriasFaltantes = listaMateriasFaltantes;
        this.totalMaterias = totalMaterias;
        this.totalFaltantes = totalFaltantes;
        this.totalCursando = totalCursantes;
        this.totalCreditos = totalCreditos;
        this.creditosCursados = creditosCursados;
        this.creditosCursando = creditosCursando;
        this.creditosFaltantes = creditosFaltantes;
        this.creditosPensum = creditosPensum;
        this.creditosExtra = creditosExtra;
        this.faltanElectiva = faltanElectiva;
        this.faltanComplementaria = faltanComplementaria;
        this.faltanEnfasis = faltanEnfasis;
        this.faltanElectivaBasicas = faltanElectivaBasicas;
        this.semestre = semestre;
        this.cursosElectivas = cursosElectivas;
        this.cursosEnfasis = cursosEnfasis;
        this.cursosComplementariaLenguas = cursosComplementariaLenguas;
        this.cursosComplementariaInformacion = cursosComplementariaInformacion;
        this.cursosIA = cursosIA;
        this.cursosDesarrolloComputacion = cursosDesarrolloComputacion;
        this.cursosDesarrolloGestion = cursosDesarrolloGestion;
        this.cursosComputacionVisual = cursosComputacionVisual;
        this.cursosCVtoIA = cursosCVtoIA;
        this.cursosSIGtoIA = cursosSIGtoIA;
        this.cursosElectivaBasicas = cursosElectivaBasicas;
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

    public int getMateriasFaltantes() {
        return materiasFaltantes;
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

    public void setMateriasFaltantes(int materiasFaltantes) {
        this.materiasFaltantes = materiasFaltantes;
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

    public List<MateriaDTO> getCursosComplementariaInformacion() {
        return cursosComplementariaInformacion;
    }

    public void setCursosComplementariaInformacion(List<MateriaDTO> cursosComplementariaInformacion) {
        this.cursosComplementariaInformacion = cursosComplementariaInformacion;
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

    public Progreso copy() {

        List<Materia> nuevaListaMateriasFaltantes = new ArrayList<>();
        if (this.listaMateriasFaltantes != null) {
            for (Materia materia : this.listaMateriasFaltantes) {
                Materia nuevaMateria = new Materia();
                nuevaMateria.setCodigo(materia.getCodigo());
                nuevaMateria.setNombre(materia.getNombre());
                nuevaMateria.setCreditos(materia.getCreditos());
                nuevaMateria.setSemestre(materia.getSemestre());
                nuevaMateria.setTipo(materia.getTipo());
                if (materia.getRequisitos() != null) {
                    nuevaMateria.setRequisitos(new ArrayList<>(materia.getRequisitos()));
                }
                nuevaListaMateriasFaltantes.add(nuevaMateria);
            }
        }

        return new Progreso(
                this.promedio,
                this.materiasCursadas,
                this.materiasFaltantes,
                nuevaListaMateriasFaltantes,
                this.totalMaterias,
                this.totalFaltantes,
                this.totalCursando,
                this.totalCreditos,
                this.creditosCursados,
                this.creditosCursando,
                this.creditosFaltantes,
                this.creditosPensum,
                this.creditosExtra,
                this.faltanElectiva,
                this.faltanComplementaria,
                this.faltanEnfasis,
                this.faltanElectivaBasicas,
                this.semestre,
                this.cursosElectivas,
                this.cursosEnfasis,
                this.cursosComplementariaLenguas,
                this.cursosComplementariaInformacion,
                this.cursosIA,
                this.cursosDesarrolloComputacion,
                this.cursosDesarrolloGestion,
                this.cursosComputacionVisual,
                this.cursosCVtoIA,
                this.cursosSIGtoIA,
                this.cursosElectivaBasicas);
    }

    @Override
    public String toString() {
        return "Progreso [promedio=" + promedio + ", materiasCursadas=" + materiasCursadas + ", materiasFaltantes="
                + materiasFaltantes + ", listaMateriasFaltantes=" + listaMateriasFaltantes + ", totalMaterias="
                + totalMaterias + ", totalFaltantes=" + totalFaltantes + ", totalCursando=" + totalCursando
                + ", totalCreditos=" + totalCreditos + ", creditosCursados=" + creditosCursados + ", creditosCursando="
                + creditosCursando + ", creditosFaltantes=" + creditosFaltantes + ", creditosPensum=" + creditosPensum
                + ", creditosExtra="
                + creditosExtra + ", faltanElectiva=" + faltanElectiva + ", faltanComplementaria="
                + faltanComplementaria + ", faltanEnfasis=" + faltanEnfasis + ", faltanElectivaBasicas="
                + faltanElectivaBasicas + ", semestre=" + semestre + ", porcentaje=" + porcentaje
                + ", materias=" + materias + ", lineasRequisitosGrado=" + lineasRequisitosGrado
                + ", cursosElectivas=" + cursosElectivas + ", cursosEnfasis=" + cursosEnfasis
                + ", cursosComplementariaLenguas=" + cursosComplementariaLenguas
                + ", cursosComplementariaInformacion=" + cursosComplementariaInformacion
                + ", cursosIA=" + cursosIA + ", cursosDesarrolloComputacion=" + cursosDesarrolloComputacion
                + ", cursosDesarrolloGestion=" + cursosDesarrolloGestion + ", cursosComputacionVisual="
                + cursosComputacionVisual
                + ", cursosCVtoIA=" + cursosCVtoIA + ", cursosSIGtoIA=" + cursosSIGtoIA
                + ", cursosElectivaBasicas=" + cursosElectivaBasicas + "]";
    }

}