package com.example.smilecare.service;

import com.example.smilecare.entity.Appointment;
import com.example.smilecare.entity.DentalService;
import com.example.smilecare.entity.Doctor;
import com.example.smilecare.repository.AppointmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentService {

    private static final int OPEN_HOUR = 9;
    private static final int CLOSE_HOUR = 17;

    private final AppointmentRepository appointmentRepository;
    private final DoctorService doctorService;
    private final ServiceCatalogService serviceCatalogService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              DoctorService doctorService,
                              ServiceCatalogService serviceCatalogService) {
        this.appointmentRepository = appointmentRepository;
        this.doctorService = doctorService;
        this.serviceCatalogService = serviceCatalogService;
    }

    @Transactional
    public Appointment bookAppointment(String patientName, String patientPhone,
                                        Long doctorId, Long serviceId,
                                        LocalDateTime appointmentDateTime) {
        Doctor doctor = doctorService.findById(doctorId);
        if (!Boolean.TRUE.equals(doctor.getAvailable())) {
            throw new IllegalArgumentException("Bác sĩ " + doctor.getName() + " hiện không nhận lịch mới");
        }
        DentalService dentalService = serviceCatalogService.findById(serviceId);

        validateBusinessHours(appointmentDateTime);
        if (appointmentRepository.existsByDoctorIdAndAppointmentDateTime(doctorId, appointmentDateTime)) {
            throw new IllegalArgumentException("Khung giờ " + appointmentDateTime + " đã có người đặt, vui lòng chọn giờ khác");
        }

        Appointment appointment = new Appointment(
                patientName, patientPhone, doctor, dentalService,
                appointmentDateTime, "CONFIRMED", null);
        return appointmentRepository.save(appointment);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findByPhone(String patientPhone) {
        return appointmentRepository.findByPatientPhone(patientPhone);
    }

    @Transactional(readOnly = true)
    public Appointment findById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch hẹn với mã: " + id));
    }

    @Transactional(readOnly = true)
    public boolean isSlotBooked(Long doctorId, LocalDateTime dateTime) {
        return appointmentRepository.existsByDoctorIdAndAppointmentDateTime(doctorId, dateTime);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getBookedSlots(Long doctorId, LocalDate date) {
        return appointmentRepository.findByDoctorIdAndAppointmentDateTimeBetween(
                doctorId, date.atStartOfDay(), date.plusDays(1).atStartOfDay());
    }

    /**
     * UC-04: Hủy lịch hẹn, giải phóng khung giờ (đổi status → CANCELLED).
     * Xác minh quyền sở hữu bằng mã lịch hẹn + số điện thoại.
     */
    @Transactional
    public Appointment cancelAppointment(Long appointmentId, String patientPhone) {
        Appointment appointment = appointmentRepository.findByIdAndPatientPhone(appointmentId, patientPhone)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy lịch hẹn #" + appointmentId + " với số điện thoại " + patientPhone));
        if ("CANCELLED".equals(appointment.getStatus())) {
            throw new IllegalArgumentException("Lịch hẹn #" + appointmentId + " đã bị hủy trước đó");
        }
        appointment.setStatus("CANCELLED");
        return appointmentRepository.save(appointment);
    }

    /**
     * UC-05: Đổi lịch hẹn sang ngày/giờ mới.
     * Kiểm tra slot mới còn trống trước khi cập nhật.
     */
    @Transactional
    public Appointment rescheduleAppointment(Long appointmentId, String patientPhone,
                                             LocalDateTime newDateTime) {
        Appointment appointment = appointmentRepository.findByIdAndPatientPhone(appointmentId, patientPhone)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy lịch hẹn #" + appointmentId + " với số điện thoại " + patientPhone));
        if ("CANCELLED".equals(appointment.getStatus())) {
            throw new IllegalArgumentException("Lịch hẹn #" + appointmentId + " đã bị hủy, không thể đổi lịch");
        }
        validateBusinessHours(newDateTime);
        Long doctorId = appointment.getDoctor().getId();
        if (appointmentRepository.existsByDoctorIdAndAppointmentDateTime(doctorId, newDateTime)) {
            throw new IllegalArgumentException(
                    "Khung giờ " + newDateTime + " đã có người đặt, vui lòng chọn giờ khác");
        }
        appointment.setAppointmentDateTime(newDateTime);
        appointment.setStatus("CONFIRMED");
        return appointmentRepository.save(appointment);
    }

    private void validateBusinessHours(LocalDateTime dateTime) {
        if (dateTime.getHour() < OPEN_HOUR || dateTime.getHour() >= CLOSE_HOUR) {
            throw new IllegalArgumentException("Phòng khám chỉ nhận lịch hẹn trong khoảng 09:00 - 17:00");
        }
        if (dateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Không thể đặt lịch ở thời điểm đã qua");
        }
    }
}