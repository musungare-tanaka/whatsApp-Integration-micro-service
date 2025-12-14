package com.tanaka.joinai.webhook_controller;

import com.tanaka.joinai.WhatsappService.WebhookService;
import com.tanaka.joinai.WhatsappService.WhatsAppService;
import com.tanaka.joinai.dto.WhatsAppRequestDTO;
import com.tanaka.joinai.dto.WhatsAppResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/whatsapp")
public class WhatsAppController {

    private final WhatsAppService whatsAppService;
    private final WebhookService webhookService;

    @Autowired
    public WhatsAppController(WhatsAppService whatsAppService, WebhookService webhookService) {
        this.whatsAppService = whatsAppService;
        this.webhookService = webhookService;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendHelloMessage() {
        String response = whatsAppService.sendCustomTextMessage("+263717151583", "How do i reset my password");
        whatsAppService.sendDefaultHelloWorldMessage("+263717151583");
        return ResponseEntity.ok(response);
    }



    /// endpoints to send test data to another micro-service
    @PostMapping("/test")
    public String test_data(){

        String message = "How do i reset my password";
        String phone_number = "+263717151583";
        var messageRequest = new WhatsAppRequestDTO();
        messageRequest.setMessage(message);
        messageRequest.setPhoneNumber(phone_number);
        webhookService.sendToChatbot(messageRequest);


        return "Success";
    }

    @PostMapping("/reply")
    public ResponseEntity<String> sendMessage(@RequestBody WhatsAppResponseDTO whatsAppResponseDTO){
        System.out.println("Chatbot message received");
        System.out.println("AI response :" + whatsAppResponseDTO.getResponseText());
        return ResponseEntity.ok(whatsAppService.sendCustomTextMessage(whatsAppResponseDTO.getRecipientPhone(), whatsAppResponseDTO.getResponseText()));
    }
}
