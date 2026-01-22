package com.villavredestein.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret:MySuperSecretKeyForVillaVredesteinThatIsLongEnough!!}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationMs;

    // =====================================================================
    // # Token creation
    // =====================================================================

    public String generateToken(String username, String role) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", normalizeRoleClaim(role));

        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(username.trim())
                .addClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // =====================================================================
    // # Validation
    // =====================================================================

    public boolean validateToken(String token, String username) {
        if (token == null || token.isBlank()) {
            log.warn("JWT missing or empty");
            return false;
        }
        if (username == null || username.isBlank()) {
            log.warn("JWT validation failed: username is missing");
            return false;
        }

        try {
            Claims claims = extractAllClaims(token);
            String extractedUsername = claims.getSubject();

            if (extractedUsername == null || extractedUsername.isBlank()) {
                log.warn("JWT invalid: missing subject");
                return false;
            }

            if (!extractedUsername.equals(username)) {
                log.warn("JWT invalid: username mismatch (tokenUser={}, expectedUser={})",
                        maskEmail(extractedUsername), maskEmail(username));
                return false;
            }

            Date exp = claims.getExpiration();
            if (exp == null) {
                log.warn("JWT invalid: missing expiration");
                return false;
            }

            if (exp.before(new Date())) {
                log.warn("JWT invalid: token expired");
                return false;
            }

            return true;

        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
        } catch (UnsupportedJwtException | MalformedJwtException | SecurityException e) {
            log.warn("JWT invalid: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT parsing failed: {}", e.getMessage());
        }

        return false;
    }

    // =====================================================================
    // # Extraction
    // =====================================================================

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        Object role = extractAllClaims(token).get("role");
        return role != null ? role.toString() : null;
    }

    // =====================================================================
    // # Internals
    // =====================================================================

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length < 32) {
            log.warn("jwt.secret is shorter than 32 bytes; it will be padded. Use a longer secret.");
            keyBytes = Arrays.copyOf(keyBytes, 32);
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String normalizeRoleClaim(String role) {
        if (role == null || role.isBlank()) {
            return "ROLE_STUDENT";
        }
        String r = role.trim().toUpperCase();
        return r.startsWith("ROLE_") ? r : "ROLE_" + r;
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) return "(no-email)";
        String trimmed = email.trim();
        int at = trimmed.indexOf('@');
        if (at <= 1) return "***";
        return trimmed.charAt(0) + "***" + trimmed.substring(at);
    }
}