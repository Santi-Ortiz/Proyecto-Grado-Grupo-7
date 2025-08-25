package com.grupo7.tesis.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.grupo7.tesis.services.PensumMateriaService;
import com.grupo7.tesis.models.PensumMateria;
import java.util.List;

@RestController
@RequestMapping("/api/pensum-materias")
public class PensumMateriaController {

    @Autowired
    private PensumMateriaService pensumMateriaService;

    // Se asocia una materia a un pensum
    @PostMapping("/asociar/{pensumId}/{materiaId}")
    public ResponseEntity<PensumMateria> asociarMateria(
            @PathVariable Long pensumId,
            @PathVariable Long materiaId) {
        try {
            PensumMateria result = pensumMateriaService.asociarMateriaAPensum(pensumId, materiaId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Se asocian múltiples materias a un pensum
    @PostMapping("/asociar-multiples/{pensumId}")
    public ResponseEntity<List<PensumMateria>> asociarMaterias(
            @PathVariable Long pensumId,
            @RequestBody List<Long> materiaIds) {
        try {
            List<PensumMateria> result = pensumMateriaService.asociarMateriasAPensum(pensumId, materiaIds);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Se obtienen todas las materias de un pensum con información completa
    @GetMapping("/pensum/{pensumId}")
    public ResponseEntity<List<PensumMateria>> obtenerMateriasPensum(@PathVariable Long pensumId) {
        try {
            List<PensumMateria> materias = pensumMateriaService.obtenerMateriasPensum(pensumId);
            return ResponseEntity.ok(materias);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Se obtienen pensums que contienen una materia específica
    @GetMapping("/materia/{materiaId}")
    public ResponseEntity<List<PensumMateria>> obtenerPensumsMateria(@PathVariable Long materiaId) {
        try {
            List<PensumMateria> pensums = pensumMateriaService.obtenerMateriasPensum(materiaId);
            return ResponseEntity.ok(pensums);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Se elimina una asociación específica
    @DeleteMapping("/{pensumMateriaId}")
    public ResponseEntity<String> eliminarAsociacion(@PathVariable Long pensumMateriaId) {
        try {
            pensumMateriaService.eliminarMateriaPensum(pensumMateriaId);
            return ResponseEntity.ok("Asociación eliminada exitosamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Se limpian todas las materias de un pensum
    @DeleteMapping("/limpiar/{pensumId}")
    public ResponseEntity<String> limpiarMateriasPensum(@PathVariable Long pensumId) {
        try {
            pensumMateriaService.limpiarMateriasPensum(pensumId);
            return ResponseEntity.ok("Todas las materias del pensum han sido eliminadas");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

}
