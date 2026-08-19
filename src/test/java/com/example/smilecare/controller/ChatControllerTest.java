package com.example.smilecare.controller;

import com.example.smilecare.dto.ChatMessageDto;
import com.example.smilecare.dto.ChatRequest;
import com.example.smilecare.dto.ChatResponse;
import com.example.smilecare.exception.GlobalExceptionHandler;
import com.example.smilecare.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ChatControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ChatService chatService;

    @InjectMocks
    private ChatController chatController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(chatController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testPostChat_Success() throws Exception {
        ChatResponse response = new ChatResponse(100L, "Chào bạn! Tôi có thể tư vấn gì cho bạn?");
        when(chatService.sendMessage(any(ChatRequest.class))).thenReturn(response);

        String jsonPayload = """
                {
                    "conversationId": 100,
                    "message": "Xin chào nha khoa"
                }
                """;

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").value(100))
                .andExpect(jsonPath("$.reply").value("Chào bạn! Tôi có thể tư vấn gì cho bạn?"));

        verify(chatService, times(1)).sendMessage(any(ChatRequest.class));
    }

    @Test
    void testPostChat_ValidationError_MissingConversationId() throws Exception {
        String jsonPayload = """
                {
                    "message": "Xin chào"
                }
                """;

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("conversationId không được để trống"));
    }

    @Test
    void testPostChat_ValidationError_BlankMessage() throws Exception {
        String jsonPayload = """
                {
                    "conversationId": 100,
                    "message": "   "
                }
                """;

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Tin nhắn không được để trống"));
    }

    @Test
    void testDeleteSession_Success() throws Exception {
        doNothing().when(chatService).deleteSession(100L);

        mockMvc.perform(delete("/api/chat/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Đã xóa lịch sử phiên trò chuyện thành công"))
                .andExpect(jsonPath("$.conversationId").value("100"));

        verify(chatService, times(1)).deleteSession(100L);
    }

    @Test
    void testGetHistory_Success() throws Exception {
        List<ChatMessageDto> history = List.of(
                new ChatMessageDto("user", "Xin chào"),
                new ChatMessageDto("assistant", "Chào bạn!")
        );
        when(chatService.getHistory(100L)).thenReturn(history);

        mockMvc.perform(get("/api/chat/100/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("user"))
                .andExpect(jsonPath("$[0].content").value("Xin chào"))
                .andExpect(jsonPath("$[1].role").value("assistant"))
                .andExpect(jsonPath("$[1].content").value("Chào bạn!"));

        verify(chatService, times(1)).getHistory(100L);
    }
}
