package com.doctorri.clinic.servicecatalog.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceJpaRepository extends JpaRepository<ServiceEntity, Long> {
  List<ServiceEntity> findByStatusOrderByNameAsc(String status);

  Optional<ServiceEntity> findByCode(String code);

  List<ServiceEntity> findAllByOrderByNameAsc();
}
