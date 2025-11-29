package com.tanaka.joinai.webhook_controller;

import com.tanaka.joinai.WhatsappService.WhatsAppService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/whatsapp")
public class WhatsAppController {

    private final WhatsAppService whatsAppService;

    public WhatsAppController(WhatsAppService whatsAppService) {
        this.whatsAppService = whatsAppService;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendHelloMessage(@RequestParam String to) {
        String response = whatsAppService.sendDefaultHelloWorldMessage(to);
        return ResponseEntity.ok(response);
    }
}
