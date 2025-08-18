package com.grupo7.tesis.repositories;

import com.grupo7.tesis.models.SimulacionMateria;
import com.grupo7.tesis.models.SimulacionMateriaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SimulacionMateriaRepository extends JpaRepository<SimulacionMateria, SimulacionMateriaId> {

    List<SimulacionMateria> findBySimulacionId(Long simulacionId);
    List<SimulacionMateria> findByMateriaId(Long materiaId);

}
