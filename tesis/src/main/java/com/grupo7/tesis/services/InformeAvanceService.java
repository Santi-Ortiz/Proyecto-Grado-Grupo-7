package com.grupo7.tesis.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.grupo7.tesis.models.Estudiante;
import com.grupo7.tesis.models.InformeAvance;
import com.grupo7.tesis.repositories.InformeAvanceRepository;

@Service
public class InformeAvanceService {
    
    @Autowired
    private InformeAvanceRepository informeAvanceRepository;

    @Autowired
    private EstudianteService estudianteService;

    public List<InformeAvance> obtenerTodosInformesAvance(){
        return informeAvanceRepository.findAll();
    }

    public InformeAvance obtenerInformeAvancePorID(Long id){
        return informeAvanceRepository.findById(id).orElse(null);
    }
     
    public InformeAvance crearInformeAvance(InformeAvance informeAvance) {
        return informeAvanceRepository.save(informeAvance);
    }

    byte[] obtenerArchivoInformeEstudianteAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String correo = authentication.getName();

        Estudiante estudiante = estudianteService.obtenerEstudiantePorCorreo(correo);
        if (estudiante == null) {
            throw new RuntimeException("Estudiante autenticado no encontrado");
        }

        InformeAvance informe = informeAvanceRepository
                .findFirstByEstudianteIdOrderByFechaPublicacionDesc(estudiante.getId());

        if (informe == null || informe.getArchivo() == null) {
            throw new RuntimeException("No se encontró informe de avance con archivo para este estudiante");
        }

        return informe.getArchivo();
    }

    public InformeAvance obtenerUltimoInformeAvance() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String correo = authentication.getName();

        Estudiante estudiante = estudianteService.obtenerEstudiantePorCorreo(correo);
        if (estudiante == null) {
            throw new RuntimeException("Estudiante autenticado no encontrado");
        }

        InformeAvance informe = informeAvanceRepository
                .findFirstByEstudianteIdOrderByFechaPublicacionDesc(estudiante.getId());

        if (informe == null || informe.getArchivo() == null) {
            throw new RuntimeException("No se encontró informe de avance con archivo para este estudiante");
        }

        return informe; // Devuelve el objeto completo, incluyendo el byte[]
    }



}
