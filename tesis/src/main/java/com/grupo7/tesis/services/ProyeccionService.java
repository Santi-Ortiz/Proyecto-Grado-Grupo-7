package com.grupo7.tesis.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.grupo7.tesis.models.Proyeccion;
import com.grupo7.tesis.repositories.ProyeccionRepository;

@Service
public class ProyeccionService {

    @Autowired
    private ProyeccionRepository proyeccionRepository;

    public Proyeccion generarProyeccion(int semestre, int creditos, int materias) {

        Proyeccion proyeccion = new Proyeccion();
        proyeccion.setSemestre(semestre);
        proyeccion.setnumMaxCreditos(creditos);
        proyeccion.setnumMaxMaterias(materias);

        return proyeccion;
    }

}
