package com.doctorri.clinic.admin.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record UpsertDoctorScheduleRequest(
    @NotNull @Min(1) @Max(7) Integer dayOfWeek,
    @NotNull LocalTime startTime,
    @NotNull LocalTime endTime,
    LocalDate effectiveFrom,
    LocalDate effectiveTo,
    String status
) {}
