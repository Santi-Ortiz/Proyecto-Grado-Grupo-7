package com.grupo7.tesis.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.grupo7.tesis.services.MateriaService;

@RestController
public class MateriaController {

    @Autowired
    private MateriaService materiaService;
    
}
