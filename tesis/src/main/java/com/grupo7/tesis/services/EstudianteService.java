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

    public Estudiante obtenerEstudiantePorCorreo(String correo) {
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

        List<Pensum> todosPensums = pensumService.obtenerPensums();
        List<Facultad> todasFacultades = facultadService.obtenerTodasFacultades();

        Pensum pensum = null;
        Facultad facultad = null;

        // Se busca el plan de estudios por similitud exacta de carrera
        for (Pensum p : todosPensums) {
            if (p.getCarrera().equalsIgnoreCase(estudianteDTO.getCarrera().trim())) {
                pensum = p;
                break;
            }
        }

        // Si no se encuentra exactamente, se busca por palabras clave
        if (pensum == null) {
            String carreraLower = estudianteDTO.getCarrera().toLowerCase();
            for (Pensum p : todosPensums) {
                String pensumCarreraLower = p.getCarrera().toLowerCase();
                // Se verifica si contiene palabras clave de la carrera
                if (carreraLower.contains("sistemas") && pensumCarreraLower.contains("sistemas") ||
                        carreraLower.contains("civil") && pensumCarreraLower.contains("civil") ||
                        carreraLower.contains("electronica") && pensumCarreraLower.contains("electronica") ||
                        carreraLower.contains("electrónica") && pensumCarreraLower.contains("electronica") ||
                        carreraLower.contains("industrial") && pensumCarreraLower.contains("industrial") ||
                        carreraLower.contains("mecanica") && pensumCarreraLower.contains("mecanica") ||
                        carreraLower.contains("mecánica") && pensumCarreraLower.contains("mecanica") ||
                        carreraLower.contains("redes") && pensumCarreraLower.contains("redes")) {
                    pensum = p;
                    break;
                }
            }
        }

        // Se busca la facultad por tipo de carrera
        String carreraLower = estudianteDTO.getCarrera().toLowerCase();
        for (Facultad f : todasFacultades) {
            String facultadNombreLower = f.getNombre().toLowerCase();

            // Si la carrera es alguna ingeniería, se asigna facultad de ingeniería
            if ((carreraLower.contains("ingeniería") || carreraLower.contains("ingenieria")) &&
                    (facultadNombreLower.contains("ingeniería") || facultadNombreLower.contains("ingenieria"))) {
                facultad = f;
                break;
            }
            // Falta la lógica para las demás carreras
        }

        // Se valida que se encontraron pensum y facultad
        if (pensum == null) {
            throw new RuntimeException("No se encontró un pensum para la carrera: " + estudianteDTO.getCarrera());
        }
        if (facultad == null) {
            throw new RuntimeException("No se encontró una facultad para la carrera: " + estudianteDTO.getCarrera());
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
