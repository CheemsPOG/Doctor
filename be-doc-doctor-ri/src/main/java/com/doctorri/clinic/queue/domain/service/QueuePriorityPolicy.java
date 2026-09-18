package com.doctorri.clinic.queue.domain.service;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Queue priority per spec §5.4 — lower number = higher priority (ORDER BY priority ASC).
 *
 * <ol>
 *   <li>Medical urgent (staff) → 10</li>
 *   <li>Scheduled, on-time → 20</li>
 *   <li>Scheduled, early → 30</li>
 *   <li>Scheduled, late within grace → 40</li>
 *   <li>Walk-in → 50</li>
 * </ol>
 */
public final class QueuePriorityPolicy {

  public static final int MEDICAL_URGENT = 10;
  public static final int ON_TIME = 20;
  public static final int EARLY = 30;
  public static final int LATE_IN_GRACE = 40;
  public static final int WALK_IN = 50;
  public static final int DEFAULT = 100;

  /** On-time window: arrive within ±5 minutes of scheduled start counts as on-time. */
  private static final int ON_TIME_TOLERANCE_MINUTES = 5;

  private QueuePriorityPolicy() {}

  public static int compute(
      String appointmentSource,
      LocalDateTime scheduledStartAt,
      LocalDateTime checkedInAt,
      int lateGraceMinutes,
      boolean medicalUrgent) {
    if (medicalUrgent) {
      return MEDICAL_URGENT;
    }
    if (appointmentSource != null && "WALK_IN".equalsIgnoreCase(appointmentSource)) {
      return WALK_IN;
    }
    if (scheduledStartAt == null || checkedInAt == null) {
      return DEFAULT;
    }
    long minutesEarly = Duration.between(checkedInAt, scheduledStartAt).toMinutes();
    // positive minutesEarly => arrived before scheduled start
    if (Math.abs(minutesEarly) <= ON_TIME_TOLERANCE_MINUTES) {
      return ON_TIME;
    }
    if (minutesEarly > ON_TIME_TOLERANCE_MINUTES) {
      return EARLY;
    }
    // arrived after scheduled start
    long minutesLate = -minutesEarly;
    if (minutesLate <= lateGraceMinutes) {
      return LATE_IN_GRACE;
    }
    // beyond grace — still enqueue (no auto no-show); treat like late-in-grace for ordering
    return LATE_IN_GRACE + 5;
  }
}
