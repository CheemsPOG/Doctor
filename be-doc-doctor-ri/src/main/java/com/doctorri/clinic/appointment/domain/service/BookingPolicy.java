package com.doctorri.clinic.appointment.domain.service;

import com.doctorri.clinic.shared.domain.exception.DomainException;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Booking window and free-cancel rules (MVP defaults from clinic settings).
 */
public class BookingPolicy {

  private final int windowDays;
  private final int freeCancelHours;
  private final Clock clock;

  public BookingPolicy(int windowDays, int freeCancelHours, Clock clock) {
    this.windowDays = windowDays;
    this.freeCancelHours = freeCancelHours;
    this.clock = clock;
  }

  public void assertWithinBookingWindow(OffsetDateTime startAt) {
    OffsetDateTime now = OffsetDateTime.now(clock);
    if (!startAt.isAfter(now)) {
      throw new DomainException("INVALID_SLOT", "Thời gian khám phải sau thời điểm hiện tại.");
    }
    if (startAt.isAfter(now.plusDays(windowDays))) {
      throw new DomainException(
          "BOOKING_WINDOW_EXCEEDED",
          "Chỉ được đặt lịch trong vòng " + windowDays + " ngày tới.");
    }
  }

  public boolean isLateCancel(OffsetDateTime scheduledStartAt) {
    long hours = ChronoUnit.HOURS.between(OffsetDateTime.now(clock), scheduledStartAt);
    return hours < freeCancelHours;
  }
}
