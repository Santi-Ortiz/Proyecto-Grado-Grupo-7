package com.grupo7.tesis.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.grupo7.tesis.models.Estudiante;
import com.grupo7.tesis.models.Proyeccion;

@Repository
public interface ProyeccionRepository extends JpaRepository<Proyeccion, Long> {
    // Custom query method to find projections by student ID
    List<Proyeccion> findByestudianteId(Estudiante estudianteId);

    // Aquí indicamos explícitamente el id del estudiante
    boolean existsByNombreSimulacionIgnoreCaseAndEstudianteId_Id(String nombreSimulacion, Long idEstudiante);
}

