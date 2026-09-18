package com.doctorri.clinic.queue.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.doctorri.clinic.queue.domain.service.QueuePriorityPolicy;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class QueuePriorityPolicyTest {

  private final LocalDateTime scheduled = LocalDateTime.of(2026, 7, 20, 10, 0);

  @Test
  void medicalUrgentBeatsEverything() {
    assertEquals(
        QueuePriorityPolicy.MEDICAL_URGENT,
        QueuePriorityPolicy.compute("WALK_IN", scheduled, scheduled.plusHours(1), 15, true));
  }

  @Test
  void walkInIsLowestAmongStandardBuckets() {
    int walkIn = QueuePriorityPolicy.compute("WALK_IN", scheduled, scheduled, 15, false);
    int onTime = QueuePriorityPolicy.compute("ONLINE", scheduled, scheduled, 15, false);
    assertEquals(QueuePriorityPolicy.WALK_IN, walkIn);
    assertTrue(walkIn > onTime);
  }

  @Test
  void onTimeEarlyAndLateOrdering() {
    int onTime = QueuePriorityPolicy.compute("ONLINE", scheduled, scheduled.plusMinutes(2), 15, false);
    int early = QueuePriorityPolicy.compute("ONLINE", scheduled, scheduled.minusMinutes(30), 15, false);
    int late = QueuePriorityPolicy.compute("ONLINE", scheduled, scheduled.plusMinutes(10), 15, false);
    assertEquals(QueuePriorityPolicy.ON_TIME, onTime);
    assertEquals(QueuePriorityPolicy.EARLY, early);
    assertEquals(QueuePriorityPolicy.LATE_IN_GRACE, late);
    assertTrue(onTime < early && early < late && late < QueuePriorityPolicy.WALK_IN);
  }

  @Test
  void beyondGraceStillEnqueuedWithLateBucketPlusOffset() {
    int beyond = QueuePriorityPolicy.compute("RECEPTION", scheduled, scheduled.plusMinutes(40), 15, false);
    assertEquals(QueuePriorityPolicy.LATE_IN_GRACE + 5, beyond);
  }
}
