package com.doctorri.clinic.resource.domain.service;

import java.time.LocalDateTime;

public final class ResourceOverlap {
  private ResourceOverlap() {}
  public static boolean overlaps(LocalDateTime s1, LocalDateTime e1, LocalDateTime s2, LocalDateTime e2) {
    return s1.isBefore(e2) && s2.isBefore(e1);
  }
}
