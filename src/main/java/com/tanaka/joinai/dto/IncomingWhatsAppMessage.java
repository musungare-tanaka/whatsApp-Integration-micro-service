package com.tanaka.joinai.dto;

public record IncomingWhatsAppMessage(
        String from,
        String messageId,
        String timestamp,
        String type,
        String text,
        String mediaId,
        String mimeType,
        String mediaUrl,
        Boolean isVoice
) {}
