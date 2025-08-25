package com.grupo7.tesis.config;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Controller;

import com.grupo7.tesis.models.Facultad;
import com.grupo7.tesis.models.Pensum;
import com.grupo7.tesis.repositories.EstudianteRepository;
import com.grupo7.tesis.repositories.FacultadRepository;
import com.grupo7.tesis.repositories.MateriaRepository;
import com.grupo7.tesis.repositories.PensumRepository;

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
    private EstudianteRepository estudianteRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        if (pensumRepository.count() != 0) {
            return;
        } else if(facultadRepository.count() != 0) {
            return;
        }

        crearFacultades();
        crearPensums();

    }

    public void crearPensums() {

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

    public void crearFacultades() {
        
        Facultad facultadIngenieria = new Facultad("Facultad de Ingeniería");
        Facultad facultadCiencias = new Facultad("Facultad de Ciencias");
        Facultad facultadCienciasEcon = new Facultad("Facultad de Ciencias Económicas y Administrativas");

        facultadRepository.save(facultadIngenieria);
        facultadRepository.save(facultadCiencias);
        facultadRepository.save(facultadCienciasEcon);
    }

    public void crearMateriasDesdeJson() throws Exception {
        
    }
