package com.example.smilecare.service;

import com.example.smilecare.entity.Appointment;
import com.example.smilecare.entity.DentalService;
import com.example.smilecare.entity.Doctor;
import com.example.smilecare.entity.TimeSlot;
import com.example.smilecare.repository.AppointmentRepository;
import com.example.smilecare.repository.TimeSlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private static final int OPEN_HOUR = 9;
    private static final int CLOSE_HOUR = 17;
    private static final String BOOKING_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ0123456789";

    private final AppointmentRepository appointmentRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final DoctorService doctorService;
    private final ServiceCatalogService serviceCatalogService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              TimeSlotRepository timeSlotRepository,
                              DoctorService doctorService,
                              ServiceCatalogService serviceCatalogService) {
        this.appointmentRepository = appointmentRepository;
        this.timeSlotRepository = timeSlotRepository;
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

    @Transactional
    public List<TimeSlot> getAvailableTimeSlots(Long doctorId, Long serviceId, LocalDate date) {
        Doctor doctor = doctorService.findById(doctorId);
        DentalService service = serviceCatalogService.findById(serviceId);
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày " + date + " đã qua, vui lòng chọn ngày khác");
        }

        int duration = service.getDurationMinutes();
        LocalTime close = LocalTime.of(CLOSE_HOUR, 0);
        LocalTime cursor = LocalTime.of(OPEN_HOUR, 0);

        List<TimeSlot> availableSlots = new ArrayList<>();
        while (!cursor.plusMinutes(duration).isAfter(close)) {
            LocalTime start = cursor;
            LocalTime end = start.plusMinutes(duration);
            LocalDateTime startDateTime = date.atTime(start);
            LocalDateTime endDateTime = date.atTime(end);

            boolean hasOverlap = !appointmentRepository
                    .findByDoctorIdAndAppointmentDateTimeBetween(doctorId, startDateTime, endDateTime)
                    .isEmpty();
            if (!hasOverlap) {
                TimeSlot slot = timeSlotRepository
                        .findByDoctorIdAndSlotDateAndStartTime(doctorId, date, start)
                        .orElseGet(() -> timeSlotRepository.save(
                                new TimeSlot(doctor, date, start, end, TimeSlot.Status.AVAILABLE, null)));
                if (slot.getStatus() == TimeSlot.Status.AVAILABLE) {
                    availableSlots.add(slot);
                }
            }
            cursor = end;
        }
        return availableSlots;
    }

    @Transactional
    public Appointment createBooking(String patientName, String patientPhone,
                                     Long doctorId, Long serviceId,
                                     LocalDateTime appointmentDateTime) {
        Doctor doctor = doctorService.findById(doctorId);
        if (!Boolean.TRUE.equals(doctor.getAvailable())) {
            throw new IllegalArgumentException("Bác sĩ " + doctor.getName() + " hiện không nhận lịch mới");
        }
        DentalService service = serviceCatalogService.findById(serviceId);
        validateBusinessHours(appointmentDateTime);

        LocalTime startTime = appointmentDateTime.toLocalTime();
        LocalTime endTime = startTime.plusMinutes(service.getDurationMinutes());
        if (endTime.isAfter(LocalTime.of(CLOSE_HOUR, 0))) {
            throw new IllegalArgumentException("Dịch vụ " + service.getName() + " cần "
                    + service.getDurationMinutes() + " phút, vượt quá giờ đóng cửa 17:00, vui lòng chọn giờ sớm hơn");
        }

        List<Appointment> overlaps = appointmentRepository
                .findByDoctorIdAndAppointmentDateTimeBetween(doctorId, appointmentDateTime, appointmentDateTime.plusMinutes(service.getDurationMinutes()));
        if (!overlaps.isEmpty()) {
            throw new IllegalArgumentException("Khung giờ " + startTime + " ngày " + appointmentDateTime.toLocalDate()
                    + " của bác sĩ " + doctor.getName() + " đã có lịch hẹn, vui lòng chọn khung giờ khác");
        }

        TimeSlot slot = timeSlotRepository
                .findByDoctorIdAndSlotDateAndStartTime(doctorId, appointmentDateTime.toLocalDate(), startTime)
                .orElse(null);
        if (slot != null && slot.getStatus() == TimeSlot.Status.BOOKED) {
            throw new IllegalArgumentException("Khung giờ " + startTime + " ngày " + appointmentDateTime.toLocalDate()
                    + " đã được đặt, vui lòng chọn khung giờ khác");
        }

        String bookingCode = generateBookingCode();
        Appointment appointment = new Appointment(
                patientName, patientPhone, doctor, service,
                appointmentDateTime, "CONFIRMED", bookingCode, null);
        appointmentRepository.save(appointment);

        if (slot == null) {
            slot = new TimeSlot(doctor, appointmentDateTime.toLocalDate(),
                    startTime, endTime, TimeSlot.Status.BOOKED, bookingCode);
        } else {
            slot.setEndTime(endTime);
            slot.setStatus(TimeSlot.Status.BOOKED);
            slot.setBookingCode(bookingCode);
        }
        timeSlotRepository.save(slot);

        return appointment;
    }

    @Transactional(readOnly = true)
    public List<Appointment> findByPhone(String patientPhone) {
        return appointmentRepository.findByPatientPhone(patientPhone);
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

    private String generateBookingCode() {
        Random random = new Random();
        for (int attempt = 0; attempt < 5; attempt++) {
            String code = "SC-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-"
                    + random.ints(6, 0, BOOKING_CODE_CHARS.length())
                    .mapToObj(i -> String.valueOf(BOOKING_CODE_CHARS.charAt(i)))
                    .collect(Collectors.joining());
            if (!appointmentRepository.existsByBookingCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Không thể sinh mã lịch hẹn, vui lòng thử lại sau");
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