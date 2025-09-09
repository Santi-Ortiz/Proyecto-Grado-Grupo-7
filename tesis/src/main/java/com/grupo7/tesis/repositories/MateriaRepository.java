package com.grupo7.tesis.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.grupo7.tesis.models.Materia;

@Repository
public interface MateriaRepository extends JpaRepository<Materia, Long> {

    Optional<Materia> findByCodigo(String codigo);

}
