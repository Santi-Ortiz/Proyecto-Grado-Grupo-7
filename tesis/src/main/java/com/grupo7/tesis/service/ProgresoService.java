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

    public Progreso obtenerResumenAcademico(List<Materia> materiasCursadas, List<Materia> cursosElectivas, List<Materia> cursosComplementariaLenguas, List<Materia> cursosComplementariaInformacion, List<Materia> cursosEnfasis, List<Materia> cursosElectivaBasicas, List<Materia> cursosSeguridad, List<Materia> cursosIA) {
        double promedio = calcularPromedio(materiasCursadas);
        int totalMaterias = 0;
        int totalFaltantes = 0;
        int totalCursando = 0;
        int totalCreditos = 0;
        int faltanElectiva = 0;
        int faltanComplementaria = 0;
        int faltanEnfasis = 0;
        int faltanElectivaBasicas = 0;

        Set<String> codigosCursados = new HashSet<>();
        Set<String> codigosCursando = new HashSet<>();
        List<Materia> materiasRealmenteCursadas = new ArrayList<>();
        Set<String> codigosAgregados = new HashSet<>();
        List<MateriaJson> materiasFaltantes = new ArrayList<>();

        for (Materia m : materiasCursadas) {
            String codigoSinCeros = m.getCurso().replaceFirst("^0+(?!$)", "");
            if (m.getTipo() != null && m.getTipo().equalsIgnoreCase("Si") && esNumero(m.getCred()) && ((int)Double.parseDouble(m.getCred())) != 0) {
                codigosCursando.add(codigoSinCeros);
                continue;
            }
            if (m.getCred() != null && esNumero(m.getCred()) &&
                m.getCalif() != null && esNumero(m.getCalif())) {
                double calif = Double.parseDouble(m.getCalif());
                if (calif > 0 && !codigosAgregados.contains(codigoSinCeros)) {
                    codigosCursados.add(codigoSinCeros);
                    materiasRealmenteCursadas.add(m);
                    codigosAgregados.add(codigoSinCeros);
                    totalCreditos += (int) Double.parseDouble(m.getCred());
                }
            }
        }

        int creditosElectiva = cursosElectivas.stream()
            .filter(m -> esNumero(m.getCred()))
            .mapToInt(m -> (int) Double.parseDouble(m.getCred()))
            .sum();

        int creditosComplementaria = Stream.of(cursosComplementariaLenguas,cursosComplementariaInformacion)
            .flatMap(Collection::stream)
            .filter(m -> esNumero(m.getCred()))
            .mapToInt(m -> (int) Double.parseDouble(m.getCred()))
            .sum();

        int creditosEnfasis = Stream.of(cursosEnfasis, cursosSeguridad, cursosIA)
            .flatMap(Collection::stream)
            .filter(m -> esNumero(m.getCred()))
            .mapToInt(m -> (int) Double.parseDouble(m.getCred()))
            .sum();

        int creditosElectivaBasicas = cursosElectivaBasicas.stream()
            .filter(m -> esNumero(m.getCred()))
            .mapToInt(m -> (int) Double.parseDouble(m.getCred()))
            .sum();

        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = getClass().getResourceAsStream("/plan_estudios_INGSIS.json");
            List<MateriaJson> todasLasMaterias = mapper.readValue(is, new TypeReference<List<MateriaJson>>() {});

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

            int extraEnfasis = Math.max(creditosEnfasis - REQ_ENFASIS, 0);
            int creditosComplementariaValidados = creditosComplementaria + extraEnfasis;

            faltanElectiva = Math.max(REQ_ELECTIVA - creditosElectiva, 0);
            faltanComplementaria = Math.max(REQ_COMPLEMENTARIA - creditosComplementariaValidados, 0);
            faltanEnfasis = Math.max(REQ_ENFASIS - creditosEnfasis, 0);
            faltanElectivaBasicas = Math.max(REQ_ELECTIVA_BASICAS - creditosElectivaBasicas, 0);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new Progreso(
            promedio,
            materiasRealmenteCursadas.size(),
            materiasFaltantes.size(),
            materiasFaltantes,
            totalMaterias,
            totalFaltantes,
            totalCursando,
            totalCreditos,
            faltanElectiva,
            faltanComplementaria,
            faltanEnfasis,
            faltanElectivaBasicas
        );
    }
    
}
