package org.example.planner_backend.service;


import io.jsonwebtoken.Jwts;
import org.example.planner_backend.model.enums.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {

    private final String secret;
    private final long expiryMinutes;


    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiry-minutes}") long expiryMinutes
    ) {
        this.secret = secret;
        this.expiryMinutes = expiryMinutes;
        System.out.println(">>> JwtService loaded. Signing key algorithm: " +
                getSigningKey().getAlgorithm());

        String sample = generateToken("test@example.com", Role.USER);
        System.out.println(">>> Sample token: " + sample);
    }

    public String generateToken(String email, Role role) {
        Instant now = Instant.now();
        Instant expiry = now.plus(expiryMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
                .subject(email)
                .claim("role", role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
