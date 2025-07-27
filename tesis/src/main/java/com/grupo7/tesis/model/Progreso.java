package com.grupo7.tesis.model;

import java.util.List;
import java.util.ArrayList;

public class Progreso {
    private double promedio;
    private int materiasCursadas;
    private int materiasFaltantes;
    private List<MateriaJson> listaMateriasFaltantes;
    private int totalMaterias;
    private int totalFaltantes;
    private int totalCursando;
    private int totalCreditos;
    private int creditosPensum;
    private int creditosExtra;
    private int faltanElectiva;
    private int faltanComplementaria;
    private int faltanEnfasis;
    private int faltanElectivaBasicas;
    private int semestre;
    private double porcentaje;
    private List<Materia> materias;
    private List<String> lineasRequisitosGrado;

    public Progreso(double promedio, int materiasCursadas, int materiasFaltantes,
            List<MateriaJson> listaMateriasFaltantes, int totalMaterias, int totalFaltantes, int totalCursantes,
            int totalCreditos, int creditosPensum, int creditosExtra, int faltanElectiva, int faltanComplementaria,
            int faltanEnfasis, int faltanElectivaBasicas, int semestre) {
        this.promedio = promedio;
        this.materiasCursadas = materiasCursadas;
        this.materiasFaltantes = materiasFaltantes;
        this.listaMateriasFaltantes = listaMateriasFaltantes;
        this.totalMaterias = totalMaterias;
        this.totalFaltantes = totalFaltantes;
        this.totalCursando = totalCursantes;
        this.totalCreditos = totalCreditos;
        this.creditosPensum = creditosPensum;
        this.creditosExtra = creditosExtra;
        this.faltanElectiva = faltanElectiva;
        this.faltanComplementaria = faltanComplementaria;
        this.faltanEnfasis = faltanEnfasis;
        this.faltanElectivaBasicas = faltanElectivaBasicas;
        this.semestre = semestre;
    }

    public List<Materia> getMaterias() {
        return materias;
    }

    public void setMaterias(List<Materia> materias) {
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

    public List<MateriaJson> getListaMateriasFaltantes() {
        return listaMateriasFaltantes;
    }

    public int getTotalMaterias() {
        return totalMaterias;
    }

    public int getTotalFaltantes() {
        return totalFaltantes;
    };

    public int getTotalCursando() {
        return totalCursando;
    }

    public int getTotalCreditos() {
        return totalCreditos;
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

    public void setListaMateriasFaltantes(List<MateriaJson> listaMateriasFaltantes) {
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

    public Progreso copy() {

        List<MateriaJson> nuevaListaMateriasFaltantes = new ArrayList<>();
        if (this.listaMateriasFaltantes != null) {
            for (MateriaJson materia : this.listaMateriasFaltantes) {
                MateriaJson nuevaMateria = new MateriaJson();
                nuevaMateria.setCodigo(materia.getCodigo());
                nuevaMateria.setNombre(materia.getNombre());
                nuevaMateria.setCreditos(materia.getCreditos());
                nuevaMateria.setSemestre(materia.getSemestre());
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
                this.creditosPensum,
                this.creditosExtra,
                this.faltanElectiva,
                this.faltanComplementaria,
                this.faltanEnfasis,
                this.faltanElectivaBasicas,
                this.semestre);
    }

    @Override
    public String toString() {
        return "Progreso [promedio: " + promedio + ", materiasCursadas: " + materiasCursadas + ", materiasFaltantes="
                + materiasFaltantes + ", listaMateriasFaltantes: " + listaMateriasFaltantes + ", totalMaterias: "
                + totalMaterias + ", totalFaltantes: " + totalFaltantes + ", totalCursando: " + totalCursando
                + ", totalCreditos: " + totalCreditos + ", creditosPensum: " + creditosPensum + ", creditosExtra: "
                + creditosExtra + ", faltanElectiva: " + faltanElectiva + ", faltanComplementaria: "
                + faltanComplementaria + ", faltanEnfasis: " + faltanEnfasis + ", faltanElectivaBasicas: "
                + faltanElectivaBasicas + ", semestre=" + semestre + ", porcentaje=" + porcentaje + ", materias="
                + materias + ", lineasRequisitosGrado=" + lineasRequisitosGrado + "]";
    }

}