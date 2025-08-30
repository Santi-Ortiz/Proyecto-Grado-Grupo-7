package com.grupo7.tesis.security;

import java.security.Key;
import java.util.Date;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JWTGenerator {
    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    public static final Long EXPIRATION_TIME = 600000L; // El token tendrá una vida de 10 minutos

    // Se genera el token JWT
    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + EXPIRATION_TIME);

        // Se crea el JWT
        String token = Jwts.builder().setSubject(username)
        .setIssuedAt(currentDate)
        .setExpiration(expireDate)
        .signWith(key, SignatureAlgorithm.HS512)
        .compact();
        return token;
    }

    // Se obtiene el usuario a partir del Json Web Token
    public String getUserFromJwt(String token){
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }

    // Se valida el JWT
    public boolean validateToken(String token){
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
