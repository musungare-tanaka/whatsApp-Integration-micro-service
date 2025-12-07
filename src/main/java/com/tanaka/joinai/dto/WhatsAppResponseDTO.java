package com.tanaka.joinai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class WhatsAppResponseDTO {
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("response_text")
    private String responseText;
    
    @JsonProperty("message_id")
    private String messageId;
    
    @JsonProperty("recipient_phone")
    private String recipientPhone;
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;


}