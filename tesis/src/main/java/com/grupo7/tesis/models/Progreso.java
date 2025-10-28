package com.grupo7.tesis.models;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupo7.tesis.dtos.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class Progreso {
    //Nueva version
    private List<MateriaDTO> materias;
    private List<String> lineasRequisitosGrado;
    private List<MateriaDTO> cursosElectivas;
    private List<MateriaDTO> cursosEnfasis;
    private List<MateriaDTO> cursosComplementariaLenguas;
    private List<MateriaDTO> cursosComplementariaInformacion;
    private List<MateriaDTO> cursosComplementariaEstetica;
    private List<MateriaDTO> cursosComplementariaCienciaPolitica;
    private List<MateriaDTO> cursosIA;
    private List<MateriaDTO> cursosDesarrolloComputacion;
    private List<MateriaDTO> cursosDesarrolloGestion;
    private List<MateriaDTO> cursosComputacionVisual;
    private List<MateriaDTO> cursosCVtoIA;
    private List<MateriaDTO> cursosSIGtoIA;
    private List<MateriaDTO> cursosElectivaBasicas;
    private List<MateriaDTO> cursosSeguridad;
    private List<Materia> listaMateriasFaltantes;

    private static final int REQ_ELECTIVA = 8;
    private static final int REQ_COMPLEMENTARIA = 6;
    private static final int REQ_ENFASIS = 6;
    private static final int REQ_ELECTIVA_BASICAS = 3;

    public Progreso() {
    }

    public Progreso(List<MateriaDTO> cursosVacios, List<String> lineasVacias, List<MateriaDTO> cursosVacios2,
            List<MateriaDTO> cursosVacios3, List<MateriaDTO> cursosVacios4, List<MateriaDTO> cursosVacios5,
            List<MateriaDTO> cursosVacios6, List<MateriaDTO> cursosVacios7, List<MateriaDTO> cursosVacios8,
            List<MateriaDTO> cursosVacios9, List<MateriaDTO> cursosVacios10, List<MateriaDTO> cursosVacios11,
            List<MateriaDTO> cursosVacios12, List<MateriaDTO> cursosVacios13, List<MateriaDTO> cursosVacios14,
            List<Materia> clases) {
        this.materias = cursosVacios;
        this.lineasRequisitosGrado = lineasVacias;
        this.cursosElectivas = cursosVacios2;
        this.cursosEnfasis = cursosVacios3;
        this.cursosComplementariaLenguas = cursosVacios4;
        this.cursosComplementariaInformacion = cursosVacios5;
        this.cursosIA = cursosVacios6;
        this.cursosDesarrolloComputacion = cursosVacios7;
        this.cursosDesarrolloGestion = cursosVacios8;
        this.cursosComputacionVisual = cursosVacios9;
        this.cursosCVtoIA = cursosVacios10;
        this.cursosSIGtoIA = cursosVacios11;
        this.cursosElectivaBasicas = cursosVacios12;
        this.cursosSeguridad = cursosVacios13;
        this.listaMateriasFaltantes = clases;
    }



    public List<MateriaDTO> getMaterias() {
        return materias;
    }

    public void setMaterias(List<MateriaDTO> materias) {
        this.materias = materias;
    }

    @JsonIgnore
    public List<Materia> getMateriasFaltantes() {
        return getListaMateriasFaltantes();
    }

    @JsonIgnore
    public void setMateriasFaltantes() {
        this.listaMateriasFaltantes = getListaMateriasFaltantes();
    }

    public List<String> getLineasRequisitosGrado() {
        return lineasRequisitosGrado;
    }

    public void setLineasRequisitosGrado(List<String> lineasRequisitosGrado) {
        this.lineasRequisitosGrado = lineasRequisitosGrado;
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

    public List<MateriaDTO> getCursosComplementariaEstetica() { return cursosComplementariaEstetica; }
    public void setCursosComplementariaEstetica(List<MateriaDTO> cursosComplementariaEstetica) { this.cursosComplementariaEstetica = cursosComplementariaEstetica; }

    public List<MateriaDTO> getCursosComplementariaCienciaPolitica() { return cursosComplementariaCienciaPolitica; }
    public void setCursosComplementariaCienciaPolitica(List<MateriaDTO> cursosComplementariaCienciaPolitica) { this.cursosComplementariaCienciaPolitica = cursosComplementariaCienciaPolitica; }


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

    // Getters/setters directos para serialización JSON (no calculados)
    @JsonProperty("listaMateriasFaltantes")
    public List<Materia> getListaMateriasFaltantesDirecto() {
        return listaMateriasFaltantes;
    }

    @JsonProperty("listaMateriasFaltantes")
    public void setListaMateriasFaltantesDirecto(List<Materia> listaMateriasFaltantes) {
        this.listaMateriasFaltantes = listaMateriasFaltantes;
    }

    @JsonIgnore
    public double getPromedio() {
        if (materias == null || materias.isEmpty()) return 0.0;
        
        double promedio = 0.0;
        int totalCreditos = 0;

        for (MateriaDTO materia : materias) {
            String calif = materia.getCalif();
            String cred = materia.getCred();
            if (calif != null)
                calif = calif.replace(",", ".");
            if (cred != null)
                cred = cred.replace(",", ".");

            if (calif != null && cred != null && esNumero(calif) && esNumero(cred) && Double.parseDouble(calif) > 0) {
                int creditosInt = (int) Double.parseDouble(cred);
                promedio += Double.parseDouble(calif) * creditosInt;
                totalCreditos += creditosInt;
            }
        }

        if (totalCreditos > 0) {
            promedio /= totalCreditos;
            promedio = Math.round(promedio * 100.0) / 100.0;
        } else {
            promedio = 0.0;
        }

        return promedio;
    }

    @JsonIgnore
    public boolean esNumero(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @JsonIgnore
    public List<MateriaDTO> getMateriasCursadas() {
        if (materias == null) return new ArrayList<>();
        
        Set<String> codigosAgregados = new HashSet<>();
        List<MateriaDTO> materiasRealmenteCursadas = new ArrayList<>();

        for (MateriaDTO m : materias) {
            String codigoSinCeros = m.getCurso().replaceFirst("^0+(?!$)", "");
            String cred = m.getCred() != null ? m.getCred().replace(",", ".") : null;
            String calif = m.getCalif() != null ? m.getCalif().replace(",", ".") : null;

            if (cred != null && esNumero(cred) && calif != null && esNumero(calif)) {
                double califNum = Double.parseDouble(calif);

                if (califNum >= 3.0 && !codigosAgregados.contains(codigoSinCeros)) {
                    materiasRealmenteCursadas.add(m);
                    codigosAgregados.add(codigoSinCeros);
                }
            }
        }

        return materiasRealmenteCursadas;
    }

    @JsonIgnore
    public List<Materia> getListaMateriasFaltantes() {
        if (materias == null) return new ArrayList<>();
        
        List<Materia> materiasFaltantes = new ArrayList<>();
        Set<String> codigosCursados = new HashSet<>();
        Set<String> codigosCursando = new HashSet<>();
        Set<String> codigosAgregados = new HashSet<>();
        List<Materia> todasLasMaterias = getTotalMaterias();

        for (MateriaDTO m : materias) {
            String codigoSinCeros = m.getCurso().replaceFirst("^0+(?!$)", "");
            String cred = m.getCred() != null ? m.getCred().replace(",", ".") : null;
            String calif = m.getCalif() != null ? m.getCalif().replace(",", ".") : null;

            if (m.getTipo() != null && m.getTipo().equalsIgnoreCase("Si") && cred != null && esNumero(cred) && ((int) Double.parseDouble(cred)) != 0) {
                codigosCursando.add(codigoSinCeros);
                continue;
            }

            if (cred != null && esNumero(cred) && calif != null && esNumero(calif)) {
                double califNum = Double.parseDouble(calif);
                if (califNum >= 3.0 && !codigosAgregados.contains(codigoSinCeros)) {
                    codigosCursados.add(codigoSinCeros);
                    codigosAgregados.add(codigoSinCeros);
                } 
            }
        }
        
        for (Materia m : todasLasMaterias) {
            String nombre = m.getNombre().toLowerCase();
            String codigoJson = m.getCodigo().replaceFirst("^0+(?!$)", "");
            if (nombre.contains("electiva") || nombre.contains("complementaria") || nombre.contains("énfasis"))
                continue;
            if (!codigosCursados.contains(codigoJson) && !codigosCursando.contains(codigoJson))
                materiasFaltantes.add(m);
        }
       
        return materiasFaltantes;
    }

    @JsonIgnore
    public List<Materia> getTotalMaterias() {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = getClass().getResourceAsStream("/plan_estudios_INGSIS.json");
        List<Materia> todasLasMaterias = null;

        try {
            todasLasMaterias = mapper.readValue(is, new TypeReference<List<Materia>>() {});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return todasLasMaterias;
    }

    @JsonIgnore
    public int getTotalCursando(){
        if (materias == null) return 0;
        
        Set<String> codigosCursando = new HashSet<>();
        for (MateriaDTO m : materias) {
            String codigoSinCeros = m.getCurso().replaceFirst("^0+(?!$)", "");
            String cred = m.getCred() != null ? m.getCred().replace(",", ".") : null;

            if (m.getTipo() != null && m.getTipo().equalsIgnoreCase("Si") && cred != null && esNumero(cred) && ((int) Double.parseDouble(cred)) != 0) {
                codigosCursando.add(codigoSinCeros);
                continue;
            }
        }
        return codigosCursando.size();
    }
    
    @JsonIgnore
    public int getTotalCreditos(){
        if (materias == null) return 0;
        
        int totalCreditos = 0;
        Set<String> codigosAgregados = new HashSet<>();
        Set<String> codigosPerdidosAgregados = new HashSet<>();

        for (MateriaDTO m : materias) {
            String codigoSinCeros = m.getCurso().replaceFirst("^0+(?!$)", "");
            String cred = m.getCred() != null ? m.getCred().replace(",", ".") : null;
            String calif = m.getCalif() != null ? m.getCalif().replace(",", ".") : null;

            if (cred != null && esNumero(cred) && calif != null && esNumero(calif)) {
                double califNum = Double.parseDouble(calif);
                int creditos = (int) Double.parseDouble(cred);

                if (califNum >= 3.0 && !codigosAgregados.contains(codigoSinCeros)) {
                    codigosAgregados.add(codigoSinCeros);
                    totalCreditos += creditos;
                } else if (califNum < 3.0 && !codigosPerdidosAgregados.contains(codigoSinCeros)) {
                    codigosPerdidosAgregados.add(codigoSinCeros);
                    totalCreditos += creditos;
                }
            }
        }

        return totalCreditos;
    }

    @JsonIgnore
    public int getCreditosCursados() {
        List<MateriaDTO> materiasCursadas = getMateriasCursadas();
        int creditosCursados = materiasCursadas.stream()
                .filter(m -> m.getCred() != null && esNumero(m.getCred().replace(",", ".")))
                .mapToInt(m -> (int) Double.parseDouble(m.getCred().replace(",", ".")))
                .sum();
        return creditosCursados;
    }

    @JsonIgnore
    public int getCreditosCursando() {
        if (materias == null) return 0;
        
        int creditosCursando = materias.stream()
                .filter(m -> "Si".equalsIgnoreCase(m.getTipo()))
                .filter(m -> m.getCred() != null && esNumero(m.getCred().replace(",", ".")))
                .mapToInt(m -> (int) Double.parseDouble(m.getCred().replace(",", ".")))
                .sum();
        return creditosCursando;
    }

    @JsonIgnore
    public int getCreditosFaltantes() {
        return getListaMateriasFaltantes().stream()
            .filter(m -> m.getCreditos() != null)
            .filter(m -> !m.getTipo().contains("enfasis"))
            .mapToInt(Materia::getCreditos)
            .sum();
    }

    @JsonIgnore
    public int getCreditosPensum() {
        int creditosPensum = 0;
        Set<String> codigosPensum = new HashSet<>();

        for (Materia m : getTotalMaterias()) {
            String codigo = m.getCodigo().replaceFirst("^0+(?!$)", "");
            codigosPensum.add(codigo);
        }
        for (MateriaDTO m : getMateriasCursadas()) {
            String codigoSinCeros = m.getCurso().replaceFirst("^0+(?!$)", "");
            String cred = m.getCred() != null ? m.getCred().replace(",", ".") : null;

            if (cred != null && esNumero(cred)) {
                int creditos = (int) Double.parseDouble(cred);
                if (codigosPensum.contains(codigoSinCeros)) {
                    creditosPensum += creditos;
                }
            }
        }

        int extraEnfasis = Math.max(getCreditosEnfasis() - REQ_ENFASIS, 0);
        int creditosComplementariaValidados = getCreditosComplementaria() + extraEnfasis;

        creditosPensum += Math.min(getCreditosElectiva(), REQ_ELECTIVA);
        creditosPensum += Math.min(creditosComplementariaValidados, REQ_COMPLEMENTARIA);
        creditosPensum += Math.min(getCreditosEnfasis() - extraEnfasis, REQ_ENFASIS);
        creditosPensum += Math.min(getCreditosElectivaBasicas(), REQ_ELECTIVA_BASICAS);

        return creditosPensum;
    }

    @JsonIgnore
    public int getCreditosElectiva() {
        if (cursosElectivas == null) return 0;
        
        int creditosElectiva = cursosElectivas.stream()
                .filter(m -> m.getCred() != null && esNumero(m.getCred().replace(",", ".")))
                .mapToInt(m -> (int) Double.parseDouble(m.getCred().replace(",", ".")))
                .sum();
        return creditosElectiva;
    }

    @JsonIgnore
    public int getCreditosComplementaria() {
        List<MateriaDTO> lenguas = cursosComplementariaLenguas != null ? cursosComplementariaLenguas : new ArrayList<>();
        List<MateriaDTO> informacion = cursosComplementariaInformacion != null ? cursosComplementariaInformacion : new ArrayList<>();
        
        int creditosComplementaria = Stream.of(lenguas, informacion)
                    .flatMap(Collection::stream)
                    .filter(m -> m.getCred() != null && esNumero(m.getCred().replace(",", ".")))
                    .mapToInt(m -> (int) Double.parseDouble(m.getCred().replace(",", ".")))
                    .sum();
        return creditosComplementaria;
    }

    @JsonIgnore
    public int getCreditosEnfasis() {
        List<MateriaDTO> enfasis = cursosEnfasis != null ? cursosEnfasis : new ArrayList<>();
        List<MateriaDTO> seguridad = cursosSeguridad != null ? cursosSeguridad : new ArrayList<>();
        List<MateriaDTO> ia = cursosIA != null ? cursosIA : new ArrayList<>();
        List<MateriaDTO> desarrollo = cursosDesarrolloComputacion != null ? cursosDesarrolloComputacion : new ArrayList<>();
        List<MateriaDTO> gestion = cursosDesarrolloGestion != null ? cursosDesarrolloGestion : new ArrayList<>();
        List<MateriaDTO> visual = cursosComputacionVisual != null ? cursosComputacionVisual : new ArrayList<>();
        List<MateriaDTO> cvToIa = cursosCVtoIA != null ? cursosCVtoIA : new ArrayList<>();
        List<MateriaDTO> sigToIa = cursosSIGtoIA != null ? cursosSIGtoIA : new ArrayList<>();
        
        int creditosEnfasis = Stream
                .of(enfasis, seguridad, ia, desarrollo, gestion, visual, cvToIa, sigToIa)
                .flatMap(Collection::stream)
                .filter(m -> m.getCred() != null && esNumero(m.getCred().replace(",", ".")))
                .mapToInt(m -> (int) Double.parseDouble(m.getCred().replace(",", ".")))
                .sum();

        return creditosEnfasis;
    }

    @JsonIgnore
    public int getCreditosElectivaBasicas() {
        if (cursosElectivaBasicas == null) return 0;
        
        int creditosElectivaBasicas = cursosElectivaBasicas.stream()
                .filter(m -> m.getCred() != null && esNumero(m.getCred().replace(",", ".")))
                .mapToInt(m -> (int) Double.parseDouble(m.getCred().replace(",", ".")))
                .sum();
        return creditosElectivaBasicas;
    }

    @JsonIgnore
    public int getCreditosExtra(){

        int creditosPerdidos = 0;
        Set<String> codigosPerdidosAgregados = new HashSet<>();

        for (MateriaDTO m : materias) {
            String codigoSinCeros = m.getCurso().replaceFirst("^0+(?!$)", "");
            String cred = m.getCred() != null ? m.getCred().replace(",", ".") : null;
            String calif = m.getCalif() != null ? m.getCalif().replace(",", ".") : null;

            if (cred != null && esNumero(cred) && calif != null && esNumero(calif)) {
                double califNum = Double.parseDouble(calif);
                int creditos = (int) Double.parseDouble(cred);

                if (califNum < 3.0 && !codigosPerdidosAgregados.contains(codigoSinCeros)) {
                    codigosPerdidosAgregados.add(codigoSinCeros);
                    creditosPerdidos += creditos;
                }
            }
        }

        int creditosExtra = 0;
        int extraEnfasis = Math.max(getCreditosEnfasis() - REQ_ENFASIS, 0);
        int creditosComplementariaValidados = getCreditosComplementaria() + extraEnfasis;

        int creditosElectivaExtra = Math.max(getCreditosElectiva() - REQ_ELECTIVA, 0);
        int creditosComplementariaExtra = Math.max(creditosComplementariaValidados - REQ_COMPLEMENTARIA, 0);
        int creditosEnfasisExtra = Math.max(getCreditosEnfasis() - REQ_ENFASIS - extraEnfasis, 0);
        int creditosElectivaBasicasExtra = Math.max(getCreditosElectivaBasicas() - REQ_ELECTIVA_BASICAS, 0);

        creditosExtra = creditosElectivaExtra + creditosComplementariaExtra + creditosEnfasisExtra + creditosElectivaBasicasExtra + creditosPerdidos;

        return creditosExtra;
    }

    @JsonIgnore
    public int getFaltanElectiva() {
        int faltanElectiva = Math.max(REQ_ELECTIVA - getCreditosElectiva(), 0);
        return faltanElectiva;
    }

    @JsonIgnore
    public int getFaltanComplementaria() {
        int extraEnfasis = Math.max(getCreditosEnfasis() - REQ_ENFASIS, 0);
        int creditosComplementariaValidados = getCreditosComplementaria() + extraEnfasis;

        int faltanComplementaria = Math.max(REQ_COMPLEMENTARIA - creditosComplementariaValidados, 0);
        return faltanComplementaria;
    }

    @JsonIgnore
    public int getFaltanEnfasis() {
        int faltanEnfasis = Math.max(REQ_ENFASIS - getCreditosEnfasis(), 0);
        return faltanEnfasis;
    }

    @JsonIgnore
    public int getFaltanElectivaBasicas() {
        int faltanElectivaBasicas = Math.max(REQ_ELECTIVA_BASICAS - getCreditosElectivaBasicas(), 0);
        return faltanElectivaBasicas;
    }

    @JsonIgnore
    public int getFaltanEnfasisTesis() {
        List<Materia> materiasFaltantes = getMateriasFaltantes();
        int creditosTesis = 0;

        for (Materia materia : materiasFaltantes) {
            if (materia.getTipo().equals("enfasis") && !materia.getCodigo().equals("5") ) {
                creditosTesis += materia.getCreditos();
            }
        }

        return creditosTesis;
    }

    @JsonIgnore
    public int getMateriasEnfasisCursando() {
        int enfasisCursando = 0;

        if (materias == null) return 0;
        
        Set<String> codigosPensum = new HashSet<>();

        List<Materia> todasLasMaterias = getTotalMaterias();
        for (Materia m : todasLasMaterias) {
            String tipo = m.getTipo();
            String codigoJson = m.getCodigo().replaceFirst("^0+(?!$)", "");
            if (tipo.contains("enfasis") && !codigoJson.equals("5")) { 
                codigosPensum.add(codigoJson);
            }
        }

        for (MateriaDTO m : materias) {
            String codigoSinCeros = m.getCurso().replaceFirst("^0+(?!$)", "");
            String cred = m.getCred() != null ? m.getCred().replace(",", ".") : null;

            if (m.getTipo() != null && m.getTipo().equalsIgnoreCase("Si") && 
                cred != null && esNumero(cred) && 
                ((int) Double.parseDouble(cred)) != 0 &&
                codigosPensum.contains(codigoSinCeros)) {
                enfasisCursando+= m.getCred() != null && esNumero(m.getCred().replace(",", ".")) ? (int) Double.parseDouble(m.getCred().replace(",", ".")) : 0;
            }
        }
        return enfasisCursando; 
    }

    @JsonIgnore
    public int getMateriasNucleoCursando() {
        int nucleoCursando = 0;

        if (materias == null) return 0;
        
        Set<String> codigosPensum = new HashSet<>();
        
        List<Materia> todasLasMaterias = getTotalMaterias();
        for (Materia m : todasLasMaterias) {
            String nombre = m.getNombre().toLowerCase();
            String codigoJson = m.getCodigo().replaceFirst("^0+(?!$)", "");
            if (!nombre.contains("electiva") && !nombre.contains("complementaria") && !m.getTipo().contains("enfasis")) {
                codigosPensum.add(codigoJson);
            }
        }
        
        for (MateriaDTO m : materias) {
            String codigoSinCeros = m.getCurso().replaceFirst("^0+(?!$)", "");
            String cred = m.getCred() != null ? m.getCred().replace(",", ".") : null;

            if (m.getTipo() != null && m.getTipo().equalsIgnoreCase("Si") && 
                cred != null && esNumero(cred) && 
                ((int) Double.parseDouble(cred)) != 0 &&
                codigosPensum.contains(codigoSinCeros)) {
                nucleoCursando+= m.getCred() != null && esNumero(m.getCred().replace(",", ".")) ? (int) Double.parseDouble(m.getCred().replace(",", ".")) : 0;
            }
        }
        return nucleoCursando; 
    }

    @JsonIgnore
    public double getPorcentaje() {
        double porcentaje = (getCreditosPensum() * 100.0) / 138.0;
        return porcentaje;
    }

    @JsonIgnore
    public int getSemestre() {
        if (materias == null || materias.isEmpty()) {
            return 1;
        }

        String semestreAnterior = "";
        int contadorSemestres = 0;

        for (MateriaDTO materia : materias) {
            String cicloLectivo = materia.getCicloLectivo();

            if (cicloLectivo == null || cicloLectivo.trim().isEmpty()) {
                continue;
            }

            cicloLectivo = cicloLectivo.trim();

            if (cicloLectivo.startsWith("TerPe") || cicloLectivo.startsWith("PrimPe")) {

                if (!cicloLectivo.equals(semestreAnterior)) {
                    contadorSemestres++;
                    semestreAnterior = cicloLectivo;
                }
            }
        }

        return Math.max(1, contadorSemestres);
    }

    @JsonIgnore
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

        Progreso nuevoProgreso = new Progreso();
        
        if (this.materias != null) {
            nuevoProgreso.setMaterias(new ArrayList<>(this.materias));
        }
        if (this.lineasRequisitosGrado != null) {
            nuevoProgreso.setLineasRequisitosGrado(new ArrayList<>(this.lineasRequisitosGrado));
        }
        
        if (this.cursosElectivas != null) {
            nuevoProgreso.setCursosElectivas(new ArrayList<>(this.cursosElectivas));
        }
        if (this.cursosEnfasis != null) {
            nuevoProgreso.setCursosEnfasis(new ArrayList<>(this.cursosEnfasis));
        }
        if (this.cursosComplementariaLenguas != null) {
            nuevoProgreso.setCursosComplementariaLenguas(new ArrayList<>(this.cursosComplementariaLenguas));
        }
        if (this.cursosComplementariaInformacion != null) {
            nuevoProgreso.setCursosComplementariaInformacion(new ArrayList<>(this.cursosComplementariaInformacion));
        }
        if (this.cursosIA != null) {
            nuevoProgreso.setCursosIA(new ArrayList<>(this.cursosIA));
        }
        if (this.cursosDesarrolloComputacion != null) {
            nuevoProgreso.setCursosDesarrolloComputacion(new ArrayList<>(this.cursosDesarrolloComputacion));
        }
        if (this.cursosDesarrolloGestion != null) {
            nuevoProgreso.setCursosDesarrolloGestion(new ArrayList<>(this.cursosDesarrolloGestion));
        }
        if (this.cursosComputacionVisual != null) {
            nuevoProgreso.setCursosComputacionVisual(new ArrayList<>(this.cursosComputacionVisual));
        }
        if (this.cursosCVtoIA != null) {
            nuevoProgreso.setCursosCVtoIA(new ArrayList<>(this.cursosCVtoIA));
        }
        if (this.cursosSIGtoIA != null) {
            nuevoProgreso.setCursosSIGtoIA(new ArrayList<>(this.cursosSIGtoIA));
        }
        if (this.cursosElectivaBasicas != null) {
            nuevoProgreso.setCursosElectivaBasicas(new ArrayList<>(this.cursosElectivaBasicas));
        }
        if (this.cursosSeguridad != null) {
            nuevoProgreso.setCursosSeguridad(new ArrayList<>(this.cursosSeguridad));
        }
        
        // Copiar la lista de materias faltantes
        nuevoProgreso.setListaMateriasFaltantesDirecto(nuevaListaMateriasFaltantes);
        
        return nuevoProgreso;
    }

    @Override
    public String toString() {
        return "Progreso [promedio=" + getPromedio() + ", materiasCursadas=" + getMateriasCursadas().size() + ", listaMateriasFaltantes=" + listaMateriasFaltantes + ", totalMaterias="
                + getTotalMaterias().size() + ", totalFaltantes=" + getListaMateriasFaltantes().size() + ", totalCursando=" + getTotalCursando()
                + ", totalCreditos=" + getTotalCreditos() + ", creditosCursados=" + getCreditosCursados() + ", creditosCursando="
                + getCreditosCursando() + ", creditosFaltantes=" + getCreditosFaltantes() + ", creditosPensum=" + getCreditosPensum()
                + ", creditosExtra=" + getCreditosExtra() + ", faltanElectiva=" + getFaltanElectiva() + ", faltanComplementaria="
                + getFaltanComplementaria() + ", faltanEnfasis=" + getFaltanEnfasis() + ", faltanElectivaBasicas="
                + getFaltanElectivaBasicas() + ", semestre=" + getSemestre() + ", porcentaje=" + getPorcentaje()
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