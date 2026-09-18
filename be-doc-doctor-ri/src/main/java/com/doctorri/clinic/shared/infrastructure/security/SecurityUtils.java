package com.doctorri.clinic.shared.infrastructure.security;

import com.doctorri.clinic.auth.infrastructure.security.AuthUserPrincipal;
import com.doctorri.clinic.shared.domain.exception.DomainException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
  private SecurityUtils() {}

  public static AuthUserPrincipal requireUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof AuthUserPrincipal p)) {
      throw new DomainException("UNAUTHORIZED", "Cần đăng nhập.");
    }
    return p;
  }
}
