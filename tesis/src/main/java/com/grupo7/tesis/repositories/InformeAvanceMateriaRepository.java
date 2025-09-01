package com.grupo7.tesis.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.grupo7.tesis.models.InformeAvanceMateria;

@Repository
public interface InformeAvanceMateriaRepository extends JpaRepository<InformeAvanceMateria, Long> {

    List<InformeAvanceMateria> findByInformeAvanceId(Long informeAvanceId);
    List<InformeAvanceMateria> findByMateriaId(Long materiaId);
}
