package com.grupo7.tesis.models;

public class SimulacionJob {
    public enum Estado {
        PENDIENTE, EN_PROCESO, COMPLETADA, ERROR
    }
    
    private String id;
    private Estado estado;
    private String mensaje;
    private Object resultado;
    private String errorDetalle;
    private long tiempoInicio;
    private long tiempoFin;
    
    public SimulacionJob(String id) {
        this.id = id;
        this.estado = Estado.PENDIENTE;
        this.tiempoInicio = System.currentTimeMillis();
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }
    
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    
    public Object getResultado() { return resultado; }
    public void setResultado(Object resultado) { this.resultado = resultado; }
    
    public String getErrorDetalle() { return errorDetalle; }
    public void setErrorDetalle(String errorDetalle) { this.errorDetalle = errorDetalle; }
    
    public long getTiempoInicio() { return tiempoInicio; }
    public void setTiempoInicio(long tiempoInicio) { this.tiempoInicio = tiempoInicio; }
    
    public long getTiempoFin() { return tiempoFin; }
    public void setTiempoFin(long tiempoFin) { this.tiempoFin = tiempoFin; }
    
    public long getTiempoDuracion() {
        if (tiempoFin > 0) {
            return tiempoFin - tiempoInicio;
        }
        return System.currentTimeMillis() - tiempoInicio;
    }
}
