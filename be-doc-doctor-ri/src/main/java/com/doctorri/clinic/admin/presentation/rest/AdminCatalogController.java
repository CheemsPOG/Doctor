package com.doctorri.clinic.admin.presentation.rest;

import com.doctorri.clinic.admin.application.dto.AdminCatalogDtos.DoctorAdminResponse;
import com.doctorri.clinic.admin.application.dto.AdminCatalogDtos.ScheduleAdminResponse;
import com.doctorri.clinic.admin.application.dto.AdminCatalogDtos.ServiceAdminResponse;
import com.doctorri.clinic.admin.application.dto.ReplaceDoctorServicesRequest;
import com.doctorri.clinic.admin.application.dto.UpsertDoctorRequest;
import com.doctorri.clinic.admin.application.dto.UpsertDoctorScheduleRequest;
import com.doctorri.clinic.admin.application.dto.UpsertServiceRequest;
import com.doctorri.clinic.admin.application.usecase.AdminCatalogService;
import com.doctorri.clinic.shared.infrastructure.security.SecurityUtils;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminCatalogController {

  private final AdminCatalogService catalog;

  public AdminCatalogController(AdminCatalogService catalog) {
    this.catalog = catalog;
  }

  // ── Services ──────────────────────────────────────────────

  @GetMapping("/services")
  public List<ServiceAdminResponse> listServices() {
    return catalog.listServices();
  }

  @PostMapping("/services")
  @ResponseStatus(HttpStatus.CREATED)
  public ServiceAdminResponse createService(@Valid @RequestBody UpsertServiceRequest request) {
    return catalog.createService(request, actorId());
  }

  @PutMapping("/services/{id}")
  public ServiceAdminResponse updateService(
      @PathVariable Long id, @Valid @RequestBody UpsertServiceRequest request) {
    return catalog.updateService(id, request, actorId());
  }

  @DeleteMapping("/services/{id}")
  public ServiceAdminResponse deactivateService(@PathVariable Long id) {
    return catalog.deactivateService(id, actorId());
  }

  // ── Doctors ───────────────────────────────────────────────

  @GetMapping("/doctors")
  public List<DoctorAdminResponse> listDoctors() {
    return catalog.listDoctors();
  }

  @PostMapping("/doctors")
  @ResponseStatus(HttpStatus.CREATED)
  public DoctorAdminResponse createDoctor(@Valid @RequestBody UpsertDoctorRequest request) {
    return catalog.createDoctor(request, actorId());
  }

  @PutMapping("/doctors/{id}")
  public DoctorAdminResponse updateDoctor(
      @PathVariable Long id, @Valid @RequestBody UpsertDoctorRequest request) {
    return catalog.updateDoctor(id, request, actorId());
  }

  @DeleteMapping("/doctors/{id}")
  public DoctorAdminResponse deactivateDoctor(@PathVariable Long id) {
    return catalog.deactivateDoctor(id, actorId());
  }

  @GetMapping("/doctors/{id}/services")
  public Map<String, List<Long>> doctorServices(@PathVariable Long id) {
    return Map.of("serviceIds", catalog.listDoctorServiceIds(id));
  }

  @PutMapping("/doctors/{id}/services")
  public Map<String, List<Long>> replaceDoctorServices(
      @PathVariable Long id, @Valid @RequestBody ReplaceDoctorServicesRequest request) {
    return Map.of("serviceIds", catalog.replaceDoctorServices(id, request, actorId()));
  }

  // ── Schedules ─────────────────────────────────────────────

  @GetMapping("/doctors/{id}/schedules")
  public List<ScheduleAdminResponse> listSchedules(@PathVariable Long id) {
    return catalog.listSchedules(id);
  }

  @PostMapping("/doctors/{id}/schedules")
  @ResponseStatus(HttpStatus.CREATED)
  public ScheduleAdminResponse createSchedule(
      @PathVariable Long id, @Valid @RequestBody UpsertDoctorScheduleRequest request) {
    return catalog.createSchedule(id, request, actorId());
  }

  @PutMapping("/schedules/{scheduleId}")
  public ScheduleAdminResponse updateSchedule(
      @PathVariable Long scheduleId, @Valid @RequestBody UpsertDoctorScheduleRequest request) {
    return catalog.updateSchedule(scheduleId, request, actorId());
  }

  @DeleteMapping("/schedules/{scheduleId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deactivateSchedule(@PathVariable Long scheduleId) {
    catalog.deactivateSchedule(scheduleId, actorId());
  }

  private static Long actorId() {
    return SecurityUtils.requireUser().getUserId();
  }
}
