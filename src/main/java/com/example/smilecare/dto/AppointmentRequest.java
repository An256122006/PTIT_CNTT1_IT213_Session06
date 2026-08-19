package com.example.smilecare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AppointmentRequest(
        @NotBlank(message = "Tên bệnh nhân không được để trống")
        String patientName,

        @NotBlank(message = "Số điện thoại không được để trống")
        String patientPhone,

        @NotNull(message = "Bác sĩ không được để trống")
        Long doctorId,

        @NotNull(message = "Dịch vụ không được để trống")
        Long serviceId,

        @NotNull(message = "Thời gian hẹn không được để trống")
        LocalDateTime appointmentDateTime,

        String note
) {
}