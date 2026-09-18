package com.doctorri.clinic.auth.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import com.doctorri.clinic.auth.application.dto.LoginRequest;
import com.doctorri.clinic.auth.application.dto.RegisterRequest;
import com.doctorri.clinic.auth.application.usecase.AuthService;
import com.doctorri.clinic.auth.domain.model.UserRole;
import com.doctorri.clinic.auth.infrastructure.persistence.RefreshTokenJpaRepository;
import com.doctorri.clinic.auth.infrastructure.persistence.UserEntity;
import com.doctorri.clinic.auth.infrastructure.persistence.UserJpaRepository;
import com.doctorri.clinic.auth.infrastructure.security.JwtService;
import com.doctorri.clinic.doctor.infrastructure.persistence.DoctorJpaRepository;
import com.doctorri.clinic.patient.infrastructure.persistence.PatientEntity;
import com.doctorri.clinic.patient.infrastructure.persistence.PatientJpaRepository;
import com.doctorri.clinic.shared.domain.exception.DomainException;
import com.doctorri.clinic.shared.infrastructure.config.JwtProperties;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock UserJpaRepository users;
  @Mock PatientJpaRepository patients;
  @Mock DoctorJpaRepository doctors;
  @Mock RefreshTokenJpaRepository refreshTokens;
  @Mock PasswordEncoder passwordEncoder;
  @Mock JwtService jwtService;

  AuthService authService;
  Clock clock = Clock.fixed(Instant.parse("2026-07-20T03:00:00Z"), ZoneOffset.ofHours(7));
  JwtProperties jwtProperties = new JwtProperties("doctor-ri-clinic-dev-secret-change-me-32bytes!!", 120, 14);

  @BeforeEach
  void setUp() {
    authService = new AuthService(
        users, patients, doctors, refreshTokens, passwordEncoder, jwtService, jwtProperties, clock);
  }

  @Test
  void registerCreatesUserAndPatient() {
    when(users.existsByEmailIgnoreCase("a@b.com")).thenReturn(false);
    when(passwordEncoder.encode("Password123!")).thenReturn("hash");
    when(users.save(any(UserEntity.class))).thenAnswer(inv -> {
      UserEntity u = inv.getArgument(0);
      setId(u, 10L);
      return u;
    });
    when(patients.save(any(PatientEntity.class))).thenAnswer(inv -> {
      PatientEntity p = inv.getArgument(0);
      setId(p, 100L);
      return p;
    });
    when(jwtService.createAccessToken(eq(10L), eq("a@b.com"), eq(UserRole.PATIENT), eq(100L), isNull()))
        .thenReturn("access");
    when(jwtService.createRefreshTokenRaw()).thenReturn("refresh-raw");
    when(refreshTokens.save(any())).thenAnswer(inv -> inv.getArgument(0));

    var res = authService.register(new RegisterRequest("a@b.com", "Password123!", "Hoa", "0901"));
    assertEquals(10L, res.userId());
    assertEquals(100L, res.patientId());
    assertEquals("access", res.accessToken());
  }

  @Test
  void loginRejectsBadPassword() {
    UserEntity user = new UserEntity();
    user.setEmail("a@b.com");
    user.setPasswordHash("hash");
    user.setFullName("Hoa");
    user.setRole("PATIENT");
    when(users.findByEmailIgnoreCase("a@b.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);
    assertThrows(DomainException.class, () -> authService.login(new LoginRequest("a@b.com", "wrong")));
  }

  private static void setId(Object entity, Long id) {
    try {
      var f = entity.getClass().getDeclaredField("id");
      f.setAccessible(true);
      f.set(entity, id);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
