package com.tanaka.joinai.WhatsappService;

import com.tanaka.joinai.dto.IncomingWhatsAppMessage;
import com.tanaka.joinai.dto.WhatsAppRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
public class WebhookService {

    private static final Logger logger = LoggerFactory.getLogger(WebhookService.class);

    @Value("${external.service-url}")
    private String chatbotMicroserviceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // --------------------------------------------------
    // MAIN ROUTER
    // --------------------------------------------------
    public void handleIncomingMessage(IncomingWhatsAppMessage msg) {

        logger.info("Incoming WhatsApp message | from={} | type={}",
                msg.from(), msg.type());

        switch (msg.type()) {
            case "text" -> forwardText(msg);
            case "audio" -> forwardAudio(msg);
            default -> logger.info("Message type {} not supported yet", msg.type());
        }
    }

    // --------------------------------------------------
    // TEXT → CHATBOT
    // --------------------------------------------------
    private void forwardText(IncomingWhatsAppMessage msg) {

        WhatsAppRequestDTO dto = new WhatsAppRequestDTO();
        dto.setPhoneNumber(msg.from());
        dto.setMessage(msg.text());
        dto.setMessageId(msg.messageId());
        dto.setTimestamp(msg.timestamp());

        sendToChatbot(dto);
    }

    // --------------------------------------------------
    // AUDIO → CHATBOT (NO TRANSCRIPTION YET)
    // --------------------------------------------------
    private void forwardAudio(IncomingWhatsAppMessage msg) {

        WhatsAppRequestDTO dto = new WhatsAppRequestDTO();
        dto.setPhoneNumber(msg.from());
        dto.setMessage("[VOICE_NOTE]");
        dto.setMessageId(msg.messageId());
        dto.setTimestamp(msg.timestamp());
        dto.setMediaUrl(msg.mediaUrl());
        dto.setMediaType(msg.mimeType());

        sendToChatbot(dto);
    }

    // --------------------------------------------------
    // COMMON SENDER
    // --------------------------------------------------
    public void sendToChatbot(WhatsAppRequestDTO dto) {

        try {
            restTemplate.postForObject(
                    chatbotMicroserviceUrl + "/message",
                    dto,
                    String.class
            );

            logger.info("Message forwarded to chatbot | phone={}", dto.getPhoneNumber());

        } catch (Exception e) {
            logger.error("Failed to forward message to chatbot", e);
        }
    }
}
