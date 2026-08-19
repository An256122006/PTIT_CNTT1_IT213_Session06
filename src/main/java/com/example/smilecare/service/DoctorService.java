package com.example.smilecare.service;

import com.example.smilecare.entity.Doctor;
import com.example.smilecare.repository.DoctorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    public List<Doctor> findAll() {
        return doctorRepository.findByAvailableTrue();
    }

    public List<Doctor> search(String keyword) {
        return doctorRepository.findByNameContainingIgnoreCaseOrSpecialtyContainingIgnoreCase(keyword, keyword);
    }

    public Doctor findById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bác sĩ có ID " + id));
    }
}