package com.example.smilecare.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "doctors")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Tên bác sĩ không được để trống")
    private String name;

    @Column(nullable = false)
    @NotBlank(message = "Chuyên khoa không được để trống")
    private String specialty;

    @Column(nullable = false)
    @NotNull(message = "Số năm kinh nghiệm không được để trống")
    private Integer experienceYears;

    @Column(nullable = false)
    @NotNull(message = "Chi phí khám không được để trống")
    private Long fee;

    @Column(nullable = false)
    private Boolean available = true;

    public Doctor() {
    }

    public Doctor(String name, String specialty, Integer experienceYears, Long fee, Boolean available) {
        this.name = name;
        this.specialty = specialty;
        this.experienceYears = experienceYears;
        this.fee = fee;
        this.available = available;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public Long getFee() {
        return fee;
    }

    public void setFee(Long fee) {
        this.fee = fee;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }
}