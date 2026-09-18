package com.doctorri.clinic.shared.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "doctorri.jwt")
public record JwtProperties(
    String secret,
    int accessTokenMinutes,
    int refreshTokenDays
) {}
