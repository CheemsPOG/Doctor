package com.doctorri.clinic.queue.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "visit_queue")
public class VisitQueueEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "appointment_id", nullable = false)
  private Long appointmentId;

  @Column(name = "queue_type", nullable = false)
  private String queueType = "CONSULT";

  @Column(nullable = false)
  private int priority = 100;

  @Column(name = "queue_number", nullable = false)
  private int queueNumber;

  @Column(nullable = false)
  private String status = "WAITING";

  @Column(name = "assigned_room_id")
  private Long assignedRoomId;

  @Column(name = "checked_in_at")
  private LocalDateTime checkedInAt;

  @Column(name = "called_at")
  private LocalDateTime calledAt;

  @Column(name = "started_at")
  private LocalDateTime startedAt;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;

  public Long getId() {
    return id;
  }

  public Long getAppointmentId() {
    return appointmentId;
  }

  public void setAppointmentId(Long appointmentId) {
    this.appointmentId = appointmentId;
  }

  public String getQueueType() {
    return queueType;
  }

  public void setQueueType(String queueType) {
    this.queueType = queueType;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public int getQueueNumber() {
    return queueNumber;
  }

  public void setQueueNumber(int queueNumber) {
    this.queueNumber = queueNumber;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Long getAssignedRoomId() {
    return assignedRoomId;
  }

  public void setAssignedRoomId(Long assignedRoomId) {
    this.assignedRoomId = assignedRoomId;
  }

  public LocalDateTime getCheckedInAt() {
    return checkedInAt;
  }

  public void setCheckedInAt(LocalDateTime checkedInAt) {
    this.checkedInAt = checkedInAt;
  }

  public LocalDateTime getCalledAt() {
    return calledAt;
  }

  public void setCalledAt(LocalDateTime calledAt) {
    this.calledAt = calledAt;
  }

  public LocalDateTime getStartedAt() {
    return startedAt;
  }

  public void setStartedAt(LocalDateTime startedAt) {
    this.startedAt = startedAt;
  }

  public LocalDateTime getCompletedAt() {
    return completedAt;
  }

  public void setCompletedAt(LocalDateTime completedAt) {
    this.completedAt = completedAt;
  }
}
