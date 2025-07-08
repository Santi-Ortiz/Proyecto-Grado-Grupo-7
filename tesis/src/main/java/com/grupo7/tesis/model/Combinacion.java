package com.grupo7.tesis.model;

import java.util.ArrayList;
import java.util.List;

public class Combinacion {
        private List<MateriaJson> materias;
        private double puntajeTotal;
        private int creditosTotales;
        
        public Combinacion(List<MateriaJson> materias, double puntaje, int creditos) {
            this.materias = new ArrayList<>(materias);
            this.puntajeTotal = puntaje;
            this.creditosTotales = creditos;
        }
        
        public List<MateriaJson> getMaterias() { return materias; }
        public double getPuntajeTotal() { return puntajeTotal; }
        public int getCreditosTotales() { return creditosTotales; }
    }
