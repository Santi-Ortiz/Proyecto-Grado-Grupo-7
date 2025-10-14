package com.grupo7.tesis.services;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.grupo7.tesis.models.Estudiante;
import com.grupo7.tesis.models.Proyeccion;
import com.grupo7.tesis.models.Simulacion;
import com.grupo7.tesis.repositories.ProyeccionRepository;
import com.grupo7.tesis.repositories.SimulacionMateriaRepository;
import com.grupo7.tesis.repositories.SimulacionRepository;
import com.grupo7.tesis.models.SimulacionMateria;
import jakarta.transaction.Transactional;

@Service
public class ProyeccionService {

    @Autowired
    private ProyeccionRepository proyeccionRepository;

    @Autowired
    private EstudianteService estudianteService;

    @Autowired
    private SimulacionRepository simulacionRepository;

    @Autowired
    private SimulacionMateriaRepository simulacionMateriaRepository;

    private Map<Long, Proyeccion> proyeccionesCache = new HashMap<>();

    public void guardarProyeccionEnCache(Proyeccion proyeccion) {
        proyeccionesCache.put(proyeccion.getId(), proyeccion);
    }

    public Proyeccion obtenerProyeccionDeCache(Long id) {
        return proyeccionesCache.get(id);
    }

    public List<Proyeccion> obtenerTodasProyecciones() {
        return proyeccionRepository.findAll();
    }

    public Proyeccion obtenerProyeccionPorId(Long id) {
        return proyeccionRepository.findById(id).orElse(null);
    }

    public Proyeccion crearProyeccion(Proyeccion proyeccion) {
        Long estudianteId = estudianteService.getEstudianteAutenticadoId();
        Estudiante estudiante = estudianteService.obtenerEstudiantePorId(estudianteId);

        if (estudiante == null) {
            throw new RuntimeException("Estudiante autenticado no encontrado");
        }

        proyeccion.setEstudianteId(estudiante); // asignar dueño de la proyección
        return proyeccionRepository.save(proyeccion);
    }


    public Proyeccion actualizarProyeccion(Long id, Proyeccion proyeccionActualizada) {
        if (proyeccionRepository.existsById(id)) {
            proyeccionActualizada.setId(id);
            return proyeccionRepository.save(proyeccionActualizada);
        }
        return null;
    }

    @Transactional
    public boolean eliminarProyeccion(Long id) {
        Long proyeccionId = id;
        try {
            List<Simulacion> simulaciones = simulacionRepository.findByProyeccionId_Id(proyeccionId);

            if (simulaciones != null && !simulaciones.isEmpty()) {
                for (Simulacion s : simulaciones) {
                    if (s == null || s.getId() == null) continue;
                    Long simulacionId = s.getId();
                    try {
                        List<SimulacionMateria> materiasAsoc = simulacionMateriaRepository.findBySimulacionId(simulacionId);
                        if (materiasAsoc != null && !materiasAsoc.isEmpty()) {
                            simulacionMateriaRepository.deleteAll(materiasAsoc);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw e;
                    }
                }

                try {
                    simulacionRepository.deleteAll(simulaciones);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }
            }

            try {
                if (proyeccionRepository.existsById(proyeccionId)) {
                    proyeccionRepository.deleteById(proyeccionId);
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }

            return true;
        } catch (Exception ex) {
            throw new RuntimeException("Error eliminando simulación y proyección: " + ex.getMessage(), ex);
        }
    }

    public boolean eliminarProyecciones(String correo) {
        Estudiante estudiante = estudianteService.obtenerEstudiantePorCorreo(correo);
        List<Proyeccion> proyecciones = proyeccionRepository.findByestudianteId(estudiante);
        for (Proyeccion proyeccion : proyecciones) {
            eliminarProyeccion(proyeccion.getId());
        }
        return true;
    }

    public Proyeccion generarProyeccion(int semestre, int creditos, int materias) {

        Proyeccion proyeccion = new Proyeccion();
        proyeccion.setSemestre(semestre);
        proyeccion.setNumMaxCreditos(creditos);
        proyeccion.setNumMaxMaterias(materias);

        return proyeccion;
    }

    public List<Proyeccion> obtenerProyeccionesEstudianteAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String correo = authentication.getName();

        Estudiante estudiante = estudianteService.obtenerEstudiantePorCorreo(correo);
        if (estudiante == null) {
            throw new RuntimeException("Estudiante autenticado no encontrado");
        }

        return proyeccionRepository.findByestudianteId(estudiante);
    }

    public boolean existeProyeccionConNombre(String nombre) {
        Long idEstudiante = estudianteService.getEstudianteAutenticadoId();
        System.out.println("Verificando nombre=" + nombre + " para estudiante=" + idEstudiante);
        return proyeccionRepository.existsByNombreSimulacionIgnoreCaseAndEstudianteId_Id(nombre, idEstudiante);
    }


}
