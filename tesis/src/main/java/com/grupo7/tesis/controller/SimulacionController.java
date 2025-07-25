package com.grupo7.tesis.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.grupo7.tesis.model.Materia;
import com.grupo7.tesis.model.Progreso;
import com.grupo7.tesis.model.Proyeccion;
import com.grupo7.tesis.model.Simulacion;
import com.grupo7.tesis.service.ProgresoService;
import com.grupo7.tesis.service.SimulacionService;
import com.grupo7.tesis.service.ProyeccionService;
import com.grupo7.tesis.service.lecturaService;

@Controller
@RequestMapping("/api/simulacion")
public class SimulacionController {

    @Autowired
    private SimulacionService simulacionService;

    @Autowired
    private ProgresoService progresoService;

    @Autowired
    private ProyeccionService ProyeccionService;

    @Autowired
    private lecturaService lecturaService;

    @GetMapping("/mostrar")
    @ResponseBody
    public Map<Integer, Simulacion> mostrarSimulacion() {
        Map<Integer, Simulacion> simulacion = new HashMap<>();

        Progreso progreso = new Progreso(0, 0, 0, null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        Proyeccion proyeccion = new Proyeccion();

        simulacion = simulacionService.generarSimulacionMultiSemestre(progreso,
                proyeccion, 0, null);

        return simulacion;
    }

}
