package com.grupo7.tesis.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grupo7.tesis.services.FacultadService;

@RestController
@RequestMapping("/api/facultad")
public class FacultadController {
    
    @Autowired
    private FacultadService facultadService;

}
