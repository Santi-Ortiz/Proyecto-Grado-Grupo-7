package com.grupo7.tesis.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.grupo7.tesis.models.*;
import com.grupo7.tesis.repositories.PensumMateriaRepository;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PensumMateriaService {

    @Autowired
    private PensumMateriaRepository pensumMateriaRepository;

    @Autowired
    private MateriaService materiaService;

    @Autowired
    @Lazy
    private PensumService pensumService;

    // Se asocia una materia a un pensum
    public PensumMateria asociarMateriaAPensum(Long pensumId, Long materiaId) {
        Pensum pensum = pensumService.obtenerPensumPorId(pensumId);
        if (pensum == null) {
            throw new RuntimeException("Pensum no encontrado con ID: " + pensumId);
        }

        Materia materia = materiaService.obtenerMateriaPorId(materiaId);
        if (materia == null) {
            throw new RuntimeException("Materia no encontrada con ID: " + materiaId);
        }

        // El semestreEsperado toma el valor del semestre de la materia
        PensumMateria pensumMateria = new PensumMateria(pensum, materia, materia.getSemestre());

        return pensumMateriaRepository.save(pensumMateria);
    }

    // Se asocian múltiples materias a un pensum
    @Transactional
    public List<PensumMateria> asociarMateriasAPensum(Long pensumId, List<Long> materiaIds) {
        Pensum pensum = pensumService.obtenerPensumPorId(pensumId);
        if (pensum == null) {
            throw new RuntimeException("Pensum no encontrado con ID: " + pensumId);
        }

        List<PensumMateria> pensumMaterias = materiaIds.stream()
                .map(materiaId -> {
                    Materia materia = materiaService.obtenerMateriaPorId(materiaId);
                    if (materia == null) {
                        throw new RuntimeException("Materia no encontrada con ID: " + materiaId);
                    }
                    return new PensumMateria(pensum, materia, materia.getSemestre());
                })
                .collect(Collectors.toList());

        return pensumMateriaRepository.saveAll(pensumMaterias);
    }

    // Se obtienen todas las materias de un pensum
    public List<PensumMateria> obtenerMateriasPensum(Long pensumId) {
        return pensumMateriaRepository.findByPensumId(pensumId);
    }

    // Se obtienen solo las materias (sin la relación completa)
    public List<Materia> obtenerSoloMateriasDePensum(Long pensumId) {
        return pensumMateriaRepository.findMateriasByPensumId(pensumId);
    }

    // Se obtienen materias ordenadas por semestre
    public List<PensumMateria> obtenerMateriasOrdenadas(Long pensumId) {
        return pensumMateriaRepository.findByPensumIdOrderBySemestreEsperado(pensumId);
    }

    // Se obtienen materias de un semestre específico
    public List<PensumMateria> obtenerMateriasPorSemestre(Long pensumId, int semestre) {
        return pensumMateriaRepository.findByPensumIdAndSemestreEsperado(pensumId, semestre);
    }

    // Se elimina una materia de un pensum
    public void eliminarMateriaPensum(Long pensumMateriaId) {
        if (!pensumMateriaRepository.existsById(pensumMateriaId)) {
            throw new RuntimeException("Relación PensumMateria no encontrada con ID: " + pensumMateriaId);
        }
        pensumMateriaRepository.deleteById(pensumMateriaId);
    }

    // Se limpian todas las materias de un pensum
    @Transactional
    public void limpiarMateriasPensum(Long pensumId) {
        pensumMateriaRepository.deleteByPensumId(pensumId);
    }

}
