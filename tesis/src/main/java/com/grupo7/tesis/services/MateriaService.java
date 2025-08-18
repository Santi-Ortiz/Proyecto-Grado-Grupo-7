package com.grupo7.tesis.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.grupo7.tesis.models.Materia;
import com.grupo7.tesis.repositories.MateriaRepository;

@Service
public class MateriaService {

    @Autowired
    private MateriaRepository materiaRepository;

    public List<Materia> obtenerMaterias() {
        return materiaRepository.findAll();
    }

    public Materia crearMateria(Materia materia) {
        return materiaRepository.save(materia);
    }

    public Materia obtenerMateriaPorId(Long id) {
        return materiaRepository.findById(id).orElse(null);
    }

    public Materia actualizarMateria(Long id, Materia materia) {
        if (materiaRepository.existsById(id)) {
            materia.setId(id);
            return materiaRepository.save(materia);
        }
        return null;
    }

    public void eliminarMateria(Long id) {
        materiaRepository.deleteById(id);
    }

}
