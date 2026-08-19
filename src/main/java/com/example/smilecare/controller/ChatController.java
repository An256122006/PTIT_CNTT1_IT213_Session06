package com.example.smilecare.controller;

import com.example.smilecare.dto.ChatMessageDto;
import com.example.smilecare.dto.ChatRequest;
import com.example.smilecare.dto.ChatResponse;
import com.example.smilecare.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Main REST API Endpoint for sending chat messages.
     * POST /api/chat
     */
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = chatService.sendMessage(request);
        return ResponseEntity.ok(response);
    }

    /**
     * API to reset/delete chat session history.
     * DELETE /api/chat/{conversationId}
     */
    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Map<String, String>> deleteSession(@PathVariable Long conversationId) {
        chatService.deleteSession(conversationId);
        return ResponseEntity.ok(Map.of(
                "message", "Đã xóa lịch sử phiên trò chuyện thành công",
                "conversationId", String.valueOf(conversationId)
        ));
    }

    /**
     * API to retrieve chat history list for a session.
     * GET /api/chat/{conversationId}/history
     */
    @GetMapping("/{conversationId}/history")
    public ResponseEntity<List<ChatMessageDto>> getHistory(@PathVariable Long conversationId) {
        List<ChatMessageDto> history = chatService.getHistory(conversationId);
        return ResponseEntity.ok(history);
    }

    /**
     * Alias endpoint to retrieve chat history list directly by conversationId.
     * GET /api/chat/{conversationId}
     */
    @GetMapping("/{conversationId}")
    public ResponseEntity<List<ChatMessageDto>> getHistoryByPath(@PathVariable Long conversationId) {
        List<ChatMessageDto> history = chatService.getHistory(conversationId);
        return ResponseEntity.ok(history);
    }
}