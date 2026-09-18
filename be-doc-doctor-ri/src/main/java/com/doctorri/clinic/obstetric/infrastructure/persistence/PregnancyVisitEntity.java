package com.doctorri.clinic.obstetric.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "pregnancy_visits")
public class PregnancyVisitEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "pregnancy_id", nullable = false)
  private Long pregnancyId;

  @Column(name = "appointment_id")
  private Long appointmentId;

  @Column(name = "gestational_week")
  private Integer gestationalWeek;

  private BigDecimal weight;

  @Column(name = "blood_pressure")
  private String bloodPressure;

  @Column(name = "fetal_heart_rate")
  private Integer fetalHeartRate;

  @Column(name = "doctor_note", length = 2000)
  private String doctorNote;

  @Column(name = "next_visit_at")
  private LocalDateTime nextVisitAt;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  public Long getId() {
    return id;
  }

  public Long getPregnancyId() {
    return pregnancyId;
  }

  public void setPregnancyId(Long pregnancyId) {
    this.pregnancyId = pregnancyId;
  }

  public Long getAppointmentId() {
    return appointmentId;
  }

  public void setAppointmentId(Long appointmentId) {
    this.appointmentId = appointmentId;
  }

  public Integer getGestationalWeek() {
    return gestationalWeek;
  }

  public void setGestationalWeek(Integer gestationalWeek) {
    this.gestationalWeek = gestationalWeek;
  }

  public BigDecimal getWeight() {
    return weight;
  }

  public void setWeight(BigDecimal weight) {
    this.weight = weight;
  }

  public String getBloodPressure() {
    return bloodPressure;
  }

  public void setBloodPressure(String bloodPressure) {
    this.bloodPressure = bloodPressure;
  }

  public Integer getFetalHeartRate() {
    return fetalHeartRate;
  }

  public void setFetalHeartRate(Integer fetalHeartRate) {
    this.fetalHeartRate = fetalHeartRate;
  }

  public String getDoctorNote() {
    return doctorNote;
  }

  public void setDoctorNote(String doctorNote) {
    this.doctorNote = doctorNote;
  }

  public LocalDateTime getNextVisitAt() {
    return nextVisitAt;
  }

  public void setNextVisitAt(LocalDateTime nextVisitAt) {
    this.nextVisitAt = nextVisitAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
