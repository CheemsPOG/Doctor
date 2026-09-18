package com.doctorri.clinic.doctor.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorServiceJpaRepository extends JpaRepository<DoctorServiceEntity, Long> {

  List<DoctorServiceEntity> findByDoctorIdAndStatus(Long doctorId, String status);

  List<DoctorServiceEntity> findByDoctorId(Long doctorId);

  Optional<DoctorServiceEntity> findByDoctorIdAndServiceId(Long doctorId, Long serviceId);

  boolean existsByDoctorIdAndServiceIdAndStatus(Long doctorId, Long serviceId, String status);
}
