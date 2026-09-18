package com.doctorri.clinic.patient.application.dto;

import java.time.LocalDate;

public record PatientProfileResponse(
    Long patientId,
    Long userId,
    String patientCode,
    String fullName,
    LocalDate dateOfBirth,
    String gender,
    String phone,
    String email,
    String emergencyContactName,
    String emergencyContactPhone
) {}
