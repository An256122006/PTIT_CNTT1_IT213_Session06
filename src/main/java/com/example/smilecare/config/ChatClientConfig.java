package com.example.smilecare.config;

import com.example.smilecare.tool.AppointmentTool;
import com.example.smilecare.tool.DoctorTool;
import com.example.smilecare.tool.ScheduleTool;
import com.example.smilecare.tool.ServiceTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient(ChatModel chatModel,
                                 DoctorTool doctorTool,
                                 ServiceTool serviceTool,
                                 ScheduleTool scheduleTool,
                                 AppointmentTool appointmentTool) {
        return ChatClient.builder(chatModel)
                .defaultTools(doctorTool, serviceTool, scheduleTool, appointmentTool)
                .build();
    }
}
