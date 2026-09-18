package com.doctorri.clinic.notification.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationDeliveryJpaRepository extends JpaRepository<NotificationDeliveryEntity, Long> {}
