package com.grupo7.tesis.services;

import java.io.InputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupo7.tesis.models.Materia;
import com.grupo7.tesis.models.Pensum;
import com.grupo7.tesis.repositories.MateriaRepository;
import com.grupo7.tesis.repositories.PensumMateriaRepository;
import com.grupo7.tesis.repositories.PensumRepository;

@Service
public class PensumService2 {

    @Autowired 
    private PensumRepository pensumRepository;

    @Autowired
    private PensumMateriaRepository pensumMateriaRepository;

    @Autowired
    private MateriaRepository materiaRepository;

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
        if (pensumRepository.existsById(id)) {
            pensumActualizado.setId(id);
            return pensumRepository.save(pensumActualizado);
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

}
