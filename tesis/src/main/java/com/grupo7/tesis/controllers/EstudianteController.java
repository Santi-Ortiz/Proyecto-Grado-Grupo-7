package com.grupo7.tesis.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.grupo7.tesis.dtos.EstudianteDTO;
import com.grupo7.tesis.models.Estudiante;
import com.grupo7.tesis.models.Facultad;
import com.grupo7.tesis.models.Pensum;
import com.grupo7.tesis.services.EstudianteService;


@RestController
@RequestMapping("/api/estudiantes")
public class EstudianteController {

    private final EstudianteService estudianteService;

    public EstudianteController(EstudianteService estudianteService) {
        this.estudianteService = estudianteService;
    }

    @GetMapping
    public List<Estudiante> obtenerTodosEstudiantes() {
        return estudianteService.obtenerTodosEstudiantes();
    }

    @GetMapping("/{id}")
    public Estudiante obtenerEstudiantePorId(@PathVariable Long id) {
        return estudianteService.obtenerEstudiantePorId(id);
    }

    @GetMapping("/{correo}")
    public Estudiante obtenerEstudiantePorCorreo(@PathVariable String correo) {
        return estudianteService.obtenerEstudiantePorCorreo(correo);
    }
    

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Estudiante crearEstudiante(@RequestBody EstudianteDTO estudianteDTO) {
        return estudianteService.crearEstudiante(estudianteDTO);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Estudiante actualizarEstudiante(@PathVariable Long id, @RequestBody Estudiante estudiante) {
        return estudianteService.actualizarEstudiante(id, estudiante);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarEstudiante(@PathVariable Long id) {
        estudianteService.eliminarEstudiante(id);
    }

    /* Acceder a los objetos Pensum y Facultad */

    @GetMapping("/{id}/pensum")
    public Pensum obtenerPensumEstudiante(@PathVariable Long id) {
        return estudianteService.obtenerPensumEstudiante(id);
    }

    @GetMapping("/{id}/facultad")
    public Facultad obtenerFacultadEstudiante(@PathVariable Long id) {
        return estudianteService.obtenerFacultadEstudiante(id);
    }

}
