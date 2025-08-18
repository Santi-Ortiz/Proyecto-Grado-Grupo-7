package com.grupo7.tesis.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grupo7.tesis.models.Estudiante;
import com.grupo7.tesis.services.EstudianteService;

@RestController
@RequestMapping("/api/estudiantes")
public class EstudianteController {

    private final EstudianteService estudianteService;

    public EstudianteController(EstudianteService estudianteService) {
        this.estudianteService = estudianteService;
    }

    @GetMapping("/todos")
    public List<Estudiante> obtenerTodosEstudiantes() {
        return estudianteService.obtenerTodosEstudiantes();
    }

}
