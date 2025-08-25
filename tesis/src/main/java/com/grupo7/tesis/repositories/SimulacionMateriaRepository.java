package com.grupo7.tesis.repositories;

import com.grupo7.tesis.models.SimulacionMateria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SimulacionMateriaRepository extends JpaRepository<SimulacionMateria, Long> {

    List<SimulacionMateria> findBySimulacionId(Long simulacionId);
    List<SimulacionMateria> findByMateriaId(Long materiaId);
}
