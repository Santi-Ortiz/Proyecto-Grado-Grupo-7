package com.grupo7.tesis.service;

import org.springframework.stereotype.Service;

import com.grupo7.tesis.model.Progreso;
import com.grupo7.tesis.model.Proyeccion;

@Service
public class ProyeccionService {

    public Proyeccion generarProyeccion(int semestre, int creditos, int materias, int tipoMatricula,
            int doblePrograma) {

        Proyeccion proyeccion = new Proyeccion();
        proyeccion.setSemestre(semestre);
        proyeccion.setCreditos(creditos);
        proyeccion.setMaterias(materias);
        proyeccion.setTipoMatricula(tipoMatricula);
        proyeccion.setDoblePrograma(doblePrograma);

        return proyeccion;
    }

}
