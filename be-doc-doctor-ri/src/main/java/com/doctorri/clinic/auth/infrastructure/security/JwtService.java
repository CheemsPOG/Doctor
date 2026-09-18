package com.doctorri.clinic.auth.infrastructure.security;

import com.doctorri.clinic.auth.domain.model.UserRole;
import com.doctorri.clinic.shared.infrastructure.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final JwtProperties props;
  private final SecretKey key;

  public JwtService(JwtProperties props) {
    this.props = props;
    this.key = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
  }

  public String createAccessToken(Long userId, String email, UserRole role, Long patientId, Long doctorId) {
    Instant now = Instant.now();
    Instant exp = now.plusSeconds(props.accessTokenMinutes() * 60L);
    var builder = Jwts.builder()
        .id(UUID.randomUUID().toString())
        .subject(String.valueOf(userId))
        .claim("email", email)
        .claim("role", role.name())
        .issuedAt(Date.from(now))
        .expiration(Date.from(exp));
    if (patientId != null) {
      builder.claim("patientId", patientId);
    }
    if (doctorId != null) {
      builder.claim("doctorId", doctorId);
    }
    return builder.signWith(key).compact();
  }

  public String createRefreshTokenRaw() {
    return UUID.randomUUID() + "." + UUID.randomUUID();
  }

  public Claims parse(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
  }

  public Long userId(Claims claims) {
    return Long.valueOf(claims.getSubject());
  }

  public UserRole role(Claims claims) {
    return UserRole.valueOf(claims.get("role", String.class));
  }

  public Long patientId(Claims claims) {
    Object v = claims.get("patientId");
    return v == null ? null : ((Number) v).longValue();
  }

  public Long doctorId(Claims claims) {
    Object v = claims.get("doctorId");
    return v == null ? null : ((Number) v).longValue();
  }

  public long refreshTtlSeconds() {
    return props.refreshTokenDays() * 24L * 3600L;
  }
}
