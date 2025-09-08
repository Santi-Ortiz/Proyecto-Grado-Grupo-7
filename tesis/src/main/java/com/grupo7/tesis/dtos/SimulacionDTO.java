package com.grupo7.tesis.dtos;

import com.grupo7.tesis.models.Progreso;
import com.grupo7.tesis.models.Proyeccion;

public class SimulacionDTO {
    private Progreso progreso;
    private Proyeccion proyeccion;
    private boolean[] priorizaciones;
    private boolean practicaProfesional;

    public boolean[] getPriorizaciones() {
        return this.priorizaciones;
    }

    public void setPriorizaciones(boolean[] priorizaciones) {
        this.priorizaciones = priorizaciones;
    }

    public Progreso getProgreso() {
        return this.progreso;
    }

    public void setProgreso(Progreso progreso) {
        this.progreso = progreso;
    }

    public Proyeccion getProyeccion() {
        return this.proyeccion;
    }

    public void setProyeccion(Proyeccion proyeccion) {
        this.proyeccion = proyeccion;
    }
    public boolean getPracticaProfesional() {
        return practicaProfesional;
    }

    public void setPracticaProfesional(boolean practicaProfesional) {
        this.practicaProfesional = practicaProfesional;
    }

}
