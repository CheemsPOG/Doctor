package com.doctorri.clinic.doctor.presentation.rest;

import com.doctorri.clinic.doctor.infrastructure.persistence.DoctorEntity;
import com.doctorri.clinic.doctor.infrastructure.persistence.DoctorJpaRepository;
import com.doctorri.clinic.doctor.infrastructure.persistence.DoctorServiceEntity;
import com.doctorri.clinic.doctor.infrastructure.persistence.DoctorServiceJpaRepository;
import com.doctorri.clinic.servicecatalog.infrastructure.persistence.ServiceJpaRepository;
import com.doctorri.clinic.servicecatalog.presentation.rest.ServiceCatalogController.ServiceResponse;
import com.doctorri.clinic.shared.domain.exception.DomainException;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/doctors")
public class DoctorController {

  private final DoctorJpaRepository doctors;
  private final DoctorServiceJpaRepository doctorServices;
  private final ServiceJpaRepository services;

  public DoctorController(
      DoctorJpaRepository doctors,
      DoctorServiceJpaRepository doctorServices,
      ServiceJpaRepository services) {
    this.doctors = doctors;
    this.doctorServices = doctorServices;
    this.services = services;
  }

  @GetMapping
  public List<DoctorResponse> list() {
    return doctors.findByStatusOrderByFullNameAsc("ACTIVE").stream().map(DoctorResponse::from).toList();
  }

  @GetMapping("/{id}")
  public DoctorResponse get(@PathVariable Long id) {
    return doctors.findById(id)
        .filter(d -> "ACTIVE".equals(d.getStatus()))
        .map(DoctorResponse::from)
        .orElseThrow(() -> new DomainException("DOCTOR_NOT_FOUND", "Không tìm thấy bác sĩ."));
  }

  @GetMapping("/{id}/services")
  public List<ServiceResponse> services(@PathVariable Long id) {
    if (doctors.findById(id).filter(d -> "ACTIVE".equals(d.getStatus())).isEmpty()) {
      throw new DomainException("DOCTOR_NOT_FOUND", "Không tìm thấy bác sĩ.");
    }
    List<Long> serviceIds = doctorServices.findByDoctorIdAndStatus(id, "ACTIVE").stream()
        .map(DoctorServiceEntity::getServiceId)
        .toList();
    if (serviceIds.isEmpty()) {
      return List.of();
    }
    return services.findAllById(serviceIds).stream()
        .filter(s -> "ACTIVE".equals(s.getStatus()))
        .map(ServiceResponse::from)
        .toList();
  }

  public record DoctorResponse(
      Long id,
      String doctorCode,
      String fullName,
      String specialty,
      String bio
  ) {
    static DoctorResponse from(DoctorEntity d) {
      return new DoctorResponse(d.getId(), d.getDoctorCode(), d.getFullName(), d.getSpecialty(), d.getBio());
    }
  }
}
