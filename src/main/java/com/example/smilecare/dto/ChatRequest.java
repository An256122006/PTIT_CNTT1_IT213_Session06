package com.example.smilecare.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        String conversationId,

        @NotBlank(message = "Tin nhắn không được để trống")
        String message
) {
}