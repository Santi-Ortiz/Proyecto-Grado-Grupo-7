package com.grupo7.tesis.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.grupo7.tesis.dtos.SimulacionDTO;
import com.grupo7.tesis.models.Materia;
import com.grupo7.tesis.models.Simulacion;
import com.grupo7.tesis.services.SimulacionService;
import com.grupo7.tesis.services.pensumService;

@RestController
@RequestMapping("/api/simulacion")
public class SimulacionController {

    @Autowired
    private SimulacionService simulacionService;

    @Autowired
    private pensumService pensumService;

    @PostMapping("/generar")
    @ResponseBody
    public Map<Integer, Simulacion> generarSimulacion(@RequestBody SimulacionDTO simulacionDTO) throws Exception {

        Map<Integer, Simulacion> simulacion = new HashMap<>();

        List<Materia> materiasPensum = pensumService.obtenerPensum();

        simulacion = simulacionService.generarSimulacionMultiSemestreAStar(simulacionDTO.getProgreso(),
                simulacionDTO.getProyeccion(), simulacionDTO.getProyeccion().getSemestre(), materiasPensum,
                simulacionDTO.getPriorizaciones());

        return simulacion;
    }

}
