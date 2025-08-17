package com.grupo7.tesis.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.grupo7.tesis.models.Proyeccion;

@Repository
public interface ProyeccionRepository extends JpaRepository<Proyeccion, Long> {
    
}
