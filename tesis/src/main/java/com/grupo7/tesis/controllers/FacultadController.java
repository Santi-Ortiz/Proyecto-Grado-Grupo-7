package com.grupo7.tesis.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grupo7.tesis.models.Facultad;
import com.grupo7.tesis.services.FacultadService;

@RestController
@RequestMapping("/api/facultad")
public class FacultadController {

    @Autowired
    private FacultadService facultadService;

    @GetMapping("/todas")
    public List<Facultad> getAllFacultades() {
        return facultadService.obtenerTodasFacultades();
    }

    @GetMapping("/{id}")
    public Facultad getFacultadById(@PathVariable Long id) {
        return facultadService.obtenerFacultadPorId(id);
    }

    @PostMapping
    public Facultad addFacultad(@RequestBody Facultad facultad) {
        return facultadService.crearFacultad(facultad);
    }

    @PutMapping("/{id}")
    public Facultad updateFacultad(@PathVariable Long id, @RequestBody Facultad facultad) {
        return facultadService.actualizarFacultad(id, facultad);
    }

    @DeleteMapping("/{id}")
    public Facultad deleteFacultad(@PathVariable Long id) {
        return facultadService.eliminarFacultad(id);
    }

}
