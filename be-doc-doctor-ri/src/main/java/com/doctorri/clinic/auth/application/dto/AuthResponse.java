package com.doctorri.clinic.auth.application.dto;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    long expiresIn,
    Long userId,
    Long patientId,
    Long doctorId,
    String fullName,
    String email,
    String role
) {}
