package com.example.smilecare.service;

import com.example.smilecare.dto.ChatMessageDto;
import com.example.smilecare.dto.ChatRequest;
import com.example.smilecare.dto.ChatResponse;
import com.example.smilecare.memory.ConversationMemoryManager;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final ConversationMemoryManager memoryManager;

    @Autowired
    public ChatService(
            @Autowired(required = false) ChatClient.Builder chatClientBuilder,
            ConversationMemoryManager memoryManager
    ) {
        this.chatClient = (chatClientBuilder != null) ? chatClientBuilder.build() : null;
        this.memoryManager = memoryManager;
    }

    public ChatResponse sendMessage(ChatRequest request) {
        Long conversationId = request.conversationId();
        String userMessage = request.message();

        // Save user prompt in memory
        memoryManager.addMessage(conversationId, "user", userMessage);

        String reply;
        if (chatClient != null) {
            reply = chatClient.prompt()
                    .user(userMessage)
                    .call()
                    .content();
        } else {
            reply = "Cảm ơn bạn đã liên hệ nha khoa SmileCare. Tôi có thể giúp gì cho bạn?";
        }

        // Save assistant response in memory
        memoryManager.addMessage(conversationId, "assistant", reply);

        return new ChatResponse(conversationId, reply);
    }

    public void deleteSession(Long conversationId) {
        memoryManager.clearHistory(conversationId);
    }

    public List<ChatMessageDto> getHistory(Long conversationId) {
        return memoryManager.getHistory(conversationId);
    }
}