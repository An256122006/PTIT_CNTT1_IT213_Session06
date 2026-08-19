package com.example.smilecare.config;

import com.example.smilecare.tool.AppointmentTool;
import com.example.smilecare.tool.DoctorTool;
import com.example.smilecare.tool.ScheduleTool;
import com.example.smilecare.tool.ServiceTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    public String smileCareSystemPrompt() {
        return """
                Bạn là trợ lý AI của phòng khám nha khoa SmileCare.
                Nhiệm vụ của bạn:
                - Giới thiệu bác sĩ, chuyên khoa và chi phí khám (dùng list_all_doctors / search_doctors).
                - Giới thiệu dịch vụ nha khoa và giá (dùng list_all_services / search_services).
                - Kiểm tra khung giờ còn trống của bác sĩ (dùng get_available_slots).
                - Đặt lịch hẹn khám cho bệnh nhân (dùng book_appointment).

                Quy tắc:
                - Luôn dùng dữ liệu thật từ các công cụ, không tự bịa thông tin.
                - Khi đặt lịch phải hỏi đủ: tên bệnh nhân, số điện thoại, bác sĩ, dịch vụ và thời gian mong muốn.
                - Nếu khung giờ không còn trống, gợi ý giờ khác còn trống.
                - Trả lời bằng tiếng Việt, thân thiện, ngắn gọn, dễ hiểu.
                """;
    }

    @Bean
    public ChatOptions.Builder smileCareChatOptions() {
        return ChatOptions.builder()
                .temperature(0.3);
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder,
                                 MessageChatMemoryAdvisor messageChatMemoryAdvisor,
                                 String smileCareSystemPrompt,
                                 ChatOptions.Builder smileCareChatOptions,
                                 DoctorTool doctorTool,
                                 ServiceTool serviceTool,
                                 ScheduleTool scheduleTool,
                                 AppointmentTool appointmentTool) {
        return builder
                .defaultSystem(smileCareSystemPrompt)
                .defaultOptions(smileCareChatOptions)
                .defaultAdvisors(messageChatMemoryAdvisor)
                .defaultTools(doctorTool, serviceTool, scheduleTool, appointmentTool)
                .build();
    }
}