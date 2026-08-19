package com.example.smilecare.memory;

import com.example.smilecare.dto.ChatMessageDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConversationMemoryManager {

    private final Map<Long, List<ChatMessageDto>> memoryStore = new ConcurrentHashMap<>();

    public void addMessage(Long conversationId, String role, String content) {
        memoryStore.computeIfAbsent(conversationId, id -> Collections.synchronizedList(new ArrayList<>()))
                .add(new ChatMessageDto(role, content));
    }

    public List<ChatMessageDto> getHistory(Long conversationId) {
        List<ChatMessageDto> history = memoryStore.get(conversationId);
        if (history == null) {
            return Collections.emptyList();
        }
        synchronized (history) {
            return new ArrayList<>(history);
        }
    }

    public void clearHistory(Long conversationId) {
        memoryStore.remove(conversationId);
    }
}