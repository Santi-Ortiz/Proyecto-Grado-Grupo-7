package com.grupo7.tesis.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.grupo7.tesis.models.InformeAvance;
import com.grupo7.tesis.repositories.InformeAvanceRepository;

@Service
public class InformeAvanceService {
    
    @Autowired
    private InformeAvanceRepository informeAvanceRepository;

    public List<InformeAvance> obtenerTodosInformesAvance(){
        return informeAvanceRepository.findAll();
    }

    public InformeAvance obtenerInformeAvancePorID(Long id){
        return informeAvanceRepository.findById(id).orElse(null);
    }
     
    public InformeAvance crearInformeAvance(InformeAvance informeAvance) {
        return informeAvanceRepository.save(informeAvance);
    }

}
