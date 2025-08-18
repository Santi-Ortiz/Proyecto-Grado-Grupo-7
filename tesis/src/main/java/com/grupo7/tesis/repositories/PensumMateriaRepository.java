package com.grupo7.tesis.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.grupo7.tesis.models.PensumMateria;

@Repository
public interface PensumMateriaRepository extends JpaRepository<PensumMateria, Long> {
    
}
