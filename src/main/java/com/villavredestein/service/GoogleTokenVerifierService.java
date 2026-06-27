package com.villavredestein.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class GoogleTokenVerifierService {

    private static final Logger log = LoggerFactory.getLogger(GoogleTokenVerifierService.class);
    private static final String TOKENINFO_URL =
            "https://www.googleapis.com/oauth2/v3/tokeninfo?access_token=";

    @Value("${google.client-id:}")
    private String googleClientId;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String verifyAndGetEmail(String accessToken) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TOKENINFO_URL + accessToken))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("Google tokeninfo returned HTTP {}", response.statusCode());
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Ongeldig Google token");
            }

            JsonNode json = objectMapper.readTree(response.body());

            if (!googleClientId.isBlank()) {
                String azp = json.path("azp").asText("");
                String aud = json.path("aud").asText("");
                if (!googleClientId.equals(azp) && !googleClientId.equals(aud)) {
                    log.warn("Google token audience mismatch: azp={} aud={}", azp, aud);
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                            "Token is niet voor deze applicatie");
                }
            }

            String emailVerified = json.path("email_verified").asText("false");
            if (!Boolean.parseBoolean(emailVerified)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "E-mailadres is niet geverifieerd bij Google");
            }

            String email = json.path("email").asText("");
            if (email.isBlank()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Geen e-mailadres ontvangen van Google");
            }

            return email;

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google token verificatie mislukt: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Google login is tijdelijk niet beschikbaar");
        }
    }
}
