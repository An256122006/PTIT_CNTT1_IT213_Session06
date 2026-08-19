package com.example.smilecare.service;

import com.example.smilecare.entity.DentalService;
import com.example.smilecare.repository.DentalServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ServiceCatalogService {

    private final DentalServiceRepository dentalServiceRepository;

    public ServiceCatalogService(DentalServiceRepository dentalServiceRepository) {
        this.dentalServiceRepository = dentalServiceRepository;
    }

    public List<DentalService> findAll() {
        return dentalServiceRepository.findAll();
    }

    public List<DentalService> search(String keyword) {
        return dentalServiceRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
    }

    public DentalService findById(Long id) {
        return dentalServiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dịch vụ có ID " + id));
    }
}