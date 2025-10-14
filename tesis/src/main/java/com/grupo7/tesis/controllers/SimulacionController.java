package com.grupo7.tesis.controllers;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.grupo7.tesis.dtos.SimulacionDTO;
import com.grupo7.tesis.dtos.SimulacionJobStatusDTO;
import com.grupo7.tesis.models.Materia;
import com.grupo7.tesis.models.Simulacion;
import com.grupo7.tesis.models.SimulacionJob;
import com.grupo7.tesis.services.SimulacionAsyncService;
import com.grupo7.tesis.services.SimulacionJobService;
import com.grupo7.tesis.services.SimulacionService;
import com.grupo7.tesis.services.PensumService;
import com.grupo7.tesis.models.Proyeccion;

@RestController
@RequestMapping("/api/simulaciones")
public class SimulacionController {

    @Autowired
    private SimulacionAsyncService simulacionAsyncService;
    
    @Autowired
    private SimulacionJobService jobService;

    @Autowired
    private SimulacionService simulacionService;

    @Autowired
    private PensumService pensumService;

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Object> obtenerSimulacion(@PathVariable Long id) {
        Map<Integer, Simulacion> resultado = (Map<Integer, Simulacion>) simulacionService.obtenerSimulacionPorId(id);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/generar")
    public List<Simulacion> generarSimulacion(@RequestBody SimulacionDTO simulacionDTO, Principal principal) throws Exception {
        String correo = principal.getName();

        List<Materia> materiasPensum = pensumService.obtenerPensumJson();

        return simulacionService.generarSimulacionMultiSemestreAStar(
            simulacionDTO.getProgreso(),
            simulacionDTO.getProyeccion(),
            simulacionDTO.getProyeccion().getSemestre(),
            materiasPensum,
            simulacionDTO.getProyeccion().getPriorizaciones(),
            simulacionDTO.getProyeccion().getPracticaProfesional(),
            correo
        ).values().stream().toList();
    }

    @PostMapping("/iniciar")
    @ResponseBody
    public ResponseEntity<Map<String, String>> iniciarSimulacion(@RequestBody SimulacionDTO simulacionDTO, Principal principal) {
        try {
            String correo = principal.getName();
            String jobId = jobService.crearTrabajo();
            
            simulacionAsyncService.ejecutarSimulacionAsync(jobId, simulacionDTO, correo);
            
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
        
        @SuppressWarnings("unchecked")
        Map<Integer, Simulacion> resultado = (Map<Integer, Simulacion>) job.getResultado();


        return ResponseEntity.ok(resultado);
    }

    @DeleteMapping("eliminarSimulacion/{id}")
    public void eliminarSimulacion(@PathVariable Long id) {
        simulacionService.eliminarSimulacion(id);
    }

    @PostMapping("/guardarSimulacion")
    @ResponseBody
    public ResponseEntity<Object> guardarSimulacion(@RequestBody Map<String, Object> body, Principal principal) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            try {
                SimulacionDTO simulacionDTO = null;
                if (body.containsKey("simulacionDTO")) {
                    try {
                        Object simDtoObj = body.get("simulacionDTO");
                        if (simDtoObj instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> simDtoMap = (Map<String, Object>) simDtoObj;
                            if (simDtoMap.containsKey("proyeccion")) {
                                Proyeccion proy = mapper.convertValue(simDtoMap.get("proyeccion"), Proyeccion.class);
                                simulacionDTO = new SimulacionDTO();
                                simulacionDTO.setProyeccion(proy);
                            } else {
                                simulacionDTO = mapper.convertValue(simDtoObj, SimulacionDTO.class);
                            }
                        } else {
                            simulacionDTO = mapper.convertValue(simDtoObj, SimulacionDTO.class);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        simulacionDTO = null;
                    }
                }

                Map<Integer, Simulacion> simulacionMap = new HashMap<>();
                if (body.containsKey("resultadoSimulacion")) {
                    TypeReference<Map<String, Object>> typeRefObj = new TypeReference<>() {};
                    Map<String, Object> tempObj = mapper.convertValue(body.get("resultadoSimulacion"), typeRefObj);
                    if (tempObj != null) {
                        for (Map.Entry<String, Object> e : tempObj.entrySet()) {
                            try {
                                Integer key = Integer.valueOf(e.getKey());

                                Object rawVal = e.getValue();
                                if (rawVal instanceof Map) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> valMap = (Map<String, Object>) rawVal;
                                    if (valMap.containsKey("totalCreditos") && !valMap.containsKey("creditosTotales")) {
                                        valMap.put("creditosTotales", valMap.get("totalCreditos"));
                                    }
                                    Simulacion sim = mapper.convertValue(valMap, Simulacion.class);
                                    simulacionMap.put(key, sim);
                                } else {
                                    Simulacion sim = mapper.convertValue(rawVal, Simulacion.class);
                                    simulacionMap.put(key, sim);
                                }
                            } catch (NumberFormatException nfe) {
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }

                String correo = principal.getName();

                boolean exito = simulacionService.guardarSimulacion(simulacionDTO, correo, simulacionMap);
                if (exito) {
                    return ResponseEntity.ok(true);
                } else {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "No se pudo guardar la simulación en el servidor");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
                }

            } catch (Exception e) {
                e.printStackTrace();
                Map<String, String> error = new HashMap<>();
                error.put("error", e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
            }
    }
}
