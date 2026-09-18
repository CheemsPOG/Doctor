package com.doctorri.clinic.appointment.domain.service;

import java.time.LocalDateTime;
import java.util.Collection;

/**
 * Pure overlap-detection logic shared by doctor/room/equipment resource booking.
 * Two half-open intervals [start, end) overlap iff each starts before the other ends.
 */
public final class ResourceBookingOverlapChecker {

  private ResourceBookingOverlapChecker() {}

  public static boolean overlaps(LocalDateTime aStart, LocalDateTime aEnd, LocalDateTime bStart, LocalDateTime bEnd) {
    return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
  }

  public static boolean hasConflict(Collection<Window> existingBookings, LocalDateTime start, LocalDateTime end) {
    return existingBookings.stream().anyMatch(w -> overlaps(start, end, w.start(), w.end()));
  }

  public record Window(LocalDateTime start, LocalDateTime end) {}
}
