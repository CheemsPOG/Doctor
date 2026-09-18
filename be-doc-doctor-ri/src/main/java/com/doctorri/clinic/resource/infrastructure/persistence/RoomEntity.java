package com.doctorri.clinic.resource.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "rooms")
public class RoomEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "clinic_id", nullable = false)
  private Long clinicId;

  @Column(nullable = false)
  private String code;

  @Column(nullable = false)
  private String name;

  @Column(name = "room_type", nullable = false)
  private String roomType;

  @Column(nullable = false)
  private String status;

  public Long getId() {
    return id;
  }

  public Long getClinicId() {
    return clinicId;
  }

  public String getCode() {
    return code;
  }

  public String getName() {
    return name;
  }

  public String getRoomType() {
    return roomType;
  }

  public String getStatus() {
    return status;
  }
}
