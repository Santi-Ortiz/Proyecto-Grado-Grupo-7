package com.grupo7.tesis.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.grupo7.tesis.dtos.EstudianteDTO;
import com.grupo7.tesis.dtos.RegisterDTO;

@Service
public class AuthService {
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    public EstudianteDTO registrarEstudiante(RegisterDTO registerDTO) {

        EstudianteDTO estudianteDTO = new EstudianteDTO();
        
        estudianteDTO.setCodigo(registerDTO.getCodigo());
        estudianteDTO.setCorreo(registerDTO.getCorreo());
        estudianteDTO.setCarrera(registerDTO.getCarrera());
        estudianteDTO.setAnioIngreso(registerDTO.getAnioIngreso());
        estudianteDTO.setContrasenia(passwordEncoder.encode(registerDTO.getContrasenia()));
        estudianteDTO.setPrimerNombre(registerDTO.getPrimerNombre());
        if (estudianteDTO.getSegundoNombre() != null) {
            estudianteDTO.setSegundoNombre(registerDTO.getSegundoNombre());
        }
        estudianteDTO.setPrimerApellido(registerDTO.getPrimerApellido());
        estudianteDTO.setSegundoApellido(registerDTO.getSegundoApellido());

        return estudianteDTO;
    }
}
