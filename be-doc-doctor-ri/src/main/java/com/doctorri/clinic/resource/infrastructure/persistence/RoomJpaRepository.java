package com.doctorri.clinic.resource.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomJpaRepository extends JpaRepository<RoomEntity, Long> {
  List<RoomEntity> findByClinicIdAndStatus(Long clinicId, String status);

  List<RoomEntity> findByClinicIdAndRoomTypeAndStatus(Long clinicId, String roomType, String status);

  Optional<RoomEntity> findByIdAndStatus(Long id, String status);
}
