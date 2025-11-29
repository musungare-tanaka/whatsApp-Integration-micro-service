package com.tanaka.joinai.webhook_controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);

    @Value("${whatsapp.webhook.verify.token}")    private String verifyToken;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * GET endpoint for webhook verification
     */
    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam(name = "hub.mode", required = false) String mode,
            @RequestParam(name = "hub.challenge", required = false) String challenge,
            @RequestParam(name = "hub.verify_token", required = false) String token
    ) {
        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            logger.info("WEBHOOK VERIFIED");
            return ResponseEntity.ok(challenge);
        } else {
            logger.warn("Webhook verification failed. Mode: {}, Token match: {}", mode, verifyToken.equals(token));
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * POST endpoint to receive webhook events
     */
    @PostMapping
    public ResponseEntity<String> receiveWebhook(@RequestBody Map<String, Object> payload) {
        try {
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            logger.info("\n\nWebhook received {}\n", timestamp);
            
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(payload);
            logger.info(prettyJson);
            
            return ResponseEntity.ok("EVENT_RECEIVED");
            
        } catch (Exception e) {
            logger.error("Error processing webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}