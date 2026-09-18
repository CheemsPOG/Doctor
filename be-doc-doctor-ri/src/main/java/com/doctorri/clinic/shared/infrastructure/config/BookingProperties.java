package com.doctorri.clinic.shared.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "doctorri.booking")
public record BookingProperties(
    int holdMinutes,
    int windowDays,
    int freeCancelHours,
    int lateGraceMinutes
) {}
