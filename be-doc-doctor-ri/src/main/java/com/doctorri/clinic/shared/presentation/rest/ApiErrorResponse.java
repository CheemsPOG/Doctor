package com.doctorri.clinic.shared.presentation.rest;

public record ApiErrorResponse(
    String code,
    String message,
    String traceId,
    Object details
) {}
