package com.grupo7.tesis.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grupo7.tesis.services.ProyeccionService;
import com.grupo7.tesis.models.Proyeccion;


@RestController
@RequestMapping("/api/proyecciones")
public class ProyeccionController {

    @Autowired
    private ProyeccionService proyeccionService;

    @GetMapping
    public List<Proyeccion> obtenerTodasProyecciones() {
        return proyeccionService.obtenerTodasProyecciones();
    }

    @GetMapping("/{id}")
    public Proyeccion obtenerProyeccionPorId(@PathVariable Long id) {
        return proyeccionService.obtenerProyeccionPorId(id);
    }

    @PostMapping
    public Proyeccion crearProyeccion(@RequestBody Proyeccion proyeccion) {
        return proyeccionService.crearProyeccion(proyeccion);
    }

    @PutMapping("/{id}")
    public Proyeccion actualizarProyeccion(@PathVariable Long id, @RequestBody Proyeccion proyeccion) {
        return proyeccionService.actualizarProyeccion(id, proyeccion);
    }

    @DeleteMapping("/eliminar/{id}")
    public Proyeccion eliminarProyeccion(@PathVariable Long id) {
        return proyeccionService.eliminarProyeccion(id);
    }

    @GetMapping("/existe/nombre/{nombre}")
    public ResponseEntity<Boolean> existeProyeccionConNombre(@PathVariable String nombre) {
        System.out.println(">> Llamada a existeProyeccionConNombre con nombre=" + nombre);
        boolean existe = proyeccionService.existeProyeccionConNombre(nombre);
        return ResponseEntity.ok(existe);
    }

    @GetMapping("/mis-proyecciones")
    public ResponseEntity<List<Proyeccion>> obtenerProyeccionesEstudianteAutenticado() {
        List<Proyeccion> proyecciones = proyeccionService.obtenerProyeccionesEstudianteAutenticado();
        return ResponseEntity.ok(proyecciones);
    }

    

}
