package com.doctorri.clinic.servicecatalog.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "services")
public class ServiceEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String code;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String category;

  private String description;

  @Column(name = "default_duration_minutes", nullable = false)
  private int defaultDurationMinutes;

  @Column(name = "buffer_before_minutes", nullable = false)
  private int bufferBeforeMinutes = 5;

  @Column(name = "buffer_after_minutes", nullable = false)
  private int bufferAfterMinutes = 5;

  @Column(name = "hold_room_on_booking", nullable = false)
  private boolean holdRoomOnBooking;

  @Column(name = "requires_ultrasound", nullable = false)
  private boolean requiresUltrasound;

  @Column(name = "booking_policy", nullable = false)
  private String bookingPolicy = "STANDARD";

  @Column(nullable = false)
  private String status = "ACTIVE";

  public Long getId() {
    return id;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getDefaultDurationMinutes() {
    return defaultDurationMinutes;
  }

  public void setDefaultDurationMinutes(int defaultDurationMinutes) {
    this.defaultDurationMinutes = defaultDurationMinutes;
  }

  public int getBufferBeforeMinutes() {
    return bufferBeforeMinutes;
  }

  public void setBufferBeforeMinutes(int bufferBeforeMinutes) {
    this.bufferBeforeMinutes = bufferBeforeMinutes;
  }

  public int getBufferAfterMinutes() {
    return bufferAfterMinutes;
  }

  public void setBufferAfterMinutes(int bufferAfterMinutes) {
    this.bufferAfterMinutes = bufferAfterMinutes;
  }

  public boolean isHoldRoomOnBooking() {
    return holdRoomOnBooking;
  }

  public void setHoldRoomOnBooking(boolean holdRoomOnBooking) {
    this.holdRoomOnBooking = holdRoomOnBooking;
  }

  public boolean isRequiresUltrasound() {
    return requiresUltrasound;
  }

  public void setRequiresUltrasound(boolean requiresUltrasound) {
    this.requiresUltrasound = requiresUltrasound;
  }

  public String getBookingPolicy() {
    return bookingPolicy;
  }

  public void setBookingPolicy(String bookingPolicy) {
    this.bookingPolicy = bookingPolicy;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
