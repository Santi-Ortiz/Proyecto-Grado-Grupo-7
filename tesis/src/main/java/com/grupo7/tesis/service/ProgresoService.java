package com.grupo7.tesis.service;

import java.io.InputStream;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupo7.tesis.model.*;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.*;
import java.util.stream.Stream;

@Service
public class ProgresoService {

    private static final int REQ_ELECTIVA = 8;
    private static final int REQ_COMPLEMENTARIA = 6;
    private static final int REQ_ENFASIS = 6;
    private static final int REQ_ELECTIVA_BASICAS = 3;
    
    private boolean esNumero(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public double calcularPromedio(List<Materia> materias){
        double promedio = 0.0;
        int totalCreditos = 0;
        
        for (Materia materia : materias) {
            String calif = materia.getCalif();
            String cred = materia.getCred();
            if (calif != null) calif = calif.replace(",", ".");
            if (cred != null) cred = cred.replace(",", ".");

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

    public Progreso obtenerResumenAcademico(List<Materia> materiasCursadas, List<Materia> cursosElectivas, List<Materia> cursosComplementariaLenguas, List<Materia> cursosComplementariaInformacion, List<Materia> cursosEnfasis, List<Materia> cursosElectivaBasicas, List<Materia> cursosSeguridad, List<Materia> cursosIA, List<Materia> tablaDesarrolloComputacion, List<Materia> tablaDesarrolloGestion, List<Materia> tablaComputacionVisual, List<Materia> tablaCVtoIA, List<Materia> tablaSIGtoIA) {
        double promedio = calcularPromedio(materiasCursadas);
        int totalMaterias = 0;
        int totalFaltantes = 0;
        int totalCursando = 0;
        int totalCreditos = 0;
        int creditosPensum = 0;
        int faltanElectiva = 0;
        int faltanComplementaria = 0;
        int faltanEnfasis = 0;
        int faltanElectivaBasicas = 0;
        int creditosComplementariaValidados = 0;
        int extraEnfasis = 0;
        int creditosPerdidos = 0;

        Set<String> codigosCursados = new HashSet<>();
        Set<String> codigosCursando = new HashSet<>();
        Set<String> codigosPerdidos = new HashSet<>();
        List<Materia> materiasRealmenteCursadas = new ArrayList<>();
        Set<String> codigosAgregados = new HashSet<>();
        Set<String> codigosPerdidosAgregados = new HashSet<>();
        List<MateriaJson> materiasFaltantes = new ArrayList<>();

        int numeroSemestre = calcularNumeroSemestre(materiasCursadas);

        for (Materia m : materiasCursadas) {
            String codigoSinCeros = m.getCurso().replaceFirst("^0+(?!$)", "");
            String cred = m.getCred() != null ? m.getCred().replace(",", ".") : null;
            String calif = m.getCalif() != null ? m.getCalif().replace(",", ".") : null;

            if (m.getTipo() != null && m.getTipo().equalsIgnoreCase("Si") && cred != null && esNumero(cred) && ((int)Double.parseDouble(cred)) != 0) {
                codigosCursando.add(codigoSinCeros);
                continue;
            }
            if (cred != null && esNumero(cred) && calif != null && esNumero(calif)) {
                double califNum = Double.parseDouble(calif);
                int creditos = (int) Double.parseDouble(cred);
                
                if (califNum >= 3.0 && !codigosAgregados.contains(codigoSinCeros)) {
                    codigosCursados.add(codigoSinCeros);
                    materiasRealmenteCursadas.add(m);
                    codigosAgregados.add(codigoSinCeros);
                    totalCreditos += creditos;
                } else if ( califNum < 3.0 && !codigosPerdidosAgregados.contains(codigoSinCeros)) {
                    codigosPerdidos.add(codigoSinCeros);
                    codigosPerdidosAgregados.add(codigoSinCeros);
                    creditosPerdidos += creditos;
                    totalCreditos += creditos;
                }
            }
        }

        int creditosElectiva = cursosElectivas.stream()
            .filter(m -> m.getCred() != null && esNumero(m.getCred().replace(",", ".")))
            .mapToInt(m -> (int) Double.parseDouble(m.getCred().replace(",", ".")))
            .sum();

        int creditosComplementaria = Stream.of(cursosComplementariaLenguas, cursosComplementariaInformacion)
            .flatMap(Collection::stream)
            .filter(m -> m.getCred() != null && esNumero(m.getCred().replace(",", ".")))
            .mapToInt(m -> (int) Double.parseDouble(m.getCred().replace(",", ".")))
            .sum();

        int creditosEnfasis = Stream.of(cursosEnfasis, cursosSeguridad, cursosIA, tablaDesarrolloComputacion, tablaDesarrolloGestion, tablaComputacionVisual, tablaCVtoIA, tablaSIGtoIA)
            .flatMap(Collection::stream)
            .filter(m -> m.getCred() != null && esNumero(m.getCred().replace(",", ".")))
            .mapToInt(m -> (int) Double.parseDouble(m.getCred().replace(",", ".")))
            .sum();

        int creditosElectivaBasicas = cursosElectivaBasicas.stream()
            .filter(m -> m.getCred() != null && esNumero(m.getCred().replace(",", ".")))
            .mapToInt(m -> (int) Double.parseDouble(m.getCred().replace(",", ".")))
            .sum();

        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = getClass().getResourceAsStream("/plan_estudios_INGSIS.json");
            List<MateriaJson> todasLasMaterias = mapper.readValue(is, new TypeReference<List<MateriaJson>>() {});

            Set<String> codigosPensum = new HashSet<>();
            for (MateriaJson m : todasLasMaterias) {
                String codigo = m.getCodigo().replaceFirst("^0+(?!$)", "");
                codigosPensum.add(codigo);
            }

            for (Materia m : materiasRealmenteCursadas) {
                String codigoSinCeros = m.getCurso().replaceFirst("^0+(?!$)", "");
                String cred = m.getCred() != null ? m.getCred().replace(",", ".") : null;
                
                if (cred != null && esNumero(cred)) {
                    int creditos = (int) Double.parseDouble(cred);
                    if (codigosPensum.contains(codigoSinCeros)) {
                        creditosPensum += creditos;
                    }
                }
            }

            for (MateriaJson m : todasLasMaterias) {
                String nombre = m.getNombre().toLowerCase();
                String codigoJson = m.getCodigo().replaceFirst("^0+(?!$)", "");

                if (nombre.contains("electiva") || nombre.contains("complementaria") || nombre.contains("énfasis")) {
                    continue;
                }

                if (!codigosCursados.contains(codigoJson) && !codigosCursando.contains(codigoJson)) {
                    materiasFaltantes.add(m);
                }
            }

            totalMaterias = todasLasMaterias.size();
            totalFaltantes = materiasFaltantes.size();
            totalCursando = codigosCursando.size();

            extraEnfasis = Math.max(creditosEnfasis - REQ_ENFASIS, 0);
            creditosComplementariaValidados = creditosComplementaria + extraEnfasis;

            faltanElectiva = Math.max(REQ_ELECTIVA - creditosElectiva, 0);
            faltanComplementaria = Math.max(REQ_COMPLEMENTARIA - creditosComplementariaValidados, 0);
            faltanEnfasis = Math.max(REQ_ENFASIS - creditosEnfasis, 0);
            faltanElectivaBasicas = Math.max(REQ_ELECTIVA_BASICAS - creditosElectivaBasicas, 0);

            creditosPensum += Math.min(creditosElectiva, REQ_ELECTIVA);
            creditosPensum += Math.min(creditosComplementariaValidados, REQ_COMPLEMENTARIA);
            creditosPensum += Math.min(creditosEnfasis - extraEnfasis, REQ_ENFASIS);
            creditosPensum += Math.min(creditosElectivaBasicas, REQ_ELECTIVA_BASICAS);

        } catch (Exception e) {
            e.printStackTrace();
        }

        int creditosElectivaExtra = Math.max(creditosElectiva - REQ_ELECTIVA, 0);
        int creditosComplementariaExtra = Math.max(creditosComplementariaValidados - REQ_COMPLEMENTARIA, 0);        
        int creditosEnfasisExtra = Math.max(creditosEnfasis - REQ_ENFASIS - extraEnfasis, 0);
        int creditosElectivaBasicasExtra = Math.max(creditosElectivaBasicas - REQ_ELECTIVA_BASICAS, 0);
        
        int creditosExtra = creditosElectivaExtra + creditosComplementariaExtra + creditosEnfasisExtra + creditosElectivaBasicasExtra + creditosPerdidos;

        Progreso progreso = new  Progreso(
            promedio,
            materiasRealmenteCursadas.size(),
            materiasFaltantes.size(),
            materiasFaltantes,
            totalMaterias,
            totalFaltantes,
            totalCursando,
            totalCreditos,
            creditosPensum,
            creditosExtra,
            faltanElectiva,
            faltanComplementaria,
            faltanEnfasis,
            faltanElectivaBasicas,
            numeroSemestre
        );
        progreso.setMaterias(materiasCursadas); 
        double porcentaje = (progreso.getCreditosPensum() * 100.0) / 138.0;
        progreso.setPorcentaje(porcentaje);
        return progreso;
    }

    public int calcularNumeroSemestre(List<Materia> materias) {
        if (materias == null || materias.isEmpty()) {
            return 1;
        }
        
        String semestreAnterior = "";
        int contadorSemestres = 0;
        
        for (Materia materia : materias) {
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

}
