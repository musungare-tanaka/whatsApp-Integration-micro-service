package com.tanaka.joinai.WhatsappService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class WhatsAppService {

    @Value("${whatsapp.token}")
    private String whatsappToken;

    @Value("${whatsapp.phoneNumberId}")
    private String phoneNumberId;

    private final RestTemplate restTemplate = new RestTemplate();

    public String sendDefaultHelloWorldMessage(String toNumber) {
        System.out.println("TOKEN = " + whatsappToken);


        String url = "https://graph.facebook.com/v22.0/" + phoneNumberId + "/messages";

        // Build request body
        Map<String, Object> body = new HashMap<>();
        body.put("messaging_product", "whatsapp");
        body.put("to", toNumber);
        body.put("type", "template");

        Map<String, Object> template = new HashMap<>();
        template.put("name", "hello_world");

        Map<String, Object> language = new HashMap<>();
        language.put("code", "en_US");

        template.put("language", language);
        body.put("template", template);

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(whatsappToken);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // Send request
        ResponseEntity<String> response =
                restTemplate.exchange(url, HttpMethod.POST, request, String.class);

        return response.getBody();
    }
}
