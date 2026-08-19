package com.example.smilecare.memory;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConversationMemoryManager {

    private final ChatMemory chatMemory;
    private final Map<String, Long> lastActiveAt = new ConcurrentHashMap<>();

    public ConversationMemoryManager(ChatMemory chatMemory) {
        this.chatMemory = chatMemory;
    }

    public String getOrCreateConversationId(String requestedId) {
        String conversationId = (requestedId == null || requestedId.isBlank())
                ? UUID.randomUUID().toString()
                : requestedId.trim();
        track(conversationId);
        return conversationId;
    }

    public boolean isNewConversation(String conversationId) {
        return !lastActiveAt.containsKey(conversationId);
    }

    public void track(String conversationId) {
        lastActiveAt.put(conversationId, System.currentTimeMillis());
    }

    public void clear(String conversationId) {
        chatMemory.clear(conversationId);
        lastActiveAt.remove(conversationId);
    }

    public List<Message> getHistory(String conversationId) {
        return chatMemory.get(conversationId);
    }

    public Set<String> getActiveConversations() {
        return lastActiveAt.keySet();
    }

    public int getActiveConversationCount() {
        return lastActiveAt.size();
    }
}