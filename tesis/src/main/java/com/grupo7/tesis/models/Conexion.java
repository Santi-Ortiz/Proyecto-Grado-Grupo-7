package com.grupo7.tesis.models;

public class Conexion {
    private String origen;
    private String destino;

    public Conexion(String origen, String destino) {
        this.origen = origen;
        this.destino = destino;
    }

    public String getOrigen() {
        return origen;
    }

    public String getDestino() {
        return destino;
    }
}
