package com.doctorri.clinic.notification.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PushDeviceJpaRepository extends JpaRepository<PushDeviceEntity, Long> {
  List<PushDeviceEntity> findByUserIdAndActiveTrue(Long userId);

  Optional<PushDeviceEntity> findByDeviceToken(String deviceToken);
}
