package com.tanaka.joinai.WhatsappService;

import com.tanaka.joinai.dto.WhatsAppRequestDTO;
import com.tanaka.joinai.webhook_controller.WebhookController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class WebhookService {

    @Value("${external.service-url}")
    private String chatbot_microservice_url;

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);


    // to send whatsapp message to the chatbot micro-service
    public void forwardIncomingMessage(String from, String message) {

        WhatsAppRequestDTO whatsAppRequestDTO = new WhatsAppRequestDTO();
        whatsAppRequestDTO.setMessage(message);
        whatsAppRequestDTO.setTimestamp(String.valueOf(LocalDateTime.now()));
        whatsAppRequestDTO.setPhoneNumber(from);
        try {
            RestTemplate restTemplate = new RestTemplate();

            String url = chatbot_microservice_url + "message";
            restTemplate.postForObject(url, whatsAppRequestDTO, String.class);

            logger.info("Message forwarded to microservice successfully");
        } catch (Exception e) {
            logger.error("Failed to forward message to microservice", e);
        }
    }
}
