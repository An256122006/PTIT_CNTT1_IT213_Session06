package com.example.smilecare.tool;

import com.example.smilecare.entity.Appointment;
import com.example.smilecare.entity.Doctor;
import com.example.smilecare.service.AppointmentService;
import com.example.smilecare.service.DoctorService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ScheduleTool {

    private static final int OPEN_HOUR = 9;
    private static final int CLOSE_HOUR = 17;
    private static final int SLOT_DURATION_MINUTES = 30;

    private final AppointmentService appointmentService;
    private final DoctorService doctorService;

    public ScheduleTool(AppointmentService appointmentService, DoctorService doctorService) {
        this.appointmentService = appointmentService;
        this.doctorService = doctorService;
    }

    @Tool(name = "checkAvailableSlots",
            description = """
                    Kiểm tra các khung giờ còn trống của một bác sĩ trong một ngày cụ thể.
                    Sử dụng khi khách hàng muốn xem lịch trống của bác sĩ,
                    muốn biết bác sĩ nào rảnh vào ngày nào, hoặc muốn đặt lịch
                    khám và cần chọn khung giờ phù hợp.
                    Lưu ý: Tool này chỉ tra cứu thông tin, KHÔNG đặt lịch.
                    """)
    public String checkAvailableSlots(
            @ToolParam(description = "ID của bác sĩ cần tra cứu lịch trống") Long doctorId,
            @ToolParam(description = "Ngày cần tra cứu theo định dạng yyyy-MM-dd (ví dụ: 2025-08-25)") String date) {

        Doctor doctor;
        try {
            doctor = doctorService.findById(doctorId);
        } catch (IllegalArgumentException e) {
            return "Không tìm thấy bác sĩ với ID " + doctorId;
        }

        LocalDate targetDate;
        try {
            targetDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            return "Định dạng ngày không hợp lệ. Vui lòng sử dụng định dạng yyyy-MM-dd (ví dụ: 2025-08-25).";
        }

        if (targetDate.isBefore(LocalDate.now())) {
            return "Ngày " + date + " đã qua, vui lòng chọn ngày khác.";
        }

        List<Appointment> bookedAppointments = appointmentService.getBookedSlots(doctorId, targetDate);

        Set<LocalTime> bookedTimes = bookedAppointments.stream()
                .map(a -> a.getAppointmentDateTime().toLocalTime())
                .collect(Collectors.toSet());

        List<String> availableSlots = new ArrayList<>();
        for (int hour = OPEN_HOUR; hour < CLOSE_HOUR; hour++) {
            for (int minute = 0; minute < 60; minute += SLOT_DURATION_MINUTES) {
                LocalTime slot = LocalTime.of(hour, minute);
                if (!bookedTimes.contains(slot)) {
                    availableSlots.add(slot.format(DateTimeFormatter.ofPattern("HH:mm")));
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Lịch trống của bác sĩ ").append(doctor.getName())
                .append(" ngày ").append(date).append(":\n");

        if (availableSlots.isEmpty()) {
            sb.append("Không còn khung giờ trống trong ngày này.");
        } else {
            sb.append("Còn trống ").append(availableSlots.size()).append(" khung giờ:\n");
            sb.append(String.join(", ", availableSlots));
        }

        return sb.toString();
    }
}
