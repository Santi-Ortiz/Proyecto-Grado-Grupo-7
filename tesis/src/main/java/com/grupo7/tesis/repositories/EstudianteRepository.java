package com.grupo7.tesis.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.grupo7.tesis.models.Estudiante;


@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {
    
    Estudiante findByCorreo(String correo);
    Estudiante findByCodigo(String codigo);
    Boolean existsByCorreo(String correo);
    Boolean existsByCodigo(String codigo);
}
