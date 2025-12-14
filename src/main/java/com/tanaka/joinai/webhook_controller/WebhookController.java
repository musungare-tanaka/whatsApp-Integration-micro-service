package com.tanaka.joinai.webhook_controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanaka.joinai.WhatsappService.WebhookService;
import com.tanaka.joinai.dto.IncomingWhatsAppMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);

    private final WebhookService webhookService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${whatsapp.webhook.verify.token}")
    private String verifyToken;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    // --------------------------------------------------
    // WEBHOOK VERIFICATION
    // --------------------------------------------------
    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam(name = "hub.mode", required = false) String mode,
            @RequestParam(name = "hub.challenge", required = false) String challenge,
            @RequestParam(name = "hub.verify_token", required = false) String token
    ) {
        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            logger.info("WEBHOOK VERIFIED");
            return ResponseEntity.ok(challenge);
        }
        logger.warn("Webhook verification failed");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    // --------------------------------------------------
    // WEBHOOK RECEIVER
    // --------------------------------------------------
    @PostMapping
    public ResponseEntity<String> receiveWebhook(@RequestBody Map<String, Object> payload) {

        try {
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            logger.info("\n\nWebhook received {}\n", timestamp);

            logger.info(objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(payload));

            Object entryObj = payload.get("entry");
            if (!(entryObj instanceof List<?> entries) || entries.isEmpty()) {
                return ResponseEntity.ok("EVENT_RECEIVED");
            }

            Object entry0 = entries.getFirst();
            if (!(entry0 instanceof Map<?, ?> entry)) {
                return ResponseEntity.ok("EVENT_RECEIVED");
            }

            Object changesObj = entry.get("changes");
            if (!(changesObj instanceof List<?> changes) || changes.isEmpty()) {
                return ResponseEntity.ok("EVENT_RECEIVED");
            }

            Object change0 = changes.getFirst();
            if (!(change0 instanceof Map<?, ?> change)) {
                return ResponseEntity.ok("EVENT_RECEIVED");
            }

            Object valueObj = change.get("value");
            if (!(valueObj instanceof Map<?, ?> value)) {
                return ResponseEntity.ok("EVENT_RECEIVED");
            }

            Object messagesObj = value.get("messages");
            if (!(messagesObj instanceof List<?> messages) || messages.isEmpty()) {
                return ResponseEntity.ok("EVENT_RECEIVED");
            }

            Object messageObj = messages.getFirst();
            if (!(messageObj instanceof Map<?, ?> message)) {
                return ResponseEntity.ok("EVENT_RECEIVED");
            }

            IncomingWhatsAppMessage incoming = extractIncomingMessage(message);

            if (incoming != null) {
                webhookService.handleIncomingMessage(incoming);
            }

            return ResponseEntity.ok("EVENT_RECEIVED");

        } catch (Exception e) {
            logger.error("Error processing WhatsApp webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // --------------------------------------------------
    // MESSAGE EXTRACTION
    // --------------------------------------------------
    private IncomingWhatsAppMessage extractIncomingMessage(Map<?, ?> message) {

        String from = message.get("from") instanceof String s ? s : null;
        String messageId = message.get("id") instanceof String s ? s : null;
        String timestamp = message.get("timestamp") instanceof String s ? s : null;
        String type = message.get("type") instanceof String s ? s : null;

        if (from == null || type == null) {
            logger.warn("Missing required WhatsApp fields");
            return null;
        }

        return switch (type) {

            case "text" -> {
                Map<?, ?> text = (Map<?, ?>) message.get("text");
                String body = text != null && text.get("body") instanceof String s ? s : null;

                yield new IncomingWhatsAppMessage(
                        from, messageId, timestamp, type,
                        body, null, null, null, null
                );
            }

            case "audio" -> {
                Map<?, ?> audio = (Map<?, ?>) message.get("audio");

                yield new IncomingWhatsAppMessage(
                        from, messageId, timestamp, type,
                        null,
                        audio.get("id") instanceof String s ? s : null,
                        audio.get("mime_type") instanceof String s ? s : null,
                        audio.get("url") instanceof String s ? s : null,
                        audio.get("voice") instanceof Boolean b ? b : null
                );
            }

            case "image", "video", "document" -> {
                Map<?, ?> media = (Map<?, ?>) message.get(type);

                yield new IncomingWhatsAppMessage(
                        from, messageId, timestamp, type,
                        null,
                        media.get("id") instanceof String s ? s : null,
                        media.get("mime_type") instanceof String s ? s : null,
                        media.get("url") instanceof String s ? s : null,
                        null
                );
            }

            default -> {
                logger.warn("Unsupported message type: {}", type);
                yield null;
            }
        };
    }
}
