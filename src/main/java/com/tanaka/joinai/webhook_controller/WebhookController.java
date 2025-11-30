package com.tanaka.joinai.webhook_controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
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
     * */
    @PostMapping
    public ResponseEntity<String> receiveWebhook(@RequestBody Map<String, Object> payload) {
        try {
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            logger.info("\n\nWebhook received {}\n", timestamp);

            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(payload);
            logger.info(prettyJson);

            // ----------------------------------------
            //  EXTRACT MESSAGE (TYPE-SAFE)
            // ----------------------------------------
            try {
                Object entryObj = payload.get("entry");
                if (!(entryObj instanceof List<?> entries)) {
                    logger.warn("No valid entries in webhook payload");
                    return ResponseEntity.ok("EVENT_RECEIVED");
                }

                if (entries.isEmpty()) {
                    logger.warn("Entries list is empty");
                    return ResponseEntity.ok("EVENT_RECEIVED");
                }

                Object firstEntry = entries.getFirst();
                if (!(firstEntry instanceof Map<?, ?> entry)) {
                    logger.warn("Entry is not a map");
                    return ResponseEntity.ok("EVENT_RECEIVED");
                }

                Object changesObj = entry.get("changes");
                if (!(changesObj instanceof List<?> changes)) {
                    logger.warn("No valid changes in entry");
                    return ResponseEntity.ok("EVENT_RECEIVED");
                }

                if (changes.isEmpty()) {
                    logger.warn("Changes list is empty");
                    return ResponseEntity.ok("EVENT_RECEIVED");
                }

                Object firstChange = changes.getFirst();
                if (!(firstChange instanceof Map<?, ?> change)) {
                    logger.warn("Change is not a map");
                    return ResponseEntity.ok("EVENT_RECEIVED");
                }

                Object valueObj = change.get("value");
                if (!(valueObj instanceof Map<?, ?> value)) {
                    logger.warn("Value is not a map");
                    return ResponseEntity.ok("EVENT_RECEIVED");
                }

                // Check if this is a MESSAGE webhook (not a status update)
                Object messagesObj = value.get("messages");
                if (messagesObj instanceof List<?> messages) {
                    if (!messages.isEmpty()) {
                        Object firstMessage = messages.getFirst();
                        if (firstMessage instanceof Map<?, ?> message) {

                            Object fromObj = message.get("from");
                            String from = fromObj instanceof String ? (String) fromObj : null;

                            // Extract message body safely
                            Object textObj = message.get("text");
                            String body = null;
                            if (textObj instanceof Map<?, ?> textMap) {
                                Object bodyObj = textMap.get("body");
                                body = bodyObj instanceof String ? (String) bodyObj : null;
                            }

                            if (from != null && body != null) {
                                logger.info("Extracted Sender: {}", from);
                                logger.info("Extracted Message Body: {}", body);

                                // Forward to microservice
                                forwardIncomingMessage(from, body);
                            } else {
                                logger.warn("Missing sender or body in message. From: {}, Body: {}", from, body);
                            }
                        }
                    }
                } else if (value.get("statuses") instanceof List) {
                    logger.debug("Received status update webhook (not a message)");
                } else {
                    logger.warn("Received webhook with unknown structure");
                }

            } catch (Exception extractErr) {
                logger.error("Failed to extract incoming message", extractErr);
            }

            return ResponseEntity.ok("EVENT_RECEIVED");

        } catch (Exception e) {
            logger.error("Error processing webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private void forwardIncomingMessage(String from, String message) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            Map<String, String> request = new HashMap<>();
            request.put("from", from);
            request.put("message", message);

            String url = "http://localhost:8085/api/messages/incoming";
            restTemplate.postForObject(url, request, String.class);

            logger.info("Message forwarded to microservice successfully");
        } catch (Exception e) {
            logger.error("Failed to forward message to microservice", e);
        }
    }


}