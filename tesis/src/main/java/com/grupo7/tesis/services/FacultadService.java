package com.grupo7.tesis.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.grupo7.tesis.models.Facultad;
import com.grupo7.tesis.repositories.FacultadRepository;

@Service
public class FacultadService {

    @Autowired
    private FacultadRepository facultadRepository;

    public List<Facultad> obtenerTodasFacultades() {
        return facultadRepository.findAll();
    }

    public Facultad obtenerFacultadPorId(Long id) {
        return facultadRepository.findById(id).orElse(null);
    }

    public Facultad crearFacultad(Facultad facultad) {
        return facultadRepository.save(facultad);
    }

    public Facultad actualizarFacultad(Long id, Facultad facultadActualizada) {
        if (facultadRepository.existsById(id)) {
            facultadActualizada.setId(id);
            return facultadRepository.save(facultadActualizada);
        }
        return null;
    }

    public Facultad eliminarFacultad(Long id) {
        if (facultadRepository.existsById(id)) {
            Facultad facultad = facultadRepository.findById(id).orElse(null);
            facultadRepository.deleteById(id);
            return facultad;
        }
        return null;
    }
    
}
