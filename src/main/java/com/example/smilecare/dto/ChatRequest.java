package com.example.smilecare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChatRequest(
        @NotNull(message = "conversationId không được để trống")
        Long conversationId,

        @NotBlank(message = "Tin nhắn không được để trống")
        String message
) {
}