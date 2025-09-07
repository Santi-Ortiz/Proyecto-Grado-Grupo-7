package com.grupo7.tesis.services;

import com.grupo7.tesis.models.SimulacionJob;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

@Service
public class SimulacionJobService {
    
    private final Map<String, SimulacionJob> trabajos = new ConcurrentHashMap<>();
    
    public String crearTrabajo() {
        String jobId = UUID.randomUUID().toString();
        SimulacionJob job = new SimulacionJob(jobId);
        trabajos.put(jobId, job);
        return jobId;
    }
    
    public SimulacionJob obtenerTrabajo(String jobId) {
        return trabajos.get(jobId);
    }
    
    public void actualizarEstado(String jobId, SimulacionJob.Estado estado, String mensaje) {
        SimulacionJob job = trabajos.get(jobId);
        if (job != null) {
            job.setEstado(estado);
            job.setMensaje(mensaje);
            if (estado == SimulacionJob.Estado.COMPLETADA || estado == SimulacionJob.Estado.ERROR) {
                job.setTiempoFin(System.currentTimeMillis());
            }
        }
    }
    
    public void establecerResultado(String jobId, Object resultado) {
        SimulacionJob job = trabajos.get(jobId);
        if (job != null) {
            job.setResultado(resultado);
            job.setEstado(SimulacionJob.Estado.COMPLETADA);
            job.setTiempoFin(System.currentTimeMillis());
            job.setMensaje("Simulación completada   ");
        }
    }
    
    public void establecerError(String jobId, String error) {
        SimulacionJob job = trabajos.get(jobId);
        if (job != null) {
            job.setEstado(SimulacionJob.Estado.ERROR);
            job.setErrorDetalle(error);
            job.setTiempoFin(System.currentTimeMillis());
            job.setMensaje("Error en la simulación");
        }
    }
    
}
