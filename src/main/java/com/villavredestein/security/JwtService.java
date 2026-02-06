package com.villavredestein.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    @Value("${jwt.secret:}")
    private String secret;

    @Value("${jwt.expiration-seconds:86400}")
    private long expirationSeconds;

    private SecretKey signingKey;

    @PostConstruct
    void init() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("Missing required property: jwt.secret");
        }

        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (IllegalArgumentException ignore) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }

        if (keyBytes.length < 32) {
            throw new IllegalStateException("jwt.secret must be at least 32 bytes (256-bit) for HS256");
        }

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // =====================================================================
    // # TOKEN CREATION
    // =====================================================================

    public String generateToken(String email, String role) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expirationSeconds);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .addClaims(Map.of("role", role))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // =====================================================================
    // # TOKEN PARSING / VALIDATION
    // =====================================================================

    public boolean isTokenValid(String token) {
        try {
            Claims claims = parse(token).getBody();
            Date exp = claims.getExpiration();
            return exp != null && exp.after(new Date());
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String extractEmail(String token) {
        return parse(token).getBody().getSubject();
    }

    public String extractRole(String token) {
        Object role = parse(token).getBody().get("role");
        return role != null ? role.toString() : null;
    }

    public String getEmailFromToken(String token) {
        return extractEmail(token);
    }

    public String getRoleFromToken(String token) {
        return extractRole(token);
    }

    public boolean validateToken(String token) {
        return isTokenValid(token);
    }

    private Jws<Claims> parse(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("JWT token is required");
        }

        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token);
    }
}