package com.grupo7.tesis.services;

import java.io.InputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupo7.tesis.models.Materia;
import com.grupo7.tesis.models.Pensum;
import com.grupo7.tesis.models.PensumMateria;
import com.grupo7.tesis.repositories.PensumRepository;

@Service
public class PensumService {

    @Autowired
    private PensumRepository pensumRepository;

    @Autowired
    private PensumMateriaService pensumMateriaService;

    public List<Pensum> obtenerPensums() {
        return pensumRepository.findAll();
    }

    public Pensum obtenerPensumPorId(Long id) {
        return pensumRepository.findById(id).orElse(null);
    }

    public Pensum crearPensum(Pensum pensum) {
        return pensumRepository.save(pensum);
    }

    public Pensum actualizarPensum(Long id, Pensum pensumActualizado) {
        Pensum pensumExistente = pensumRepository.findById(id).orElse(null);
        if (pensumExistente != null) {

            if (pensumActualizado.getCarrera() != null) {
                pensumExistente.setCarrera(pensumActualizado.getCarrera());
            }
            if (pensumActualizado.getCreditosTotales() != null) {
                pensumExistente.setCreditosTotales(pensumActualizado.getCreditosTotales());
            }
            if (pensumActualizado.getNumeroSemestres() != null) {
                pensumExistente.setNumeroSemestres(pensumActualizado.getNumeroSemestres());
            }
            if (pensumActualizado.getMateriasAsociadas() != null) {
                pensumExistente.setMateriasAsociadas(pensumActualizado.getMateriasAsociadas());
            }
            return pensumRepository.save(pensumExistente);
        }
        return null;
    }

    public Pensum eliminarPensum(Long id) {
        if (pensumRepository.existsById(id)) {
            Pensum pensum = pensumRepository.findById(id).orElse(null);
            pensumRepository.deleteById(id);
            return pensum;
        }
        return null;
    }

    public List<Materia> obtenerPensumJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = getClass().getClassLoader().getResourceAsStream("plan_estudios_INGSIS.json");
        List<Materia> materias = mapper.readValue(is, new TypeReference<List<Materia>>() {
        });

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

    /* MÉTODOS PARA ASOCIAR MATERIAS A PENSUMS */

    public List<Materia> obtenerMateriasPorPensumId(Long pensumId) {
        Pensum pensum = obtenerPensumPorId(pensumId);
        if (pensum == null) {
            throw new RuntimeException("Pensum no encontrado con ID: " + pensumId);
        }

        return pensumMateriaService.obtenerSoloMateriasDePensum(pensumId);
    }

    public List<PensumMateria> obtenerPensumMateriasPorPensumId(Long pensumId) {
        Pensum pensum = obtenerPensumPorId(pensumId);
        if (pensum == null) {
            throw new RuntimeException("Pensum no encontrado con ID: " + pensumId);
        }

        return pensumMateriaService.obtenerMateriasPensum(pensumId);
    }

    public Pensum crearPensumConMaterias(Pensum pensum, List<Long> materiaIds) {
        Pensum pensumGuardado = pensumRepository.save(pensum);

        if (materiaIds != null && !materiaIds.isEmpty()) {
            pensumMateriaService.asociarMateriasAPensum(pensumGuardado.getId(), materiaIds);
        }

        return pensumGuardado;
    }

    public void asociarMateriaAPensum(Long pensumId, Long materiaId) {
        pensumMateriaService.asociarMateriaAPensum(pensumId, materiaId);
    }

    public void eliminarMateriaDepensum(Long pensumId, Long materiaId) {
        List<com.grupo7.tesis.models.PensumMateria> pensumMaterias = pensumMateriaService
                .obtenerMateriasPensum(pensumId);
        com.grupo7.tesis.models.PensumMateria pensumMateria = pensumMaterias.stream()
                .filter(pm -> pm.getMateria().getId().equals(materiaId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No se encontró la asociación entre el pensum y la materia"));

        pensumMateriaService.eliminarMateriaPensum(pensumMateria.getId());
    }

}
