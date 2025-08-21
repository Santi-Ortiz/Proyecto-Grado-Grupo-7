package com.grupo7.tesis.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.grupo7.tesis.dtos.EstudianteDTO;
import com.grupo7.tesis.models.Estudiante;
import com.grupo7.tesis.models.Facultad;
import com.grupo7.tesis.models.Pensum;
import com.grupo7.tesis.repositories.EstudianteRepository;

@Service
public class EstudianteService {

    private final EstudianteRepository estudianteRepository;
    private final PensumService pensumService;
    private final FacultadService facultadService;

    @Autowired
    public EstudianteService(EstudianteRepository estudianteRepository,
            PensumService pensumService,
            FacultadService facultadService) {
        this.estudianteRepository = estudianteRepository;
        this.pensumService = pensumService;
        this.facultadService = facultadService;
    }

    public List<Estudiante> obtenerTodosEstudiantes() {
        return estudianteRepository.findAll();
    }

    public Estudiante obtenerEstudiantePorCorreo(String correo){
        return estudianteRepository.findByCorreo(correo);
    }

    public Estudiante obtenerEstudiantePorId(Long id) {
        return estudianteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado con ID: " + id));
    }

    public Estudiante crearEstudiante(Estudiante estudiante) {
        return estudianteRepository.save(estudiante);
    }

    public Estudiante crearEstudiante(EstudianteDTO estudianteDTO) {

        // Se buscan las entidades Pensum y Facultad
        Pensum pensum = pensumService.obtenerPensumPorId(estudianteDTO.getPensumId());
        if (pensum == null) {
            throw new RuntimeException("Pensum no encontrado con ID: " + estudianteDTO.getPensumId());
        }

        Facultad facultad = facultadService.obtenerFacultadPorId(estudianteDTO.getFacultadId());
        if (facultad == null) {
            throw new RuntimeException("Facultad no encontrada con ID: " + estudianteDTO.getFacultadId());
        }

        // Se crea el estudiante
        Estudiante estudiante = new Estudiante(
                estudianteDTO.getCodigo(),
                estudianteDTO.getCorreo(),
                estudianteDTO.getContrasenia(),
                estudianteDTO.getPrimerNombre(),
                estudianteDTO.getSegundoNombre(),
                estudianteDTO.getPrimerApellido(),
                estudianteDTO.getSegundoApellido(),
                estudianteDTO.getCarrera(),
                estudianteDTO.getAnioIngreso(),
                pensum,
                facultad);

        return estudianteRepository.save(estudiante);
    }

    public Estudiante actualizarEstudiante(Long id, Estudiante estudiante) {
        Estudiante estudianteExistente = obtenerEstudiantePorId(id);

        if (estudiante.getCodigo() != null) {
            estudianteExistente.setCodigo(estudiante.getCodigo());
        }
        if (estudiante.getCorreo() != null) {
            estudianteExistente.setCorreo(estudiante.getCorreo());
        }
        if (estudiante.getContrasenia() != null) {
            estudianteExistente.setContrasenia(estudiante.getContrasenia());
        }
        if (estudiante.getPrimerNombre() != null) {
            estudianteExistente.setPrimerNombre(estudiante.getPrimerNombre());
        }
        if (estudiante.getSegundoNombre() != null) {
            estudianteExistente.setSegundoNombre(estudiante.getSegundoNombre());
        }
        if (estudiante.getPrimerApellido() != null) {
            estudianteExistente.setPrimerApellido(estudiante.getPrimerApellido());
        }
        if (estudiante.getSegundoApellido() != null) {
            estudianteExistente.setSegundoApellido(estudiante.getSegundoApellido());
        }
        if (estudiante.getCarrera() != null) {
            estudianteExistente.setCarrera(estudiante.getCarrera());
        }
        if (estudiante.getAnioIngreso() != null) {
            estudianteExistente.setAnioIngreso(estudiante.getAnioIngreso());
        }
        if (estudiante.getFacultad() != null) {
            estudianteExistente.setFacultad(estudiante.getFacultad());
        }
        if (estudiante.getPensum() != null) {
            estudianteExistente.setPensum(estudiante.getPensum());
        }
        return estudianteRepository.save(estudianteExistente);
    }

    public void eliminarEstudiante(Long id) {
        Estudiante estudiante = obtenerEstudiantePorId(id);
        estudianteRepository.delete(estudiante);
    }

    /* Acceder a los objetos Pensum y Facultad */

    public Pensum obtenerPensumEstudiante(Long estudianteId) {
        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
        return estudiante.getPensum();
    }

    public Facultad obtenerFacultadEstudiante(Long estudianteId) {
        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
        return estudiante.getFacultad();
    }

}
