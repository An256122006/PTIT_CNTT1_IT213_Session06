package com.example.smilecare.tool;

import com.example.smilecare.entity.Doctor;
import com.example.smilecare.service.DoctorService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DoctorTool {

    private final DoctorService doctorService;

    public DoctorTool(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @Tool(name = "list_all_doctors",
            description = "Liệt kê tất cả bác sĩ đang nhận lịch tại phòng khám")
    public String listAllDoctors() {
        return formatDoctors(doctorService.findAll());
    }

    @Tool(name = "search_doctors",
            description = "Tìm kiếm bác sĩ theo tên hoặc chuyên khoa (ví dụ: chỉnh nha, răng sứ, nhổ răng khôn)")
    public String searchDoctors(@ToolParam(description = "Từ khóa tìm kiếm tên hoặc chuyên khoa bác sĩ") String keyword) {
        return formatDoctors(doctorService.search(keyword));
    }

    private String formatDoctors(List<Doctor> doctors) {
        if (doctors.isEmpty()) {
            return "Không tìm thấy bác sĩ phù hợp.";
        }
        return doctors.stream()
                .map(d -> "ID: " + d.getId()
                        + " | " + d.getName()
                        + " | Chuyên khoa: " + d.getSpecialty()
                        + " | Kinh nghiệm: " + d.getExperienceYears() + " năm"
                        + " | Phí khám: " + d.getFee() + " VNĐ")
                .collect(Collectors.joining("\n"));
    }
}