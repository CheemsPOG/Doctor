package com.doctorri.clinic.obstetric.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "pregnancies")
public class PregnancyEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "patient_id", nullable = false)
  private Long patientId;

  @Column(name = "pregnancy_code", nullable = false, unique = true)
  private String pregnancyCode;

  @Column(name = "last_menstrual_period")
  private LocalDate lastMenstrualPeriod;

  @Column(name = "estimated_due_date")
  private LocalDate estimatedDueDate;

  @Column(name = "pregnancy_status", nullable = false)
  private String pregnancyStatus = "ACTIVE";

  @Column(name = "risk_level", nullable = false)
  private String riskLevel = "NORMAL";

  @Column(name = "assigned_doctor_id")
  private Long assignedDoctorId;

  public Long getId() {
    return id;
  }

  public Long getPatientId() {
    return patientId;
  }

  public void setPatientId(Long patientId) {
    this.patientId = patientId;
  }

  public String getPregnancyCode() {
    return pregnancyCode;
  }

  public void setPregnancyCode(String pregnancyCode) {
    this.pregnancyCode = pregnancyCode;
  }

  public LocalDate getLastMenstrualPeriod() {
    return lastMenstrualPeriod;
  }

  public void setLastMenstrualPeriod(LocalDate lastMenstrualPeriod) {
    this.lastMenstrualPeriod = lastMenstrualPeriod;
  }

  public LocalDate getEstimatedDueDate() {
    return estimatedDueDate;
  }

  public void setEstimatedDueDate(LocalDate estimatedDueDate) {
    this.estimatedDueDate = estimatedDueDate;
  }

  public String getPregnancyStatus() {
    return pregnancyStatus;
  }

  public void setPregnancyStatus(String pregnancyStatus) {
    this.pregnancyStatus = pregnancyStatus;
  }

  public String getRiskLevel() {
    return riskLevel;
  }

  public void setRiskLevel(String riskLevel) {
    this.riskLevel = riskLevel;
  }

  public Long getAssignedDoctorId() {
    return assignedDoctorId;
  }

  public void setAssignedDoctorId(Long assignedDoctorId) {
    this.assignedDoctorId = assignedDoctorId;
  }
}
