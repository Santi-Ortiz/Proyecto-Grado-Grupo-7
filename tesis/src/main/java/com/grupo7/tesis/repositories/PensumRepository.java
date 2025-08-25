package com.grupo7.tesis.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.grupo7.tesis.models.Pensum;

@Repository
public interface PensumRepository extends JpaRepository<Pensum, Long> {

    public Pensum findByCarrera(String carrera);
    
}
