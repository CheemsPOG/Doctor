package com.doctorri.clinic.queue.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.doctorri.clinic.queue.domain.service.QueueStatusMachine;
import com.doctorri.clinic.shared.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

class QueueStatusMachineTransitionTest {

  @Test
  void allowsWaitingToCalled() {
    assertDoesNotThrow(() -> QueueStatusMachine.assertTransition("WAITING", "CALLED"));
  }

  @Test
  void rejectsInvalidTransitions() {
    assertThrows(DomainException.class, () -> QueueStatusMachine.assertTransition("COMPLETED", "WAITING"));
    assertThrows(DomainException.class, () -> QueueStatusMachine.assertTransition("WAITING", "COMPLETED"));
  }
}
