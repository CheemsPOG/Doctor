package com.doctorri.clinic.appointment.infrastructure.persistence;

import com.doctorri.clinic.appointment.domain.model.AppointmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
public class AppointmentEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "appointment_code", nullable = false, unique = true)
  private String appointmentCode;

  @Column(name = "patient_id", nullable = false)
  private Long patientId;

  @Column(name = "clinic_id", nullable = false)
  private Long clinicId;

  @Column(name = "doctor_id", nullable = false)
  private Long doctorId;

  @Column(name = "service_id", nullable = false)
  private Long serviceId;

  @Column(name = "scheduled_start_at", nullable = false)
  private LocalDateTime scheduledStartAt;

  @Column(name = "scheduled_end_at", nullable = false)
  private LocalDateTime scheduledEndAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AppointmentStatus status;

  @Column(nullable = false)
  private String source = "ONLINE";

  private String reason;

  @Column(name = "late_cancel", nullable = false)
  private boolean lateCancel;

  @Column(name = "operational_status")
  private String operationalStatus;

  @Column(name = "hold_expires_at")
  private LocalDateTime holdExpiresAt;

  @Column(name = "created_by")
  private Long createdBy;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  @Version
  private Long version;

  public Long getId() {
    return id;
  }

  public String getAppointmentCode() {
    return appointmentCode;
  }

  public void setAppointmentCode(String appointmentCode) {
    this.appointmentCode = appointmentCode;
  }

  public Long getPatientId() {
    return patientId;
  }

  public void setPatientId(Long patientId) {
    this.patientId = patientId;
  }

  public Long getClinicId() {
    return clinicId;
  }

  public void setClinicId(Long clinicId) {
    this.clinicId = clinicId;
  }

  public Long getDoctorId() {
    return doctorId;
  }

  public void setDoctorId(Long doctorId) {
    this.doctorId = doctorId;
  }

  public Long getServiceId() {
    return serviceId;
  }

  public void setServiceId(Long serviceId) {
    this.serviceId = serviceId;
  }

  public LocalDateTime getScheduledStartAt() {
    return scheduledStartAt;
  }

  public void setScheduledStartAt(LocalDateTime scheduledStartAt) {
    this.scheduledStartAt = scheduledStartAt;
  }

  public LocalDateTime getScheduledEndAt() {
    return scheduledEndAt;
  }

  public void setScheduledEndAt(LocalDateTime scheduledEndAt) {
    this.scheduledEndAt = scheduledEndAt;
  }

  public AppointmentStatus getStatus() {
    return status;
  }

  public void setStatus(AppointmentStatus status) {
    this.status = status;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public boolean isLateCancel() {
    return lateCancel;
  }

  public void setLateCancel(boolean lateCancel) {
    this.lateCancel = lateCancel;
  }

  public String getOperationalStatus() {
    return operationalStatus;
  }

  public void setOperationalStatus(String operationalStatus) {
    this.operationalStatus = operationalStatus;
  }

  public LocalDateTime getHoldExpiresAt() {
    return holdExpiresAt;
  }

  public void setHoldExpiresAt(LocalDateTime holdExpiresAt) {
    this.holdExpiresAt = holdExpiresAt;
  }

  public Long getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(Long createdBy) {
    this.createdBy = createdBy;
  }

  public void touch() {
    this.updatedAt = Instant.now();
  }
}
