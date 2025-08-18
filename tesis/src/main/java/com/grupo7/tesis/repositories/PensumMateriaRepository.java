package com.grupo7.tesis.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.grupo7.tesis.models.PensumMateria;
import com.grupo7.tesis.models.PensumMateriaId;

@Repository
public interface PensumMateriaRepository extends JpaRepository<PensumMateria, PensumMateriaId> {

    List<PensumMateria> findByPensumId(Long pensumId);
    List<PensumMateria> findByMateriaId(Long materiaId);
}
