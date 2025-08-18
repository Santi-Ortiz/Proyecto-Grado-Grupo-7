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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.grupo7.tesis.models.Materia;
import com.grupo7.tesis.models.Pensum;
import com.grupo7.tesis.services.PensumService;

@RestController
@RequestMapping ("/api/pensum")
public class PensumController {

    @Autowired
    private PensumService pensumService;

    @GetMapping("/todos")
    public List<Pensum> getAllPensums() throws Exception {
        return pensumService.obtenerPensums();
    }

    @PostMapping
    public Pensum createPensum(@RequestBody Pensum pensum) {
        return pensumService.crearPensum(pensum);
    }

    @GetMapping("/{id}")
    public Pensum getPensumById(@PathVariable Long id) {
        return pensumService.obtenerPensumPorId(id);
    }

    @PutMapping("/{id}")
    public Pensum updatePensum(@PathVariable Long id, @RequestBody Pensum pensum) {
        return pensumService.actualizarPensum(id, pensum);
    }

    @DeleteMapping("/{id}")
    public void deletePensum(@PathVariable Long id) {
        pensumService.eliminarPensum(id);
    }

    @GetMapping
    @ResponseBody
    public List<Materia> obtenerPensumJson() throws Exception {
        return pensumService.obtenerPensum();
    }

}
