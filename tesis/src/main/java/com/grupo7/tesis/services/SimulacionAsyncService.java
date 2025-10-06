package com.grupo7.tesis.services;

import com.grupo7.tesis.dtos.SimulacionDTO;
import com.grupo7.tesis.models.Materia;
import com.grupo7.tesis.models.Simulacion;
import com.grupo7.tesis.models.SimulacionJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class SimulacionAsyncService {
    
    @Autowired
    private SimulacionService simulacionService;
    
    @Autowired
    private pensumService pensumService;
    
    @Autowired
    private SimulacionJobService jobService;
    
    @Async("simulacionExecutor")
    public CompletableFuture<Void> ejecutarSimulacionAsync(String jobId, SimulacionDTO simulacionDTO) {
        try {
            // Actualizar estado a EN_PROCESO
            jobService.actualizarEstado(jobId, SimulacionJob.Estado.EN_PROCESO, 
                "Iniciando simulación...");
            
            // Obtener datos necesarios
            List<Materia> materiasPensum = pensumService.obtenerPensum();
            
            // Actualizar progreso
            jobService.actualizarEstado(jobId, SimulacionJob.Estado.EN_PROCESO, 
                "Cargando pensum académico...");
            
            // Ejecutar la simulación (este es tu algoritmo A*)
            jobService.actualizarEstado(jobId, SimulacionJob.Estado.EN_PROCESO, 
                "Ejecutando algoritmo de simulación...");
            
            Map<Integer, Simulacion> resultado = simulacionService.generarSimulacionMultiSemestreAStar(
                simulacionDTO.getProgreso(),
                simulacionDTO.getProyeccion(), 
                simulacionDTO.getProyeccion().getSemestre(), 
                materiasPensum,
                simulacionDTO.getProyeccion().getPriorizaciones(), 
                simulacionDTO.getProyeccion().getPracticaProfesional()
            );
            
            // Guardar resultado
            jobService.establecerResultado(jobId, resultado);
            
        } catch (Exception e) {
            // Manejar errores
            String errorMsg = "Error ejecutando simulación: " + e.getMessage();
            jobService.establecerError(jobId, errorMsg);
            e.printStackTrace();
        }
        
        return CompletableFuture.completedFuture(null);
    }
}
