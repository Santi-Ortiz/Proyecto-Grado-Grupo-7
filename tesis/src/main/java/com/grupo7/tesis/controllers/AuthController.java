package com.grupo7.tesis.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

import com.grupo7.tesis.dtos.AuthResponseDTO;
import com.grupo7.tesis.dtos.EstudianteDTO;
import com.grupo7.tesis.dtos.LoginDTO;
import com.grupo7.tesis.dtos.RegisterDTO;
import com.grupo7.tesis.repositories.EstudianteRepository;
import com.grupo7.tesis.security.JWTGenerator;
import com.grupo7.tesis.services.AuthService;
import com.grupo7.tesis.services.EstudianteService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private EstudianteService estudianteService;

    @Autowired
    private AuthService authService;

    @Autowired
    private JWTGenerator jwtGenerator;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginDTO loginDTO, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getCorreo(),
                        loginDTO.getContrasenia()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtGenerator.generateToken(authentication);

        Cookie jwtCookie = new Cookie("jwt-token", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(24 * 60 * 60);

        response.addCookie(jwtCookie);

        return new ResponseEntity<>(new AuthResponseDTO(token), HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterDTO registerDTO) {
        if (estudianteRepository.existsByCorreo(registerDTO.getCorreo())) {
            return new ResponseEntity<>("El correo ya está registrado",
                    HttpStatus.BAD_REQUEST);
        } else if (estudianteRepository.existsByCodigo(registerDTO.getCodigo())) {
            return new ResponseEntity<>("El código ya está registrado",
                    HttpStatus.BAD_REQUEST);
        }

        EstudianteDTO estudianteDTO = authService.registrarEstudiante(registerDTO);

        estudianteService.crearEstudiante(estudianteDTO);

        return new ResponseEntity<>("Usuario registrado exitosamente",
                HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {

        // Se elimina la cookie cuando se sale de la app
        Cookie jwtCookie = new Cookie("jwt-token", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        
        response.addCookie(jwtCookie);
        
        return new ResponseEntity<>("Logout exitoso", HttpStatus.OK);
    }

    @GetMapping("/verify")
    public ResponseEntity<Boolean> verifyAuthentication(HttpServletRequest request) {
        try {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("jwt-token".equals(cookie.getName())) {
                        String token = cookie.getValue();
                        if (jwtGenerator.validateToken(token)) {
                            return new ResponseEntity<>(true, HttpStatus.OK);
                        }
                    }
                }
            }
            return new ResponseEntity<>(false, HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>(false, HttpStatus.UNAUTHORIZED);
        }
    }

}
