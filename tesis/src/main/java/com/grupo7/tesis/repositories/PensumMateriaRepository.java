package com.grupo7.tesis.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.grupo7.tesis.models.Materia;
import com.grupo7.tesis.models.PensumMateria;

@Repository
public interface PensumMateriaRepository extends JpaRepository<PensumMateria, Long> {

    // Buscar por pensum ID
    List<PensumMateria> findByPensumId(Long pensumId);

    // Buscar por materia ID
    List<PensumMateria> findByMateriaId(Long materiaId);

    // Obtener materias de un pensum ordenadas por semestre esperado
    @Query("SELECT pm FROM PensumMateria pm WHERE pm.pensum.id = :pensumId ORDER BY pm.semestreEsperado")
    List<PensumMateria> findByPensumIdOrderBySemestreEsperado(@Param("pensumId") Long pensumId);

    // Obtener solo las materias de un pensum (sin la relación)
    @Query("SELECT pm.materia FROM PensumMateria pm WHERE pm.pensum.id = :pensumId ORDER BY pm.semestreEsperado")
    List<Materia> findMateriasByPensumId(@Param("pensumId") Long pensumId);

    // Verificar si existe una asociación específica
    @Query("SELECT COUNT(pm) > 0 FROM PensumMateria pm WHERE pm.pensum.id = :pensumId AND pm.materia.id = :materiaId")
    boolean existsByPensumIdAndMateriaId(@Param("pensumId") Long pensumId, @Param("materiaId") Long materiaId);

    // Obtener materias de un semestre específico de un pensum
    @Query("SELECT pm FROM PensumMateria pm WHERE pm.pensum.id = :pensumId AND pm.semestreEsperado = :semestre")
    List<PensumMateria> findByPensumIdAndSemestreEsperado(@Param("pensumId") Long pensumId,
            @Param("semestre") int semestre);

    // Eliminar por pensum ID (útil para limpiar)
    void deleteByPensumId(Long pensumId);

    // Eliminar por materia ID
    void deleteByMateriaId(Long materiaId);
}
