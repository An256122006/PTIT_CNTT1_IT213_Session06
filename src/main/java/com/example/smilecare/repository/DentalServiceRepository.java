package com.example.smilecare.repository;

import com.example.smilecare.entity.DentalService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DentalServiceRepository extends JpaRepository<DentalService, Long> {

    List<DentalService> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);

    List<DentalService> findByPriceLessThanEqual(Long price);
}