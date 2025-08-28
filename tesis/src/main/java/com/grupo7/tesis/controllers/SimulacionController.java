package com.grupo7.tesis.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.grupo7.tesis.dtos.SimulacionDTO;
import com.grupo7.tesis.dtos.SimulacionJobStatusDTO;
import com.grupo7.tesis.models.Materia;
import com.grupo7.tesis.models.Simulacion;
import com.grupo7.tesis.models.SimulacionJob;
import com.grupo7.tesis.services.SimulacionAsyncService;
import com.grupo7.tesis.services.SimulacionJobService;
import com.grupo7.tesis.services.SimulacionService;
import com.grupo7.tesis.services.pensumService;

@RestController
@RequestMapping("/api/simulacion")
public class SimulacionController {

    @Autowired
    private SimulacionAsyncService simulacionAsyncService;
    
    @Autowired
    private SimulacionJobService jobService;

    @Autowired
    private SimulacionService simulacionService;

    @Autowired
    private pensumService pensumService;

    @PostMapping("/generar")
    @ResponseBody
    public Map<Integer, Simulacion> generarSimulacion(@RequestBody SimulacionDTO simulacionDTO) throws Exception {

        Map<Integer, Simulacion> simulacion = new HashMap<>();

        List<Materia> materiasPensum = pensumService.obtenerPensum();

        simulacion = simulacionService.generarSimulacionMultiSemestreAStar(simulacionDTO.getProgreso(),
                simulacionDTO.getProyeccion(), simulacionDTO.getProyeccion().getSemestre(), materiasPensum,
                simulacionDTO.getPriorizaciones());

        return simulacion;
    }

    @PostMapping("/iniciar")
    @ResponseBody
    public ResponseEntity<Map<String, String>> iniciarSimulacion(@RequestBody SimulacionDTO simulacionDTO) {
        try {
            String jobId = jobService.crearTrabajo();
            
            simulacionAsyncService.ejecutarSimulacionAsync(jobId, simulacionDTO);
            
            Map<String, String> respuesta = new HashMap<>();
            respuesta.put("jobId", jobId);
            respuesta.put("mensaje", "Simulación iniciada. Use el jobId para consultar el estado.");
            respuesta.put("estado", "PENDIENTE");
            
            return ResponseEntity.ok(respuesta);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error iniciando simulación: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/estado/{jobId}")
    @ResponseBody
    public ResponseEntity<SimulacionJobStatusDTO> consultarEstado(@PathVariable String jobId) {
        SimulacionJob job = jobService.obtenerTrabajo(jobId);
        
        if (job == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        SimulacionJobStatusDTO respuesta = new SimulacionJobStatusDTO(
            jobId, 
            job.getEstado().toString(), 
            job.getMensaje(), 
            job.getTiempoDuracion()
        );
        
        if (job.getEstado() == SimulacionJob.Estado.ERROR) {
            respuesta.setError(job.getErrorDetalle());
        }
        
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/resultado/{jobId}")
    @ResponseBody
    public ResponseEntity<Object> obtenerResultado(@PathVariable String jobId) {
        SimulacionJob job = jobService.obtenerTrabajo(jobId);
        
        if (job == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Trabajo no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        
        if (job.getEstado() != SimulacionJob.Estado.COMPLETADA) {
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("estado", job.getEstado().toString());
            respuesta.put("mensaje", "La simulación aún no está completada");
            return ResponseEntity.ok(respuesta);
        }
        
        return ResponseEntity.ok(job.getResultado());
    }
}