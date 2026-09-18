package com.doctorri.clinic.queue.domain.service;

import com.doctorri.clinic.shared.domain.exception.DomainException;
import java.util.Map;
import java.util.Set;

public final class QueueStatusMachine {

  private static final Map<String, Set<String>> ALLOWED = Map.of(
      "WAITING", Set.of("CALLED", "CANCELLED", "SKIPPED"),
      "CALLED", Set.of("READY", "IN_SERVICE", "WAITING", "SKIPPED"),
      "READY", Set.of("IN_SERVICE", "WAITING"),
      "IN_SERVICE", Set.of("COMPLETED"),
      "COMPLETED", Set.of(),
      "SKIPPED", Set.of("WAITING"),
      "CANCELLED", Set.of()
  );

  private QueueStatusMachine() {}

  public static void assertTransition(String from, String to) {
    if (!ALLOWED.getOrDefault(from, Set.of()).contains(to)) {
      throw new DomainException("INVALID_QUEUE_STATUS", "Không chuyển được từ " + from + " sang " + to);
    }
  }
}
