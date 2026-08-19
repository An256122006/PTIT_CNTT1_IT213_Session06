package com.example.smilecare.tool;

import com.example.smilecare.entity.Appointment;
import com.example.smilecare.service.AppointmentService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AppointmentTool {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final AppointmentService appointmentService;

    public AppointmentTool(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @Tool(name = "getBookingDetails",
            description = "Tra cứu chi tiết lịch hẹn bằng số điện thoại hoặc mã lịch hẹn (ID). "
                    + "Truyền số điện thoại để lấy tất cả lịch hẹn, hoặc truyền mã lịch hẹn (số nguyên) để lấy 1 lịch cụ thể.")
    public String getBookingDetails(
            @ToolParam(description = "Số điện thoại của bệnh nhân HOẶC mã lịch hẹn (ID, ví dụ: 5)") String phoneOrId) {
        try {
            // Nếu đầu vào là số nguyên → tra theo ID
            Long id = Long.parseLong(phoneOrId.trim());
            Appointment appt = appointmentService.findById(id);
            return "Chi tiết lịch hẹn:\n" + formatAppointment(appt);
        } catch (NumberFormatException e) {
            // Không phải số → tra theo số điện thoại
            List<Appointment> list = appointmentService.findByPhone(phoneOrId.trim());
            if (list.isEmpty()) {
                return "Không tìm thấy lịch hẹn nào với số điện thoại: " + phoneOrId;
            }
            String result = list.stream()
                    .map(this::formatAppointment)
                    .collect(Collectors.joining("\n---\n"));
            return "Tìm thấy " + list.size() + " lịch hẹn:\n" + result;
        }
    }

    @Tool(name = "cancelBooking",
            description = "Hủy lịch hẹn đã đặt, giải phóng khung giờ trống. "
                    + "Cần cung cấp mã lịch hẹn (ID) và số điện thoại để xác minh.")
    public String cancelBooking(
            @ToolParam(description = "Mã lịch hẹn cần hủy (ID, ví dụ: 5)") Long appointmentId,
            @ToolParam(description = "Số điện thoại của bệnh nhân để xác minh quyền sở hữu") String patientPhone) {
        try {
            Appointment cancelled = appointmentService.cancelAppointment(appointmentId, patientPhone.trim());
            return "Đã hủy thành công lịch hẹn #" + cancelled.getId()
                    + " của " + cancelled.getPatientName()
                    + " vào lúc " + cancelled.getAppointmentDateTime().format(FORMATTER)
                    + " với bác sĩ " + cancelled.getDoctor().getName()
                    + ". Khung giờ này đã được giải phóng.";
        } catch (IllegalArgumentException e) {
            return "Không thể hủy lịch: " + e.getMessage();
        }
    }

    private String formatAppointment(Appointment appt) {
        return "  • Mã lịch hẹn: #" + appt.getId() + "\n"
                + "  • Bệnh nhân: " + appt.getPatientName() + "\n"
                + "  • Số điện thoại: " + appt.getPatientPhone() + "\n"
                + "  • Bác sĩ: " + appt.getDoctor().getName() + " (" + appt.getDoctor().getSpecialty() + ")\n"
                + "  • Dịch vụ: " + appt.getDentalService().getName() + "\n"
                + "  • Thời gian: " + appt.getAppointmentDateTime().format(FORMATTER) + "\n"
                + "  • Trạng thái: " + appt.getStatus()
                + (appt.getNote() != null ? "\n  • Ghi chú: " + appt.getNote() : "");
    }
}