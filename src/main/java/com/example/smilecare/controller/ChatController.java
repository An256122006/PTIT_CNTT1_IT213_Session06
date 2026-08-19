package com.example.smilecare.controller;

import com.example.smilecare.dto.AppointmentRequest;
import com.example.smilecare.dto.ChatRequest;
import com.example.smilecare.dto.ChatResponse;
import com.example.smilecare.entity.Appointment;
import com.example.smilecare.service.AppointmentService;
import com.example.smilecare.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatService chatService;
    private final AppointmentService appointmentService;

    public ChatController(ChatService chatService, AppointmentService appointmentService) {
        this.chatService = chatService;
        this.appointmentService = appointmentService;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return chatService.chat(request);
    }

    @PostMapping("/chat/reset")
    public Map<String, String> reset(@RequestBody ChatRequest request) {
        String conversationId = request.conversationId();
        if (conversationId == null || conversationId.isBlank()) {
            return Map.of("message", "Thiếu conversationId, không thể xóa hội thoại");
        }
        chatService.resetConversation(conversationId);
        return Map.of("message", "Đã xóa bộ nhớ hội thoại " + conversationId);
    }

    @PostMapping("/appointments")
    public Appointment bookAppointment(@Valid @RequestBody AppointmentRequest request) {
        return appointmentService.bookAppointment(
                request.patientName(),
                request.patientPhone(),
                request.doctorId(),
                request.serviceId(),
                request.appointmentDateTime());
    }
}