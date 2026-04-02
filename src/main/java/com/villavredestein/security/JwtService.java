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

    private static final String ROLE_CLAIM = "role";
    private static final String TOKEN_TYPE_CLAIM = "type";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final long MINIMUM_SECRET_BYTES = 32;

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
        } catch (IllegalArgumentException exception) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }

        if (keyBytes.length < MINIMUM_SECRET_BYTES) {
            throw new IllegalStateException("jwt.secret must be at least 32 bytes (256-bit) for HS256");
        }

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String email, String role) {
        validateTokenInput(email, role);

        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(expirationSeconds);

        return Jwts.builder()
                .setSubject(email.trim())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .claim(ROLE_CLAIM, normalizeRole(role))
                .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = parse(token).getBody();
            Date expiration = claims.getExpiration();
            String subject = claims.getSubject();
            String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);

            return subject != null
                    && !subject.isBlank()
                    && expiration != null
                    && expiration.after(new Date())
                    && ACCESS_TOKEN_TYPE.equals(tokenType);
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    public String extractEmail(String token) {
        return parse(token).getBody().getSubject();
    }

    public String extractRole(String token) {
        return parse(token).getBody().get(ROLE_CLAIM, String.class);
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

    private void validateTokenInput(String email, String role) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required to generate a token");
        }

        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Role is required to generate a token");
        }

        if (expirationSeconds <= 0) {
            throw new IllegalStateException("jwt.expiration-seconds must be greater than 0");
        }
    }

    private String normalizeRole(String role) {
        String normalizedRole = role.trim().toUpperCase();
        return normalizedRole.startsWith("ROLE_")
                ? normalizedRole.substring(5)
                : normalizedRole;
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