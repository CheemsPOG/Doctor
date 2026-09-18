package com.doctorri.clinic.resource.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentJpaRepository extends JpaRepository<EquipmentEntity, Long> {
  List<EquipmentEntity> findByClinicIdAndStatus(Long clinicId, String status);

  List<EquipmentEntity> findByEquipmentTypeAndStatus(String equipmentType, String status);
}
