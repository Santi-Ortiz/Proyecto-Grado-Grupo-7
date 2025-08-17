package com.grupo7.tesis.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.grupo7.tesis.repositories.FacultadRepository;

@Service
public class FacultadService {

    @Autowired
    private FacultadRepository facultadRepository;
    
}
