package com.example.smilecare.tool;

import com.example.smilecare.entity.Appointment;
import com.example.smilecare.service.AppointmentService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class ScheduleTool {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final AppointmentService appointmentService;

    public ScheduleTool(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }
    @Tool(name = "rescheduleBooking",
            description = "Đổi lịch hẹn sang ngày/giờ mới. "
                    + "Tự động kiểm tra khung giờ mới còn trống trước khi cập nhật. "
                    + "Cần mã lịch hẹn, số điện thoại và thời gian mới (định dạng: dd/MM/yyyy HH:mm).")
    public String rescheduleBooking(
            @ToolParam(description = "Mã lịch hẹn cần đổi (ID, ví dụ: 5)") Long appointmentId,
            @ToolParam(description = "Số điện thoại của bệnh nhân để xác minh quyền sở hữu") String patientPhone,
            @ToolParam(description = "Ngày giờ mới muốn đổi sang, định dạng dd/MM/yyyy HH:mm, ví dụ: 25/08/2026 10:00") String newDateTimeStr) {
        LocalDateTime newDateTime;
        try {
            newDateTime = LocalDateTime.parse(newDateTimeStr.trim(), FORMATTER);
        } catch (DateTimeParseException e) {
            return "Định dạng thời gian không hợp lệ. Vui lòng dùng định dạng: dd/MM/yyyy HH:mm (ví dụ: 25/08/2026 10:00)";
        }
        try {
            Appointment updated = appointmentService.rescheduleAppointment(appointmentId, patientPhone.trim(), newDateTime);
            return "Đã đổi lịch thành công!\n"
                    + "  • Mã lịch hẹn: #" + updated.getId() + "\n"
                    + "  • Bệnh nhân: " + updated.getPatientName() + "\n"
                    + "  • Bác sĩ: " + updated.getDoctor().getName() + "\n"
                    + "  • Dịch vụ: " + updated.getDentalService().getName() + "\n"
                    + "  • Thời gian mới: " + updated.getAppointmentDateTime().format(FORMATTER) + "\n"
                    + "  • Trạng thái: " + updated.getStatus();
        } catch (IllegalArgumentException e) {
            return "Không thể đổi lịch: " + e.getMessage();
        }
    }
}