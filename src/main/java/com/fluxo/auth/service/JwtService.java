package com.fluxo.auth.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationTimeInSeconds;

    public String generateToken(Long userId, String name, String role) {
        // Converte a string secreta em uma chave criptográfica
        Key key = Keys.hmacShaKeyFor(secret.getBytes());
        long expirationInMillis = expirationTimeInSeconds * 1000;

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("name", name)
                .claim("role", role)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationInMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public long getExpirationTime() {
        return expirationTimeInSeconds;
    }
}