package com.grupo7.tesis.dtos;

public class SimulacionJobStatusDTO {
    private String jobId;
    private String estado;
    private String mensaje;
    private long tiempoTranscurrido;
    private String error;
    
    public SimulacionJobStatusDTO() {}
    
    public SimulacionJobStatusDTO(String jobId, String estado, String mensaje, long tiempoTranscurrido) {
        this.jobId = jobId;
        this.estado = estado;
        this.mensaje = mensaje;
        this.tiempoTranscurrido = tiempoTranscurrido;
    }
    
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    
    public long getTiempoTranscurrido() { return tiempoTranscurrido; }
    public void setTiempoTranscurrido(long tiempoTranscurrido) { this.tiempoTranscurrido = tiempoTranscurrido; }
    
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
