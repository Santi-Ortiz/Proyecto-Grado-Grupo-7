package com.grupo7.tesis.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.grupo7.tesis.models.Simulacion;

@Repository
public interface SimulacionRepository extends JpaRepository<Simulacion, Long> {
    
}
