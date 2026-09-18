package com.doctorri.clinic.patient.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UpdatePatientProfileRequest(
    @NotBlank @Size(max = 255) String fullName,
    LocalDate dateOfBirth,
    @Size(max = 16) String gender,
    @Size(max = 32) String phone,
    @Size(max = 255) String email,
    @Size(max = 255) String emergencyContactName,
    @Size(max = 32) String emergencyContactPhone
) {}
