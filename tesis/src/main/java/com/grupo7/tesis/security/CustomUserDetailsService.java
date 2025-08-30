package com.grupo7.tesis.security;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.grupo7.tesis.models.Estudiante;
import com.grupo7.tesis.repositories.EstudianteRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Estudiante estudiante = estudianteRepository.findByCorreo(correo);
        if (estudiante == null) {
            throw new UsernameNotFoundException("Usuario no encontrado con correo: " + correo);
        }

        return new User(estudiante.getCorreo(),
                estudiante.getContrasenia(),
                getAuthorities());
    }

    // Todos los usuarios son estudiantes, así que siempre devolvemos
    // ROLE_ESTUDIANTE
    private Collection<GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_ESTUDIANTE"));
    }
}
