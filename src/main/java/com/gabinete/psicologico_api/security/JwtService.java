package com.gabinete.psicologico_api.security;

import com.gabinete.psicologico_api.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey getKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Usuario usuario) {
        Long psicologoId = usuario.getPsicologo() != null ? usuario.getPsicologo().getId() : null;
        return Jwts.builder()
                .subject(usuario.getUsername())
                .claim("psicologoId", psicologoId)
                .claim("rol", usuario.getRol())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey())
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractPsicologoId(String token) {
        Object val = extractAllClaims(token).get("psicologoId");
        if (val == null) return null;
        return ((Number) val).longValue();
    }

    public String extractRol(String token) {
        return (String) extractAllClaims(token).get("rol");
    }

    public boolean isTokenValid(String token) {
        try {
            return extractAllClaims(token).getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
