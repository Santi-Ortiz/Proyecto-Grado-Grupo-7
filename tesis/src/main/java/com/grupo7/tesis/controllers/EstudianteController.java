package com.grupo7.tesis.controllers;

import java.util.Collection;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grupo7.tesis.models.Estudiante;
import com.grupo7.tesis.repositories.EstudianteRepository;

@RestController
@RequestMapping("/api/estudiantes")
public class EstudianteController {

    private final EstudianteRepository estudianteRepository;

    public EstudianteController(EstudianteRepository estudianteRepository) {
        this.estudianteRepository = estudianteRepository;
    }

    @GetMapping("/todos")
    public Collection<Estudiante> getAllEstudiantes() {
        return estudianteRepository.findAll();
    }

}
