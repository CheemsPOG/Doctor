package com.doctorri.clinic.resource.application;

import com.doctorri.clinic.resource.domain.service.ResourceOverlap;
import com.doctorri.clinic.resource.infrastructure.persistence.EquipmentEntity;
import com.doctorri.clinic.resource.infrastructure.persistence.EquipmentJpaRepository;
import com.doctorri.clinic.resource.infrastructure.persistence.ResourceBookingEntity;
import com.doctorri.clinic.resource.infrastructure.persistence.ResourceBookingJpaRepository;
import com.doctorri.clinic.resource.infrastructure.persistence.RoomEntity;
import com.doctorri.clinic.resource.infrastructure.persistence.RoomJpaRepository;
import com.doctorri.clinic.servicecatalog.infrastructure.persistence.ServiceEntity;
import com.doctorri.clinic.shared.domain.exception.DomainException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResourceBookingService {

  private final ResourceBookingJpaRepository bookings;
  private final RoomJpaRepository rooms;
  private final EquipmentJpaRepository equipments;

  public ResourceBookingService(
      ResourceBookingJpaRepository bookings,
      RoomJpaRepository rooms,
      EquipmentJpaRepository equipments) {
    this.bookings = bookings;
    this.rooms = rooms;
    this.equipments = equipments;
  }

  @Transactional
  public void reserveForAppointment(
      Long appointmentId, Long doctorId, ServiceEntity service, LocalDateTime start, LocalDateTime end) {
    LocalDateTime bookStart = start.minusMinutes(service.getBufferBeforeMinutes());
    LocalDateTime bookEnd = end.plusMinutes(service.getBufferAfterMinutes());
    book("DOCTOR", doctorId, appointmentId, bookStart, bookEnd);
    if (service.isRequiresUltrasound() || service.isHoldRoomOnBooking()) {
      RoomEntity room = rooms.findByClinicIdAndRoomTypeAndStatus(1L, "ULTRASOUND", "ACTIVE").stream()
          .filter(r -> bookings.findOverlaps("ROOM", r.getId(), bookStart, bookEnd).isEmpty())
          .findFirst()
          .orElseThrow(() -> new DomainException("ROOM_NOT_AVAILABLE", "Không còn phòng siêu âm trống."));
      book("ROOM", room.getId(), appointmentId, bookStart, bookEnd);
      EquipmentEntity eq = equipments.findByEquipmentTypeAndStatus("ULTRASOUND", "ACTIVE").stream()
          .filter(e -> bookings.findOverlaps("EQUIPMENT", e.getId(), bookStart, bookEnd).isEmpty())
          .findFirst()
          .orElseThrow(() -> new DomainException("EQUIPMENT_NOT_AVAILABLE", "Không còn máy siêu âm trống."));
      book("EQUIPMENT", eq.getId(), appointmentId, bookStart, bookEnd);
    }
  }

  @Transactional
  public void releaseAppointment(Long appointmentId) {
    for (ResourceBookingEntity b : bookings.findByAppointmentId(appointmentId)) {
      b.setStatus("CANCELLED");
    }
  }

  private void book(String type, Long resourceId, Long appointmentId, LocalDateTime start, LocalDateTime end) {
    if (!bookings.findOverlaps(type, resourceId, start, end).isEmpty()) {
      throw new DomainException("SLOT_NOT_AVAILABLE", "Tài nguyên " + type + " bị trùng lịch.");
    }
    ResourceBookingEntity b = new ResourceBookingEntity();
    b.setAppointmentId(appointmentId);
    b.setResourceType(type);
    b.setResourceId(resourceId);
    b.setStartAt(start);
    b.setEndAt(end);
    b.setStatus("CONFIRMED");
    bookings.save(b);
  }

  public static boolean overlaps(LocalDateTime s1, LocalDateTime e1, LocalDateTime s2, LocalDateTime e2) {
    return ResourceOverlap.overlaps(s1, e1, s2, e2);
  }

  public List<RoomEntity> listRooms() {
    return rooms.findByClinicIdAndStatus(1L, "ACTIVE");
  }

  public List<EquipmentEntity> listEquipments() {
    return equipments.findByClinicIdAndStatus(1L, "ACTIVE");
  }
}
