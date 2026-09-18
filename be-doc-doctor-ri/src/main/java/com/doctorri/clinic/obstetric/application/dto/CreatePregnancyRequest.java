package com.doctorri.clinic.obstetric.application.dto;

import java.time.LocalDate;

public record CreatePregnancyRequest(
    Long patientId,
    LocalDate lastMenstrualPeriod,
    LocalDate estimatedDueDate,
    Long assignedDoctorId
) {}
