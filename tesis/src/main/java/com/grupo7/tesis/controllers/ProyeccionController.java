package com.grupo7.tesis.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grupo7.tesis.services.ProyeccionService;

@RestController
@RequestMapping("/api/proyeccion")
public class ProyeccionController {

    @Autowired
    private ProyeccionService proyeccionService;
    
}
