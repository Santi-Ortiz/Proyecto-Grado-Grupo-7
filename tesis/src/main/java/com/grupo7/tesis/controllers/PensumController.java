package com.grupo7.tesis.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.grupo7.tesis.models.Pensum;
import com.grupo7.tesis.models.PensumMateria;
import com.grupo7.tesis.services.PensumService;

@RestController
@RequestMapping("/api/pensums")
public class PensumController {

    @Autowired
    private PensumService pensumService;

    @GetMapping("/todos")
    public List<Pensum> getAllPensums() {
        return pensumService.obtenerPensums();
    }

    @GetMapping("/{id}")
    public Pensum getPensumById(@PathVariable Long id) {
        return pensumService.obtenerPensumPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Pensum createPensum(@RequestBody Pensum pensum) {
        return pensumService.crearPensum(pensum);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public Pensum updatePensum(@PathVariable Long id, @RequestBody Pensum pensum) {
        return pensumService.actualizarPensum(id, pensum);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePensum(@PathVariable Long id) {
        pensumService.eliminarPensum(id);
    }

    // Devuelve la lista de materias a partir de la lectura del archivo Json
    @GetMapping
    public List<Materia> obtenerPensumJson() throws Exception {
        return pensumService.obtenerPensumJson();
    }

    /* ENDPOINTS PARA MATERIAS ASOCIADAS A PENSUMS */

    // Se obtienen solo las materias de un pensum
    @GetMapping("/{id}/materias")
    public ResponseEntity<List<Materia>> obtenerMateriasDePensum(@PathVariable Long id) {
        // A partir de una lista de Materias, se obtienen las materias asociadas a un pensum
        try {
            List<Materia> materias = pensumService.obtenerMateriasPorPensumId(id);
            return ResponseEntity.ok(materias);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Se obtiene la información completa, es decir, materias y semestre esperado
    @GetMapping("/{id}/pensum-materias")
    public ResponseEntity<List<PensumMateria>> obtenerMateriasDePensumCompleto(@PathVariable Long id) {
        // A partir de una lista de PensumMateria se obtienen las materias y el semestre esperado
        try {
            List<PensumMateria> pensumMaterias = pensumService.obtenerPensumMateriasPorPensumId(id);
            return ResponseEntity.ok(pensumMaterias);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Se asocia una materia a un pensum
    @PostMapping("/{pensumId}/materias/{materiaId}")
    public ResponseEntity<String> asociarMateriaAPensum(@PathVariable Long pensumId, @PathVariable Long materiaId) {
        try {
            pensumService.asociarMateriaAPensum(pensumId, materiaId);
            return ResponseEntity.ok("Materia asociada exitosamente al pensum");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Eliminar una materia de un pensum
    @DeleteMapping("/{pensumId}/materias/{materiaId}")
    public ResponseEntity<String> eliminarMateriaDepensum(@PathVariable Long pensumId, @PathVariable Long materiaId) {
        try {
            pensumService.eliminarMateriaDepensum(pensumId, materiaId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

}
