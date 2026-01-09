package com.villavredestein.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret:MySuperSecretKeyForVillaVredesteinThatIsLongEnough!!}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}") // 24 uur
    private long jwtExpirationMs;

    private Key getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length < 32) {
            keyBytes = Arrays.copyOf(keyBytes, 32);
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();

        String normalizedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        claims.put("role", normalizedRole);

        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(username)
                .addClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token, String username) {
        try {
            Claims claims = extractAllClaims(token);
            String extractedUsername = claims.getSubject();

            if (!extractedUsername.equals(username)) {
                log.warn("JWT invalid: username mismatch (token={}, expected={})",
                        extractedUsername, username);
                return false;
            }

            if (claims.getExpiration().before(new Date())) {
                log.warn("JWT invalid: token expired");
                return false;
            }

            return true;

        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
        } catch (UnsupportedJwtException | MalformedJwtException | SecurityException e) {
            log.warn("JWT corrupt/invalid: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT missing/empty: {}", e.getMessage());
        }

        return false;
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        Object role = extractAllClaims(token).get("role");
        return (role != null) ? role.toString() : null;
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}