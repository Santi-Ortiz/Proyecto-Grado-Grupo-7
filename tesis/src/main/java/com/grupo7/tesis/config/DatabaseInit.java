package com.grupo7.tesis.config;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Controller;

import com.grupo7.tesis.models.Facultad;
import com.grupo7.tesis.models.Materia;
import com.grupo7.tesis.models.Pensum;
import com.grupo7.tesis.repositories.EstudianteRepository;
import com.grupo7.tesis.repositories.FacultadRepository;
import com.grupo7.tesis.repositories.MateriaRepository;
import com.grupo7.tesis.repositories.PensumMateriaRepository;
import com.grupo7.tesis.repositories.PensumRepository;
import com.grupo7.tesis.services.MateriaService;
import com.grupo7.tesis.services.PensumService;

import jakarta.transaction.Transactional;

@Controller
@Transactional
public class DatabaseInit implements ApplicationRunner {

    @Autowired
    private FacultadRepository facultadRepository;

    @Autowired
    private PensumRepository pensumRepository;

    @Autowired
    private MateriaRepository materiaRepository;

    @Autowired
    private PensumMateriaRepository pensumMateriaRepository;

    @Autowired
    private MateriaService materiaService;

    @Autowired
    private PensumService pensumService;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        crearFacultades();
        crearPensums();

        crearMateriasDesdeJson();
        asociarMateriasAPensum();

    }

    public void crearPensums() {
        if (pensumRepository.count() == 0) {
            Pensum pensumIngSistemas = new Pensum("Ingeniería de Sistemas", 138L, 8L);
            Pensum pensumIngMecanica = new Pensum("Ingeniería Mecánica", 138L, 8L);
            Pensum pensumIngCivil = new Pensum("Ingeniería Civil", 138L, 8L);
            Pensum pensumIngRedes = new Pensum("Ingeniería de Redes y Telecomunicaciones", 138L, 8L);
            Pensum pensumIngIndustrial = new Pensum("Ingeniería Industrial", 138L, 8L);
            Pensum pensumBioIng = new Pensum("Bioingeniería", 138L, 8L);
            Pensum pensumIngMecatronica = new Pensum("Ingeniería Mecatrónica", 138L, 8L);
            Pensum pensumIngElectronica = new Pensum("Ingeniería Electrónica", 138L, 8L);

            pensumRepository.save(pensumIngSistemas);
            pensumRepository.save(pensumIngMecanica);
            pensumRepository.save(pensumIngCivil);
            pensumRepository.save(pensumIngRedes);
            pensumRepository.save(pensumIngIndustrial);
            pensumRepository.save(pensumBioIng);
            pensumRepository.save(pensumIngMecatronica);
            pensumRepository.save(pensumIngElectronica);
        }
    }

    public void crearFacultades() {
        if (facultadRepository.count() == 0) {
            Facultad facultadIngenieria = new Facultad("Facultad de Ingeniería");
            Facultad facultadCiencias = new Facultad("Facultad de Ciencias");
            Facultad facultadCienciasEcon = new Facultad("Facultad de Ciencias Económicas y Administrativas");

            facultadRepository.save(facultadIngenieria);
            facultadRepository.save(facultadCiencias);
            facultadRepository.save(facultadCienciasEcon);
        }
    }

    public void crearMateriasDesdeJson() throws Exception {
        try {
            if (materiaRepository.count() == 0) {
                List<Materia> materiasCreadas = materiaService.crearMateriasDesdeJson();
                System.out.println("Se crearon " + materiasCreadas.size() + " materias exitosamente");
            }
        } catch (Exception e) {
            System.err.println("Error al crear materias desde JSON: " + e.getMessage());
            throw e;
        }
    }

    public void asociarMateriasAPensum() throws Exception {

        try {
            Pensum pensum = pensumRepository.findByCarrera("Ingeniería de Sistemas");

            if (pensum == null) {
                System.err.println("No se encontró el pensum de Ingeniería de Sistemas");
                return;
            }

            List<Materia> todasLasMaterias = materiaService.obtenerMaterias();

            if (todasLasMaterias.isEmpty()) {
                System.err.println("No se encontraron materias para asociar");
                return;
            }

            // Se extraen los ID de todas las materias
            List<Long> materiaIds = todasLasMaterias.stream()
                    .map(Materia::getId)
                    .collect(Collectors.toList());

            // Se asocian todas las materias al pensum
            int asociacionesExitosas = 0;

            if (pensumMateriaRepository.count() == 0) {
                for (Long materiaId : materiaIds) {
                    try {
                        pensumService.asociarMateriaAPensum(pensum.getId(), materiaId);
                        asociacionesExitosas++;
                    } catch (Exception e) {
                        System.err.println("Error al asociar materia ID " + materiaId + ": " + e.getMessage());
                    }
                }
            }

            System.out.println("Se asociaron " + asociacionesExitosas + " de " + materiaIds.size()
                    + " materias al pensum de Ingeniería de Sistemas");

            List<Materia> materiasAsociadas = pensumService.obtenerMateriasPorPensumId(pensum.getId());
            System.out.println("Verificación: " + materiasAsociadas.size() + " materias asociadas al pensum");

        } catch (Exception e) {
            System.err.println("Error al asociar materias al pensum: " + e.getMessage());
            throw e;
        }
    }

}
