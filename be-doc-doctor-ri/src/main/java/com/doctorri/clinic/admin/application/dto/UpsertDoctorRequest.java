package com.doctorri.clinic.admin.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpsertDoctorRequest(
    @NotBlank @Size(max = 32) String doctorCode,
    @NotBlank @Size(max = 255) String fullName,
    @NotBlank @Size(max = 128) String specialty,
    @Size(max = 1024) String bio,
    Long clinicId,
    Long userId,
    @NotBlank String status
) {}
