package com.villavredestein.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * Sends WhatsApp messages via Twilio's API.
 * Requires TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN and TWILIO_WHATSAPP_FROM env vars.
 * Silently skips when not configured.
 */
@Service
public class WhatsAppService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppService.class);
    private static final String TWILIO_API = "https://api.twilio.com/2010-04-01/Accounts/%s/Messages.json";

    @Value("${twilio.account-sid:}")
    private String accountSid;

    @Value("${twilio.auth-token:}")
    private String authToken;

    @Value("${twilio.whatsapp.from:}")
    private String fromNumber;

    @Value("${twilio.whatsapp.admin-numbers:}")
    private String adminNumbersRaw;

    private final HttpClient http = HttpClient.newHttpClient();

    public boolean isConfigured() {
        return !accountSid.isBlank() && !authToken.isBlank() && !fromNumber.isBlank();
    }

    public void send(String toNumber, String message) {
        if (!isConfigured()) {
            log.debug("WhatsApp not configured, skipping message to {}", maskPhone(toNumber));
            return;
        }
        if (toNumber == null || toNumber.isBlank()) return;

        String to = toNumber.trim().startsWith("whatsapp:") ? toNumber.trim() : "whatsapp:" + toNumber.trim();
        String from = fromNumber.trim().startsWith("whatsapp:") ? fromNumber.trim() : "whatsapp:" + fromNumber.trim();

        String body = "To=" + encode(to)
                + "&From=" + encode(from)
                + "&Body=" + encode(message);

        String url = String.format(TWILIO_API, accountSid);
        String credentials = Base64.getEncoder().encodeToString((accountSid + ":" + authToken).getBytes(StandardCharsets.UTF_8));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Basic " + credentials)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                log.info("WhatsApp sent to {}", maskPhone(toNumber));
            } else {
                log.warn("WhatsApp to {} failed HTTP {}: {}", maskPhone(toNumber), resp.statusCode(), resp.body());
            }
        } catch (IOException | InterruptedException e) {
            log.error("WhatsApp send error for {}: {}", maskPhone(toNumber), e.getMessage());
        }
    }

    public void sendToAdmins(String message) {
        if (adminNumbersRaw == null || adminNumbersRaw.isBlank()) return;
        for (String num : adminNumbersRaw.split(",")) {
            String n = num.trim();
            if (!n.isEmpty()) send(n, message);
        }
    }

    public void sendToAll(List<String> phoneNumbers, String message) {
        if (phoneNumbers == null) return;
        for (String num : phoneNumbers) {
            if (num != null && !num.isBlank()) send(num, message);
        }
    }

    private String encode(String v) {
        return URLEncoder.encode(v, StandardCharsets.UTF_8);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "***";
        return phone.substring(0, Math.min(4, phone.length())) + "***";
    }
}
