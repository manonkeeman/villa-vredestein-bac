package com.villavredestein.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Mollie payment service — creates iDEAL payments and retrieves their status.
 *
 * Placeholders in application.yml:
 *   mollie.api-key     → MOLLIE_API_KEY env var
 *   mollie.webhook-url → MOLLIE_WEBHOOK_URL env var
 *   app.frontend-url   → FRONTEND_URL env var
 */
@Service
public class MollieService {

    private static final Logger log = LoggerFactory.getLogger(MollieService.class);
    private static final String BASE_URL = "https://api.mollie.com/v2";

    private final RestClient restClient;

    @Value("${mollie.api-key:}")
    private String apiKey;

    @Value("${mollie.webhook-url:}")
    private String webhookUrl;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public MollieService(RestClient.Builder builder) {
        this.restClient = builder.baseUrl(BASE_URL).build();
    }

    // =====================================================================
    // # Create payment
    // =====================================================================

    /**
     * Creates an iDEAL payment in Mollie.
     * Returns (molliePaymentId, checkoutUrl) or null if disabled/failed.
     */
    public MolliePaymentResult createPayment(BigDecimal amount, String description, Long invoiceId) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("[MOLLIE DISABLED] No API key configured — skipping payment creation for invoiceId={}", invoiceId);
            return null;
        }

        try {
            var body = Map.of(
                    "amount",      Map.of("currency", "EUR", "value", formatAmount(amount)),
                    "description", description,
                    "redirectUrl", frontendUrl + "/betaling-verwerkt",
                    "webhookUrl",  webhookUrl,
                    "method",      "ideal",
                    "metadata",    Map.of("invoiceId", String.valueOf(invoiceId))
            );

            MolliePaymentResponse response = restClient.post()
                    .uri("/payments")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(MolliePaymentResponse.class);

            if (response == null || response.id() == null) {
                log.error("Mollie returned empty response for invoiceId={}", invoiceId);
                return null;
            }

            String checkoutUrl = response.checkoutUrl();
            log.info("Mollie payment created (mollieId={}, invoiceId={})", response.id(), invoiceId);
            return new MolliePaymentResult(response.id(), checkoutUrl);

        } catch (Exception e) {
            log.error("Failed to create Mollie payment for invoiceId={}: {}", invoiceId, e.getMessage());
            return null;
        }
    }

    // =====================================================================
    // # Get payment status
    // =====================================================================

    /**
     * Fetches the current status of a Mollie payment.
     */
    public String getPaymentStatus(String molliePaymentId) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("[MOLLIE DISABLED] No API key — cannot fetch status for {}", molliePaymentId);
            return "unknown";
        }

        try {
            MolliePaymentResponse response = restClient.get()
                    .uri("/payments/" + molliePaymentId)
                    .header("Authorization", "Bearer " + apiKey)
                    .retrieve()
                    .body(MolliePaymentResponse.class);

            if (response == null) return "unknown";
            log.debug("Mollie payment status: id={}, status={}", molliePaymentId, response.status());
            return response.status() != null ? response.status() : "unknown";

        } catch (Exception e) {
            log.error("Failed to fetch Mollie payment status for {}: {}", molliePaymentId, e.getMessage());
            return "unknown";
        }
    }

    // =====================================================================
    // # Helpers
    // =====================================================================

    private String formatAmount(BigDecimal amount) {
        return String.format("%.2f", amount);
    }

    // =====================================================================
    // # Inner types — Mollie API response mapping
    // =====================================================================

    public record MolliePaymentResult(String molliePaymentId, String checkoutUrl) {}

    /**
     * Subset of the Mollie payment object we care about.
     * Jackson maps _links.checkout.href via a custom getter.
     */
    public static class MolliePaymentResponse {
        private String id;
        private String status;
        private java.util.Map<String, Object> _links;

        public String id() { return id; }
        public String status() { return status; }

        public void setId(String id) { this.id = id; }
        public void setStatus(String status) { this.status = status; }
        public void set_links(java.util.Map<String, Object> links) { this._links = links; }

        @SuppressWarnings("unchecked")
        public String checkoutUrl() {
            if (_links == null) return null;
            Object checkout = _links.get("checkout");
            if (checkout instanceof java.util.Map<?,?> map) {
                Object href = map.get("href");
                return href instanceof String s ? s : null;
            }
            return null;
        }
    }
}