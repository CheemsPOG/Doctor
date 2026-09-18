package com.doctorri.clinic.auth.infrastructure.security;

import com.doctorri.clinic.auth.domain.model.UserRole;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class AuthUserPrincipal implements UserDetails {

  private final Long userId;
  private final String email;
  private final UserRole role;
  private final Long patientId;
  private final Long doctorId;

  public AuthUserPrincipal(Long userId, String email, UserRole role, Long patientId, Long doctorId) {
    this.userId = userId;
    this.email = email;
    this.role = role;
    this.patientId = patientId;
    this.doctorId = doctorId;
  }

  public Long getUserId() {
    return userId;
  }

  public UserRole getRole() {
    return role;
  }

  public Long getPatientId() {
    return patientId;
  }

  public Long getDoctorId() {
    return doctorId;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
  }

  @Override
  public String getPassword() {
    return "";
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
