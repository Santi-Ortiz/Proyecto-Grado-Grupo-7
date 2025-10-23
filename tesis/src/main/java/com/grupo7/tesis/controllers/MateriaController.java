package com.grupo7.tesis.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.grupo7.tesis.models.Materia;
import com.grupo7.tesis.services.MateriaService;

@RestController
@RequestMapping("/api/materias")
public class MateriaController {

    @Autowired
    private MateriaService materiaService;

    @GetMapping
    public List<Materia> obtenerMaterias() {
        return materiaService.obtenerMaterias();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Materia crearMateria(@RequestBody Materia materia) {
        return materiaService.crearMateria(materia);
    }

    @PostMapping("/cargar-desde-json")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Materia> cargarMateriasDesdeJson() {
        try {
            return materiaService.crearMateriasDesdeJson();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @GetMapping("/{id}")
    public Materia obtenerMateriaPorId(@PathVariable Long id) {
        return materiaService.obtenerMateriaPorId(id);
    }

    @PutMapping("/{id}")
    public Materia actualizarMateria(@PathVariable Long id, @RequestBody Materia materia) {
        return materiaService.actualizarMateria(id, materia);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarMateria(@PathVariable Long id) {
        materiaService.eliminarMateria(id);
    }

}
