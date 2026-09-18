package com.doctorri.clinic.obstetric.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreatePregnancyVisitRequest(
    Long appointmentId,
    Integer gestationalWeek,
    BigDecimal weight,
    String bloodPressure,
    Integer fetalHeartRate,
    String doctorNote,
    LocalDateTime nextVisitAt
) {}
