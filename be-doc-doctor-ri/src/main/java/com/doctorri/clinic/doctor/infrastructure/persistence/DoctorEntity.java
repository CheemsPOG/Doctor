package com.doctorri.clinic.doctor.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "doctors")
public class DoctorEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "clinic_id", nullable = false)
  private Long clinicId;

  @Column(name = "user_id")
  private Long userId;

  @Column(name = "doctor_code", nullable = false, unique = true)
  private String doctorCode;

  @Column(name = "full_name", nullable = false)
  private String fullName;

  @Column(nullable = false)
  private String specialty;

  private String bio;

  @Column(nullable = false)
  private String status;

  public Long getId() {
    return id;
  }

  public Long getClinicId() {
    return clinicId;
  }

  public void setClinicId(Long clinicId) {
    this.clinicId = clinicId;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getDoctorCode() {
    return doctorCode;
  }

  public void setDoctorCode(String doctorCode) {
    this.doctorCode = doctorCode;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getSpecialty() {
    return specialty;
  }

  public void setSpecialty(String specialty) {
    this.specialty = specialty;
  }

  public String getBio() {
    return bio;
  }

  public void setBio(String bio) {
    this.bio = bio;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
