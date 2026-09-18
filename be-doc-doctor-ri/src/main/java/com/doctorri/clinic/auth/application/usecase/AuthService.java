package com.doctorri.clinic.auth.application.usecase;

import com.doctorri.clinic.auth.application.dto.AuthResponse;
import com.doctorri.clinic.auth.application.dto.LoginRequest;
import com.doctorri.clinic.auth.application.dto.RefreshRequest;
import com.doctorri.clinic.auth.application.dto.RegisterRequest;
import com.doctorri.clinic.auth.domain.model.UserRole;
import com.doctorri.clinic.auth.infrastructure.persistence.RefreshTokenEntity;
import com.doctorri.clinic.auth.infrastructure.persistence.RefreshTokenJpaRepository;
import com.doctorri.clinic.auth.infrastructure.persistence.UserEntity;
import com.doctorri.clinic.auth.infrastructure.persistence.UserJpaRepository;
import com.doctorri.clinic.auth.infrastructure.security.JwtService;
import com.doctorri.clinic.doctor.infrastructure.persistence.DoctorJpaRepository;
import com.doctorri.clinic.patient.infrastructure.persistence.PatientEntity;
import com.doctorri.clinic.patient.infrastructure.persistence.PatientJpaRepository;
import com.doctorri.clinic.shared.domain.exception.DomainException;
import com.doctorri.clinic.shared.infrastructure.config.JwtProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HexFormat;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  private final UserJpaRepository users;
  private final PatientJpaRepository patients;
  private final DoctorJpaRepository doctors;
  private final RefreshTokenJpaRepository refreshTokens;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final JwtProperties jwtProperties;
  private final Clock clock;

  public AuthService(
      UserJpaRepository users,
      PatientJpaRepository patients,
      DoctorJpaRepository doctors,
      RefreshTokenJpaRepository refreshTokens,
      PasswordEncoder passwordEncoder,
      JwtService jwtService,
      JwtProperties jwtProperties,
      Clock clock) {
    this.users = users;
    this.patients = patients;
    this.doctors = doctors;
    this.refreshTokens = refreshTokens;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.jwtProperties = jwtProperties;
    this.clock = clock;
  }

  @Transactional
  public AuthResponse register(RegisterRequest request) {
    if (users.existsByEmailIgnoreCase(request.email())) {
      throw new DomainException("EMAIL_EXISTS", "Email đã được đăng ký.");
    }
    UserEntity user = new UserEntity();
    user.setEmail(request.email().trim().toLowerCase());
    user.setPhone(request.phone());
    user.setFullName(request.fullName().trim());
    user.setPasswordHash(passwordEncoder.encode(request.password()));
    user.setRole(UserRole.PATIENT.name());
    user.setStatus("ACTIVE");
    users.save(user);

    PatientEntity patient = new PatientEntity();
    patient.setUserId(user.getId());
    patient.setPatientCode("PT" + String.format("%06d", user.getId()));
    patient.setFullName(user.getFullName());
    patient.setPhone(user.getPhone());
    patient.setEmail(user.getEmail());
    patients.save(patient);
    return issueTokens(user, patient.getId(), null);
  }

  @Transactional
  public AuthResponse login(LoginRequest request) {
    UserEntity user = users.findByEmailIgnoreCase(request.email().trim())
        .orElseThrow(() -> new DomainException("INVALID_CREDENTIALS", "Email hoặc mật khẩu không đúng."));
    if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
      throw new DomainException("INVALID_CREDENTIALS", "Email hoặc mật khẩu không đúng.");
    }
    Long patientId = patients.findByUserId(user.getId()).map(PatientEntity::getId).orElse(null);
    Long doctorId = doctors.findByUserId(user.getId()).map(d -> d.getId()).orElse(null);
    return issueTokens(user, patientId, doctorId);
  }

  @Transactional
  public AuthResponse refresh(RefreshRequest request) {
    RefreshTokenEntity token = refreshTokens.findByTokenHash(sha256(request.refreshToken()))
        .orElseThrow(() -> new DomainException("INVALID_REFRESH", "Refresh token không hợp lệ."));
    if (token.isRevoked() || token.getExpiresAt().isBefore(LocalDateTime.now(clock))) {
      throw new DomainException("INVALID_REFRESH", "Refresh token không hợp lệ hoặc đã hết hạn.");
    }
    token.setRevoked(true);
    UserEntity user = users.findById(token.getUserId())
        .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "Không tìm thấy người dùng."));
    Long patientId = patients.findByUserId(user.getId()).map(PatientEntity::getId).orElse(null);
    Long doctorId = doctors.findByUserId(user.getId()).map(d -> d.getId()).orElse(null);
    return issueTokens(user, patientId, doctorId);
  }

  @Transactional
  public void logout(RefreshRequest request) {
    if (request == null || request.refreshToken() == null || request.refreshToken().isBlank()) {
      return;
    }
    refreshTokens.findByTokenHash(sha256(request.refreshToken())).ifPresent(t -> t.setRevoked(true));
  }

  private AuthResponse issueTokens(UserEntity user, Long patientId, Long doctorId) {
    UserRole role = UserRole.valueOf(user.getRole());
    String access = jwtService.createAccessToken(user.getId(), user.getEmail(), role, patientId, doctorId);
    String refresh = jwtService.createRefreshTokenRaw();
    RefreshTokenEntity entity = new RefreshTokenEntity();
    entity.setUserId(user.getId());
    entity.setTokenHash(sha256(refresh));
    entity.setExpiresAt(LocalDateTime.now(clock).plusDays(jwtProperties.refreshTokenDays()));
    entity.setRevoked(false);
    refreshTokens.save(entity);
    return new AuthResponse(
        access,
        refresh,
        jwtProperties.accessTokenMinutes() * 60L,
        user.getId(),
        patientId,
        doctorId,
        user.getFullName(),
        user.getEmail(),
        role.name());
  }

  private static String sha256(String raw) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(md.digest(raw.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
