package com.grupo7.tesis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // Permite todas las solicitudes sin login
            )
            .csrf(csrf -> csrf.disable())      // Desactiva protección CSRF
            .formLogin(form -> form.disable()); // Desactiva el formulario de login
        return http.build();
    }
}
