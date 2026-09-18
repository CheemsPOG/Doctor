package com.doctorri.clinic.servicecatalog.presentation.rest;

import com.doctorri.clinic.servicecatalog.infrastructure.persistence.ServiceEntity;
import com.doctorri.clinic.servicecatalog.infrastructure.persistence.ServiceJpaRepository;
import com.doctorri.clinic.shared.domain.exception.DomainException;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/services")
public class ServiceCatalogController {

  private final ServiceJpaRepository services;

  public ServiceCatalogController(ServiceJpaRepository services) {
    this.services = services;
  }

  @GetMapping
  public List<ServiceResponse> list() {
    return services.findByStatusOrderByNameAsc("ACTIVE").stream().map(ServiceResponse::from).toList();
  }

  @GetMapping("/{id}")
  public ServiceResponse get(@PathVariable Long id) {
    return services.findById(id)
        .map(ServiceResponse::from)
        .orElseThrow(() -> new DomainException("SERVICE_NOT_FOUND", "Không tìm thấy dịch vụ."));
  }

  public record ServiceResponse(
      Long id,
      String code,
      String name,
      String category,
      String description,
      int durationMinutes,
      boolean holdRoomOnBooking,
      boolean requiresUltrasound
  ) {
    public static ServiceResponse from(ServiceEntity s) {
      return new ServiceResponse(
          s.getId(),
          s.getCode(),
          s.getName(),
          s.getCategory(),
          s.getDescription(),
          s.getDefaultDurationMinutes(),
          s.isHoldRoomOnBooking(),
          s.isRequiresUltrasound());
    }
  }
}
