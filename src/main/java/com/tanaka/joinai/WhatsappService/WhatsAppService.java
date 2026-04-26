package com.tanaka.joinai.WhatsappService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class WhatsAppService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.whatsapp-from}")
    private String fromAddress;

    @Value("${twilio.api-base-url:https://api.twilio.com}")
    private String twilioApiBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public String sendDefaultHelloWorldMessage(String toNumber) {
        return sendCustomTextMessage(toNumber, "Hello from JoinAI");
    }

    /**
     * Send a custom text message through Twilio WhatsApp.
     */
    public String sendCustomTextMessage(String toNumber, String messageText) {
        String normalizedTo = normalizeToWhatsAppAddress(toNumber);
        String normalizedFrom = normalizeFromAddress(fromAddress);
        String url = buildTwilioMessagesUrl();

        String formBody = "To=" + urlEncode(normalizedTo)
                + "&From=" + urlEncode(normalizedFrom)
                + "&Body=" + urlEncode(messageText != null ? messageText : "");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + basicAuth(accountSid, authToken));

        HttpEntity<String> request = new HttpEntity<>(formBody, headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            return response.getBody();
        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private String buildTwilioMessagesUrl() {
        String base = twilioApiBaseUrl.endsWith("/") ? twilioApiBaseUrl : twilioApiBaseUrl + "/";
        return base + "2010-04-01/Accounts/" + accountSid + "/Messages.json";
    }

    private String normalizeFromAddress(String rawFrom) {
        if (rawFrom == null || rawFrom.isBlank()) {
            throw new IllegalStateException("twilio.whatsapp-from is required");
        }
        return normalizeWhatsAppAddress(rawFrom, "From");
    }

    private String normalizeToWhatsAppAddress(String rawTo) {
        if (rawTo == null || rawTo.isBlank()) {
            throw new IllegalArgumentException("Recipient phone number is required");
        }
        return normalizeWhatsAppAddress(rawTo, "To");
    }

    private String normalizeWhatsAppAddress(String rawValue, String label) {
        String candidate = rawValue.trim();

        if (candidate.regionMatches(true, 0, "whatsapp:", 0, "whatsapp:".length())) {
            candidate = candidate.substring("whatsapp:".length()).trim();
        }

        // Remove common separators users/services may include.
        candidate = candidate.replaceAll("[\\s\\-()]", "");

        // Convert 00-prefixed numbers to E.164 plus notation.
        if (candidate.startsWith("00")) {
            candidate = "+" + candidate.substring(2);
        }

        if (!candidate.startsWith("+")) {
            candidate = "+" + candidate;
        }

        // Keep only digits after the leading plus.
        String digits = candidate.substring(1).replaceAll("\\D", "");
        if (digits.isBlank()) {
            throw new IllegalArgumentException(label + " phone number is invalid: " + rawValue);
        }

        // E.164 allows up to 15 digits, minimum practical value kept at 8.
        if (digits.length() < 8 || digits.length() > 15) {
            throw new IllegalArgumentException(label + " phone number is not valid E.164 length: " + rawValue);
        }

        return "whatsapp:+" + digits;
    }

    private String basicAuth(String username, String password) {
        String raw = username + ":" + password;
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    private String urlEncode(String value) {
        return UriUtils.encodeQueryParam(value, StandardCharsets.UTF_8);
    }
}
