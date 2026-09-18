package com.doctorri.clinic.auth.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.doctorri.clinic.auth.domain.model.UserRole;
import com.doctorri.clinic.shared.infrastructure.config.JwtProperties;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

  JwtService jwtService;

  @BeforeEach
  void setUp() {
    jwtService = new JwtService(new JwtProperties(
        "doctor-ri-clinic-dev-secret-change-me-32bytes!!", 120, 14));
  }

  @Test
  void signsAndParsesAccessToken() {
    String token = jwtService.createAccessToken(7L, "a@b.com", UserRole.PATIENT, 9L, null);
    Claims claims = jwtService.parse(token);
    assertEquals(7L, jwtService.userId(claims));
    assertEquals(UserRole.PATIENT, jwtService.role(claims));
    assertEquals(9L, jwtService.patientId(claims));
  }

  @Test
  void rejectsInvalidToken() {
    assertThrows(Exception.class, () -> jwtService.parse("not.a.jwt"));
  }

  @Test
  void refreshTtlMatchesConfig() {
    assertEquals(14L * 24 * 3600, jwtService.refreshTtlSeconds());
  }
}
