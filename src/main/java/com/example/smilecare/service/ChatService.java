package com.example.smilecare.service;

import com.example.smilecare.dto.ChatRequest;
import com.example.smilecare.dto.ChatResponse;
import com.example.smilecare.memory.ConversationMemoryManager;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final ConversationMemoryManager memoryManager;

    public ChatService(ChatClient chatClient, ConversationMemoryManager memoryManager) {
        this.chatClient = chatClient;
        this.memoryManager = memoryManager;
    }

    public ChatResponse chat(ChatRequest request) {
        String conversationId = memoryManager.getOrCreateConversationId(request.conversationId());
        String reply = chatClient.prompt()
                .user(request.message())
                .advisors(advisors -> advisors.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
        return new ChatResponse(conversationId, reply);
    }

    public void resetConversation(String conversationId) {
        memoryManager.clear(conversationId);
    }
}