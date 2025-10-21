package com.grupo7.tesis.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grupo7.tesis.models.RequisitoMateria;

public interface RequisitoMateriaRepository extends JpaRepository<RequisitoMateria, Long> {
    List<RequisitoMateria> findByMateriaCodigo(String materiaCodigo);
}
