package com.grupo7.tesis.services;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupo7.tesis.models.Materia;
import com.grupo7.tesis.repositories.MateriaRepository;
import com.grupo7.tesis.repositories.PensumMateriaRepository;
import com.grupo7.tesis.repositories.PensumRepository;

@Service
public class pensumService {

    @Autowired 
    private PensumRepository pensumRepository;

    @Autowired
    private PensumMateriaRepository pensumMateriaRepository;

    @Autowired
    private MateriaRepository materiaRepository;

    public List<Materia> obtenerPensum() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = getClass().getClassLoader().getResourceAsStream("plan_estudios_INGSIS.json");
        List<Materia> materias = mapper.readValue(is, new TypeReference<List<Materia>>() {
        });

        // Setear requisitosJson para cada materia
        for (Materia materia : materias) {
            try {
                String requisitosJson = mapper.writeValueAsString(materia.getRequisitos());
                materia.setRequisitosJson(requisitosJson);
            } catch (Exception e) {
                materia.setRequisitosJson("[]");
            }
        }

        return materias;
    }

    public Map<Integer, List<Materia>> obtenerMateriasPorSemestre() throws Exception {
        List<Materia> materias = obtenerPensum();
        ObjectMapper mapper = new ObjectMapper();

        for (Materia materia : materias) {
            if (materia.getRequisitos() != null) {
                String requisitosJson = "[]";
                try {
                    requisitosJson = mapper.writeValueAsString(materia.getRequisitos());
                } catch (Exception ignored) {
                }
                materia.setRequisitosJson(requisitosJson);
            }
        }

        return materias.stream()
                .collect(Collectors.groupingBy(Materia::getSemestre, TreeMap::new, Collectors.toList()));
    }

    public List<Map<String, String>> calcularConexionesValidas(Map<Integer, List<Materia>> materiasPorSemestre) {
        List<Materia> materias = materiasPorSemestre.values()
                .stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        // Mapa de código -> requisitos
        Map<String, List<String>> mapaRequisitos = new HashMap<>();
        for (Materia m : materias) {
            mapaRequisitos.put(m.getCodigo(), m.getRequisitos() != null ? m.getRequisitos() : new ArrayList<>());
        }

        Map<String, Set<String>> transitivosPorCodigo = new HashMap<>();
        for (String codigo : mapaRequisitos.keySet()) {
            transitivosPorCodigo.put(codigo, obtenerTransitivosDe(codigo, new HashSet<>(), mapaRequisitos));
        }

        List<Map<String, String>> conexionesValidas = new ArrayList<>();

        for (Materia destino : materias) {
            String destinoCodigo = destino.getCodigo();
            List<String> requisitos = mapaRequisitos.getOrDefault(destinoCodigo, List.of());

            List<String> filtrados = requisitos.stream().filter(req -> {
                // Se mantiene si no está contenido transitivamente en otro requisito
                for (String otroReq : requisitos) {
                    if (!otroReq.equals(req) && transitivosPorCodigo.getOrDefault(otroReq, Set.of()).contains(req)) {
                        return false;
                    }
                }
                return true;
            }).collect(Collectors.toList());

            for (String origen : filtrados) {
                Map<String, String> conexion = new HashMap<>();
                conexion.put("origen", origen);
                conexion.put("destino", destinoCodigo);
                conexionesValidas.add(conexion);
            }
        }

        return conexionesValidas;
    }

    private Set<String> obtenerTransitivosDe(String codigo, Set<String> visitados,
            Map<String, List<String>> mapaRequisitos) {
        if (visitados.contains(codigo))
            return new HashSet<>();
        visitados.add(codigo);

        Set<String> resultado = new HashSet<>();
        List<String> directos = mapaRequisitos.getOrDefault(codigo, List.of());
        resultado.addAll(directos);

        for (String req : directos) {
            resultado.addAll(obtenerTransitivosDe(req, new HashSet<>(visitados), mapaRequisitos));
        }

        return resultado;
    }
}
