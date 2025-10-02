package com.grupo7.tesis.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.grupo7.tesis.models.Estudiante;
import com.grupo7.tesis.models.Proyeccion;
import com.grupo7.tesis.repositories.ProyeccionRepository;

@Service
public class ProyeccionService {

    @Autowired
    private ProyeccionRepository proyeccionRepository;

    @Autowired
    private EstudianteService estudianteService;

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

    public Proyeccion eliminarProyeccion(Long id) {
        Proyeccion proyeccion = obtenerProyeccionPorId(id);
        if (proyeccion != null) {
            proyeccionRepository.deleteById(id);
            return proyeccion;
        }
        return null;
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
