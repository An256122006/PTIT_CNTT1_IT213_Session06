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
public class AppointmentTool {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    private final AppointmentService appointmentService;

    public AppointmentTool(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @Tool(name = "createBooking",
            description = """
                    Đặt lịch hẹn khám nha khoa mới: lưu thông tin vào CSDL và trả về Mã lịch hẹn (bookingCode).
                    Chỉ gọi khi khách đã cung cấp ĐẦY ĐỦ: tên bệnh nhân, số điện thoại, ID bác sĩ,
                    ID dịch vụ và khung giờ còn trống (đã tra cứu bằng getAvailableTimeSlots).
                    Nếu thiếu thông tin, hãy hỏi lại khách trước khi gọi tool này.
                    """)
    public String createBooking(
            @ToolParam(description = "Tên bệnh nhân") String patientName,
            @ToolParam(description = "Số điện thoại bệnh nhân (10 chữ số)") String patientPhone,
            @ToolParam(description = "ID của bác sĩ (xem list_all_doctors)") Long doctorId,
            @ToolParam(description = "ID của dịch vụ (xem list_all_services)") Long serviceId,
            @ToolParam(description = "Thời gian hẹn, định dạng yyyy-MM-ddTHH:mm (ví dụ: 2026-08-20T14:00)") String appointmentDateTime) {

        LocalDateTime dateTime;
        try {
            dateTime = LocalDateTime.parse(appointmentDateTime);
        } catch (DateTimeParseException e) {
            return "Định dạng thời gian không hợp lệ. Vui lòng sử dụng định dạng yyyy-MM-ddTHH:mm (ví dụ: 2026-08-20T14:00).";
        }

        try {
            Appointment appointment = appointmentService.createBooking(
                    patientName, patientPhone, doctorId, serviceId, dateTime);
            return "Đặt lịch thành công! Mã lịch hẹn (bookingCode): " + appointment.getBookingCode()
                    + " | Bệnh nhân: " + appointment.getPatientName()
                    + " | Bác sĩ: " + appointment.getDoctor().getName()
                    + " | Dịch vụ: " + appointment.getDentalService().getName()
                    + " | Thời gian: " + appointment.getAppointmentDateTime().format(FORMATTER);
        } catch (Exception e) {
            return "Đặt lịch thất bại: " + e.getMessage() + " Vui lòng chọn khung giờ khác.";
        }
    }
}