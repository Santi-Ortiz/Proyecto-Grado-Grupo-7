package com.grupo7.tesis.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.grupo7.tesis.dtos.Materia;
import com.grupo7.tesis.dtos.SimulacionDTO;
import com.grupo7.tesis.models.MateriaJson;
import com.grupo7.tesis.models.Progreso;
import com.grupo7.tesis.models.Proyeccion;
import com.grupo7.tesis.models.Simulacion;
import com.grupo7.tesis.services.ProgresoService;
import com.grupo7.tesis.services.ProyeccionService;
import com.grupo7.tesis.services.SimulacionService;
import com.grupo7.tesis.services.lecturaService;
import com.grupo7.tesis.services.pensumService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/simulacion")
public class SimulacionController {

    @Autowired
    private SimulacionService simulacionService;

    @Autowired
    private ProgresoService progresoService;

    @Autowired
    private ProyeccionService proyeccionService;

    @Autowired
    private lecturaService lecturaService;

    @Autowired
    private pensumService pensumService;

    @PostMapping("/generar")
    @ResponseBody
    public Map<Integer, Simulacion> generarSimulacion(@RequestBody SimulacionDTO simulacionDTO) throws Exception {

        Map<Integer, Simulacion> simulacion = new HashMap<>();

        List<MateriaJson> materiasPensum = pensumService.obtenerPensum();
        /*
         * System.out.println("\n==========================");
         * System.out.println("Progreso desde simulacionDTO: " +
         * simulacionDTO.getProgreso());
         * System.out.println("Proyeccion desde simulacionDTO: " +
         * simulacionDTO.getProyeccion());
         * System.out.println("==========================\n");
         */

        simulacion = simulacionService.generarSimulacionMultiSemestreAStar(simulacionDTO.getProgreso(),
                simulacionDTO.getProyeccion(), simulacionDTO.getProyeccion().getSemestre(), materiasPensum,
                simulacionDTO.getPriorizaciones());

        return simulacion;
    }

}
