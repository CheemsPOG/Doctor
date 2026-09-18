package com.doctorri.clinic.appointment.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.doctorri.clinic.appointment.domain.service.ResourceBookingOverlapChecker;
import com.doctorri.clinic.appointment.domain.service.ResourceBookingOverlapChecker.Window;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class ResourceBookingOverlapCheckerTest {

  private static final LocalDateTime BASE = LocalDateTime.of(2026, 7, 25, 9, 0);

  @Test
  void detectsDirectOverlap() {
    assertTrue(ResourceBookingOverlapChecker.overlaps(
        BASE, BASE.plusMinutes(30), BASE.plusMinutes(15), BASE.plusMinutes(45)));
  }

  @Test
  void adjacentIntervalsDoNotOverlap() {
    assertFalse(ResourceBookingOverlapChecker.overlaps(
        BASE, BASE.plusMinutes(30), BASE.plusMinutes(30), BASE.plusMinutes(60)));
  }

  @Test
  void hasConflictTrueWhenAnyWindowOverlaps() {
    List<Window> existing = List.of(
        new Window(BASE, BASE.plusMinutes(30)),
        new Window(BASE.plusHours(2), BASE.plusHours(3)));

    assertTrue(ResourceBookingOverlapChecker.hasConflict(existing, BASE.plusMinutes(10), BASE.plusMinutes(40)));
  }

  @Test
  void hasConflictFalseWhenNoWindowOverlaps() {
    List<Window> existing = List.of(new Window(BASE, BASE.plusMinutes(30)));

    assertFalse(ResourceBookingOverlapChecker.hasConflict(existing, BASE.plusMinutes(30), BASE.plusMinutes(60)));
  }

  @Test
  void identicalIntervalsOverlap() {
    assertTrue(ResourceBookingOverlapChecker.overlaps(BASE, BASE.plusMinutes(30), BASE, BASE.plusMinutes(30)));
  }
}
