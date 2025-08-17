package com.grupo7.tesis.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.grupo7.tesis.models.Facultad;

@Repository
public interface FacultadRepository extends JpaRepository<Facultad, Long>{
    
}
