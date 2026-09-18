package com.doctorri.clinic.notification.infrastructure.outbox;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "outbox_events")
public class OutboxEventEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @Column(name = "aggregate_type", nullable = false) private String aggregateType;
  @Column(name = "aggregate_id", nullable = false) private Long aggregateId;
  @Column(name = "event_type", nullable = false) private String eventType;
  @Column(nullable = false, columnDefinition = "json") private String payload;
  @Column(nullable = false) private String status = "PENDING";
  @Column(name = "retry_count", nullable = false) private int retryCount;
  @Column(name = "available_at", nullable = false) private Instant availableAt = Instant.now();
  @Column(name = "processed_at") private Instant processedAt;

  public Long getId() { return id; }
  public String getAggregateType() { return aggregateType; }
  public void setAggregateType(String aggregateType) { this.aggregateType = aggregateType; }
  public Long getAggregateId() { return aggregateId; }
  public void setAggregateId(Long aggregateId) { this.aggregateId = aggregateId; }
  public String getEventType() { return eventType; }
  public void setEventType(String eventType) { this.eventType = eventType; }
  public String getPayload() { return payload; }
  public void setPayload(String payload) { this.payload = payload; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public int getRetryCount() { return retryCount; }
  public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
  public Instant getAvailableAt() { return availableAt; }
  public void setAvailableAt(Instant availableAt) { this.availableAt = availableAt; }
  public Instant getProcessedAt() { return processedAt; }
  public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }
}
