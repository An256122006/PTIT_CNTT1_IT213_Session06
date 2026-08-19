package com.example.smilecare.repository;

import com.example.smilecare.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    Optional<TimeSlot> findByDoctorIdAndSlotDateAndStartTime(Long doctorId, LocalDate slotDate, LocalTime startTime);

    List<TimeSlot> findByDoctorIdAndSlotDate(Long doctorId, LocalDate slotDate);
}