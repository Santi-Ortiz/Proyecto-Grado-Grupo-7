package com.grupo7.tesis.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.grupo7.tesis.models.Estudiante;
import com.grupo7.tesis.repositories.EstudianteRepository;

@Service
public class EstudianteService {

    private final EstudianteRepository estudianteRepository;

    @Autowired
    public EstudianteService(EstudianteRepository estudianteRepository) {
        this.estudianteRepository = estudianteRepository;
    }

    public List<Estudiante> obtenerTodosEstudiantes() {
        return estudianteRepository.findAll();
    }

    public Estudiante obtenerEstudiantePorId(Long id) {
        return estudianteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado con ID: " + id));
    }

    public Estudiante crearEstudiante(Estudiante estudiante) {
        return estudianteRepository.save(estudiante);
    }

    public Estudiante actualizarEstudiante(Long id, Estudiante estudiante) {
        Estudiante estudianteExistente = obtenerEstudiantePorId(id);
        estudianteExistente = estudiante;
        return estudianteRepository.save(estudianteExistente);
    }

    public void eliminarEstudiante(Long id) {
        Estudiante estudiante = obtenerEstudiantePorId(id);
        estudianteRepository.delete(estudiante);
    }

}
