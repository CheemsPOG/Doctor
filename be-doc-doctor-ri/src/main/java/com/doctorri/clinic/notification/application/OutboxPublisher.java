package com.doctorri.clinic.notification.application;

import com.doctorri.clinic.notification.infrastructure.outbox.OutboxEventEntity;
import com.doctorri.clinic.notification.infrastructure.outbox.OutboxEventJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboxPublisher {
  private final OutboxEventJpaRepository outbox;
  private final ObjectMapper mapper;

  public OutboxPublisher(OutboxEventJpaRepository outbox, ObjectMapper mapper) {
    this.outbox = outbox;
    this.mapper = mapper;
  }

  @Transactional
  public void publish(String eventType, Long aggregateId, Map<String, Object> payload) {
    publishAt(eventType, aggregateId, payload, Instant.now());
  }

  @Transactional
  public void publishAt(String eventType, Long aggregateId, Map<String, Object> payload, Instant availableAt) {
    try {
      OutboxEventEntity e = new OutboxEventEntity();
      e.setAggregateType("APPOINTMENT");
      e.setAggregateId(aggregateId);
      e.setEventType(eventType);
      e.setPayload(mapper.writeValueAsString(payload));
      e.setStatus("PENDING");
      e.setAvailableAt(availableAt == null ? Instant.now() : availableAt);
      outbox.save(e);
    } catch (Exception ex) {
      throw new IllegalStateException("Cannot publish outbox", ex);
    }
  }

  @Transactional
  public void cancelPendingReminders(Long appointmentId) {
    List<OutboxEventEntity> pending = outbox.findPendingReminders(appointmentId);
    for (OutboxEventEntity e : pending) {
      e.setStatus("CANCELLED");
      e.setProcessedAt(Instant.now());
    }
  }
}
