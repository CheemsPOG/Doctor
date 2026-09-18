package com.doctorri.clinic.appointment.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.doctorri.clinic.appointment.domain.service.BookingPolicy;
import com.doctorri.clinic.shared.domain.exception.DomainException;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BookingPolicyTest {

  private BookingPolicy policy;
  private final ZoneOffset vn = ZoneOffset.ofHours(7);

  @BeforeEach
  void setUp() {
    Clock fixed = Clock.fixed(Instant.parse("2026-07-20T03:00:00Z"), vn);
    policy = new BookingPolicy(30, 24, fixed);
  }

  @Test
  void acceptsSlotWithinWindow() {
    OffsetDateTime start = OffsetDateTime.of(2026, 7, 25, 9, 0, 0, 0, vn);
    policy.assertWithinBookingWindow(start);
  }

  @Test
  void rejectsPastSlot() {
    OffsetDateTime start = OffsetDateTime.of(2026, 7, 19, 9, 0, 0, 0, vn);
    DomainException ex = assertThrows(DomainException.class, () -> policy.assertWithinBookingWindow(start));
    assertTrue(ex.getCode().equals("INVALID_SLOT"));
  }

  @Test
  void rejectsBeyondWindow() {
    OffsetDateTime start = OffsetDateTime.of(2026, 9, 1, 9, 0, 0, 0, vn);
    DomainException ex = assertThrows(DomainException.class, () -> policy.assertWithinBookingWindow(start));
    assertTrue(ex.getCode().equals("BOOKING_WINDOW_EXCEEDED"));
  }

  @Test
  void lateCancelWhenUnder24Hours() {
    OffsetDateTime start = OffsetDateTime.of(2026, 7, 20, 12, 0, 0, 0, vn);
    assertTrue(policy.isLateCancel(start));
  }

  @Test
  void freeCancelWhenOver24Hours() {
    OffsetDateTime start = OffsetDateTime.of(2026, 7, 25, 9, 0, 0, 0, vn);
    assertFalse(policy.isLateCancel(start));
  }
}
