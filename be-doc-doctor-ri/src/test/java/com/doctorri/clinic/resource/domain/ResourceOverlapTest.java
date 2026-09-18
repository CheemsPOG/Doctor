package com.doctorri.clinic.resource.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.doctorri.clinic.resource.domain.service.ResourceOverlap;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ResourceOverlapTest {

  @Test
  void detectsOverlap() {
    LocalDateTime a1 = LocalDateTime.of(2026, 7, 23, 9, 0);
    LocalDateTime a2 = LocalDateTime.of(2026, 7, 23, 9, 30);
    LocalDateTime b1 = LocalDateTime.of(2026, 7, 23, 9, 15);
    LocalDateTime b2 = LocalDateTime.of(2026, 7, 23, 9, 45);
    assertTrue(ResourceOverlap.overlaps(a1, a2, b1, b2));
  }

  @Test
  void adjacentNotOverlap() {
    LocalDateTime a1 = LocalDateTime.of(2026, 7, 23, 9, 0);
    LocalDateTime a2 = LocalDateTime.of(2026, 7, 23, 9, 30);
    LocalDateTime b1 = LocalDateTime.of(2026, 7, 23, 9, 30);
    LocalDateTime b2 = LocalDateTime.of(2026, 7, 23, 10, 0);
    assertFalse(ResourceOverlap.overlaps(a1, a2, b1, b2));
  }
}
