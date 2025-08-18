package com.grupo7.tesis.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.grupo7.tesis.dtos.SimulacionDTO;
import com.grupo7.tesis.models.Materia;
import com.grupo7.tesis.models.Simulacion;
import com.grupo7.tesis.services.SimulacionService;

import lombok.Delegate;

import com.grupo7.tesis.services.PensumService;

@RestController
@RequestMapping("/api/simulaciones")
public class SimulacionController {

    @Autowired
    private SimulacionService simulacionService;

    @Autowired
    private PensumService pensumService;

    @GetMapping
    public List<Simulacion> obtenerSimulaciones() {
        return simulacionService.obtenerTodasSimulaciones();
    }

    @GetMapping("/{id}")
    public Optional<Simulacion> obtenerSimulacion(@PathVariable Long id) {
        return simulacionService.obtenerSimulacionPorId(id);
    }

    @PostMapping("/generar")
    public Map<Integer, Simulacion> generarSimulacion(@RequestBody SimulacionDTO simulacionDTO) throws Exception {

        Map<Integer, Simulacion> simulacion = new HashMap<>();

        List<Materia> materiasPensum = pensumService.obtenerPensum();

        simulacion = simulacionService.generarSimulacionMultiSemestreAStar(simulacionDTO.getProgreso(),
                simulacionDTO.getProyeccion(), simulacionDTO.getProyeccion().getSemestre(), materiasPensum,
                simulacionDTO.getPriorizaciones());

        return simulacion;
    }

    @DeleteMapping("/{id}")
    public void eliminarSimulacion(@PathVariable Long id) {
        simulacionService.eliminarSimulacion(id);
    }

}
