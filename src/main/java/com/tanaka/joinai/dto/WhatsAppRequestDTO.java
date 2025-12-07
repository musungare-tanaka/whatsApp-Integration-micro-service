package com.tanaka.joinai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WhatsAppRequestDTO {
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format. Use E.164 format")
    @JsonProperty("phone_number")
    private String phoneNumber;
    
    @NotBlank(message = "Message content is required")
    @Size(max = 1000, message = "Message cannot exceed 1000 characters")
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("message_id")
    private String messageId;
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    @JsonProperty("media_url")
    private String mediaUrl;
    
    @JsonProperty("media_type")
    private String mediaType;
    

}