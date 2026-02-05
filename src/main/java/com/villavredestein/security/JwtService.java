package com.villavredestein.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final ObjectMapper objectMapper;
    private final byte[] secret;
    private final long expirySeconds;

    public JwtService(ObjectMapper objectMapper,
                      @Value("${jwt.secret}") String jwtSecret,
                      @Value("${jwt.expiry-seconds:3600}") long expirySeconds) {
        this.objectMapper = objectMapper;
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException("Missing required property: jwt.secret");
        }
        this.secret = jwtSecret.getBytes(StandardCharsets.UTF_8);
        this.expirySeconds = expirySeconds <= 0 ? 3600 : expirySeconds;
    }

    // =====================================================================
    // # EXTRACT
    // =====================================================================

    public String extractUsername(String token) {
        JsonNode payload = readPayload(token);
        if (payload == null) return null;

        String sub = asTextOrNull(payload.get("sub"));
        if (sub != null && !sub.isBlank()) return sub;

        // backward compatibility if you ever stored different keys
        String username = asTextOrNull(payload.get("username"));
        if (username != null && !username.isBlank()) return username;

        String email = asTextOrNull(payload.get("email"));
        if (email != null && !email.isBlank()) return email;

        return null;
    }

    public String extractRole(String token) {
        JsonNode payload = readPayload(token);
        if (payload == null) return null;

        String role = asTextOrNull(payload.get("role"));
        if (role == null || role.isBlank()) return null;
        return role;
    }

    // =====================================================================
    // # VALIDATION
    // =====================================================================

    public boolean validateToken(String token, String expectedUsername) {
        if (token == null || token.isBlank()) return false;
        if (!verifySignature(token)) return false;

        String actual = extractUsername(token);
        if (actual == null || expectedUsername == null) return false;
        if (!actual.equalsIgnoreCase(expectedUsername.trim())) return false;

        return !isExpired(token);
    }

    private boolean isExpired(String token) {
        JsonNode payload = readPayload(token);
        if (payload == null) return true;

        JsonNode expNode = payload.get("exp");
        if (expNode == null || expNode.isNull()) {
            // If exp is missing, treat as expired (safer)
            return true;
        }

        long expSeconds;
        if (expNode.isNumber()) {
            expSeconds = expNode.asLong();
        } else {
            String expText = asTextOrNull(expNode);
            if (expText == null) return true;
            try {
                expSeconds = Long.parseLong(expText);
            } catch (NumberFormatException e) {
                return true;
            }
        }

        long now = Instant.now().getEpochSecond();
        return now >= expSeconds;
    }

    private boolean verifySignature(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return false;

            String signingInput = parts[0] + "." + parts[1];
            String expectedSig = base64UrlEncode(hmacSha256(signingInput.getBytes(StandardCharsets.UTF_8)));

            return constantTimeEquals(expectedSig, parts[2]);
        } catch (Exception e) {
            log.debug("JWT signature verification failed: {}", e.getMessage());
            return false;
        }
    }

    // =====================================================================
    // # READ PAYLOAD
    // =====================================================================

    private JsonNode readPayload(String token) {
        if (token == null) return null;
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;

            byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
            String json = new String(decoded, StandardCharsets.UTF_8);
            return objectMapper.readTree(json);
        } catch (Exception e) {
            log.debug("Failed to parse JWT payload: {}", e.getMessage());
            return null;
        }
    }

    private String asTextOrNull(JsonNode node) {
        if (node == null || node.isNull()) return null;
        String v = node.asText(null);
        if (v == null) return null;
        return v.trim();
    }

    // =====================================================================
    // # GENERATE
    // =====================================================================

    public String generateToken(String username, String role) {
        try {
            if (username == null || username.isBlank()) {
                throw new IllegalArgumentException("username is required");
            }

            Map<String, Object> header = new HashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");

            Map<String, Object> payload = new HashMap<>();
            payload.put("sub", username.trim());
            if (role != null && !role.isBlank()) {
                payload.put("role", role.trim());
            }

            long now = Instant.now().getEpochSecond();
            payload.put("iat", now);
            payload.put("exp", now + expirySeconds);

            String headerJson = objectMapper.writeValueAsString(header);
            String payloadJson = objectMapper.writeValueAsString(payload);

            String headerB64 = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));
            String payloadB64 = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));

            String signingInput = headerB64 + "." + payloadB64;
            String sigB64 = base64UrlEncode(hmacSha256(signingInput.getBytes(StandardCharsets.UTF_8)));

            return signingInput + "." + sigB64;
        } catch (Exception e) {
            log.debug("Failed to generate JWT: {}", e.getMessage());
            return null;
        }
    }

    // =====================================================================
    // # CRYPTO + UTILS
    // =====================================================================

    private byte[] hmacSha256(byte[] data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret, "HmacSHA256"));
        return mac.doFinal(data);
    }

    private String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}