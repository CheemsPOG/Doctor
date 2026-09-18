package com.doctorri.clinic.doctor.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorJpaRepository extends JpaRepository<DoctorEntity, Long> {
  List<DoctorEntity> findByStatusOrderByFullNameAsc(String status);

  List<DoctorEntity> findAllByOrderByFullNameAsc();

  Optional<DoctorEntity> findByUserId(Long userId);

  Optional<DoctorEntity> findByDoctorCode(String doctorCode);
}
