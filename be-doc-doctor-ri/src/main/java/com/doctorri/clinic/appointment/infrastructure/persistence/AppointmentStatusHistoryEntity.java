package com.doctorri.clinic.appointment.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "appointment_status_history")
public class AppointmentStatusHistoryEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "appointment_id", nullable = false)
  private Long appointmentId;

  @Column(name = "old_status")
  private String oldStatus;

  @Column(name = "new_status", nullable = false)
  private String newStatus;

  @Column(name = "changed_by")
  private Long changedBy;

  private String reason;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  public void setAppointmentId(Long appointmentId) {
    this.appointmentId = appointmentId;
  }

  public void setOldStatus(String oldStatus) {
    this.oldStatus = oldStatus;
  }

  public void setNewStatus(String newStatus) {
    this.newStatus = newStatus;
  }

  public void setChangedBy(Long changedBy) {
    this.changedBy = changedBy;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }
}
