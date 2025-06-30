package com.grupo7.tesis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.grupo7.tesis.model.Simulacion;
import com.grupo7.tesis.service.ProgresoService;
import com.grupo7.tesis.service.SimulacionService;
import com.grupo7.tesis.service.ProyeccionService;

@Controller
public class SimulacionController {

    @Autowired
    private SimulacionService simulacionService;

    @Autowired
    private ProgresoService progresoService;

    @Autowired
    private ProyeccionService ProyeccionService;

    @GetMapping("/simulacion")
    public Simulacion mostrarMaterias() {
        Simulacion simulacion = new Simulacion();
        return simulacion;
    }

}
