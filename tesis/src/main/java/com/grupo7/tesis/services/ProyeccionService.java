package com.grupo7.tesis.services;

import org.springframework.stereotype.Service;

import com.grupo7.tesis.models.Proyeccion;

@Service
public class ProyeccionService {

    public Proyeccion generarProyeccion(int semestre, int creditos, int materias) {

        Proyeccion proyeccion = new Proyeccion();
        proyeccion.setSemestre(semestre);
        proyeccion.setCreditos(creditos);
        proyeccion.setMaterias(materias);

        return proyeccion;
    }

}
