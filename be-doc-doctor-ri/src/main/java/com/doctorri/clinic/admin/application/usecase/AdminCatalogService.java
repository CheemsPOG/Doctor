package com.doctorri.clinic.admin.application.usecase;

import com.doctorri.clinic.admin.application.dto.AdminCatalogDtos.DoctorAdminResponse;
import com.doctorri.clinic.admin.application.dto.AdminCatalogDtos.ScheduleAdminResponse;
import com.doctorri.clinic.admin.application.dto.AdminCatalogDtos.ServiceAdminResponse;
import com.doctorri.clinic.admin.application.dto.ReplaceDoctorServicesRequest;
import com.doctorri.clinic.admin.application.dto.UpsertDoctorRequest;
import com.doctorri.clinic.admin.application.dto.UpsertDoctorScheduleRequest;
import com.doctorri.clinic.admin.application.dto.UpsertServiceRequest;
import com.doctorri.clinic.audit.application.AuditService;
import com.doctorri.clinic.doctor.infrastructure.persistence.DoctorEntity;
import com.doctorri.clinic.doctor.infrastructure.persistence.DoctorJpaRepository;
import com.doctorri.clinic.doctor.infrastructure.persistence.DoctorScheduleEntity;
import com.doctorri.clinic.doctor.infrastructure.persistence.DoctorScheduleJpaRepository;
import com.doctorri.clinic.doctor.infrastructure.persistence.DoctorServiceEntity;
import com.doctorri.clinic.doctor.infrastructure.persistence.DoctorServiceJpaRepository;
import com.doctorri.clinic.servicecatalog.infrastructure.persistence.ServiceEntity;
import com.doctorri.clinic.servicecatalog.infrastructure.persistence.ServiceJpaRepository;
import com.doctorri.clinic.shared.domain.exception.DomainException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.time.LocalTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminCatalogService {

  private final ServiceJpaRepository services;
  private final DoctorJpaRepository doctors;
  private final DoctorScheduleJpaRepository schedules;
  private final DoctorServiceJpaRepository doctorServices;
  private final AuditService audit;

  public AdminCatalogService(
      ServiceJpaRepository services,
      DoctorJpaRepository doctors,
      DoctorScheduleJpaRepository schedules,
      DoctorServiceJpaRepository doctorServices,
      AuditService audit) {
    this.services = services;
    this.doctors = doctors;
    this.schedules = schedules;
    this.doctorServices = doctorServices;
    this.audit = audit;
  }

  // ── Services ──────────────────────────────────────────────

  @Transactional(readOnly = true)
  public List<ServiceAdminResponse> listServices() {
    return services.findAllByOrderByNameAsc().stream().map(ServiceAdminResponse::from).toList();
  }

  @Transactional
  public ServiceAdminResponse createService(UpsertServiceRequest req, Long actorUserId) {
    String code = req.code().trim().toUpperCase();
    services.findByCode(code).ifPresent(s -> {
      throw new DomainException("SERVICE_CODE_EXISTS", "Mã dịch vụ đã tồn tại.");
    });
    ServiceEntity entity = new ServiceEntity();
    applyService(entity, req);
    services.save(entity);
    audit.log(actorUserId, "CREATE_SERVICE", "SERVICE", entity.getId(), entity.getCode());
    return ServiceAdminResponse.from(entity);
  }

  @Transactional
  public ServiceAdminResponse updateService(Long id, UpsertServiceRequest req, Long actorUserId) {
    ServiceEntity entity = services.findById(id)
        .orElseThrow(() -> new DomainException("SERVICE_NOT_FOUND", "Không tìm thấy dịch vụ."));
    String code = req.code().trim().toUpperCase();
    services.findByCode(code).ifPresent(existing -> {
      if (!existing.getId().equals(id)) {
        throw new DomainException("SERVICE_CODE_EXISTS", "Mã dịch vụ đã tồn tại.");
      }
    });
    applyService(entity, req);
    audit.log(actorUserId, "UPDATE_SERVICE", "SERVICE", id, entity.getCode());
    return ServiceAdminResponse.from(entity);
  }

  @Transactional
  public ServiceAdminResponse deactivateService(Long id, Long actorUserId) {
    ServiceEntity entity = services.findById(id)
        .orElseThrow(() -> new DomainException("SERVICE_NOT_FOUND", "Không tìm thấy dịch vụ."));
    entity.setStatus("INACTIVE");
    audit.log(actorUserId, "DEACTIVATE_SERVICE", "SERVICE", id, entity.getCode());
    return ServiceAdminResponse.from(entity);
  }

  private void applyService(ServiceEntity entity, UpsertServiceRequest req) {
    entity.setCode(req.code().trim().toUpperCase());
    entity.setName(req.name().trim());
    entity.setCategory(req.category().trim().toUpperCase());
    entity.setDescription(req.description());
    entity.setDefaultDurationMinutes(req.durationMinutes());
    entity.setBufferBeforeMinutes(req.bufferBeforeMinutes());
    entity.setBufferAfterMinutes(req.bufferAfterMinutes());
    entity.setHoldRoomOnBooking(req.holdRoomOnBooking());
    entity.setRequiresUltrasound(req.requiresUltrasound());
    entity.setBookingPolicy(
        req.bookingPolicy() == null || req.bookingPolicy().isBlank() ? "STANDARD" : req.bookingPolicy());
    entity.setStatus(normalizeStatus(req.status()));
  }

  // ── Doctors ───────────────────────────────────────────────

  @Transactional(readOnly = true)
  public List<DoctorAdminResponse> listDoctors() {
    return doctors.findAllByOrderByFullNameAsc().stream()
        .map(d -> DoctorAdminResponse.from(d, activeServiceIds(d.getId())))
        .toList();
  }

  @Transactional
  public DoctorAdminResponse createDoctor(UpsertDoctorRequest req, Long actorUserId) {
    String code = req.doctorCode().trim().toUpperCase();
    doctors.findByDoctorCode(code).ifPresent(d -> {
      throw new DomainException("DOCTOR_CODE_EXISTS", "Mã bác sĩ đã tồn tại.");
    });
    DoctorEntity entity = new DoctorEntity();
    applyDoctor(entity, req);
    doctors.save(entity);
    audit.log(actorUserId, "CREATE_DOCTOR", "DOCTOR", entity.getId(), entity.getDoctorCode());
    return DoctorAdminResponse.from(entity, List.of());
  }

  @Transactional
  public DoctorAdminResponse updateDoctor(Long id, UpsertDoctorRequest req, Long actorUserId) {
    DoctorEntity entity = requireDoctor(id);
    String code = req.doctorCode().trim().toUpperCase();
    doctors.findByDoctorCode(code).ifPresent(existing -> {
      if (!existing.getId().equals(id)) {
        throw new DomainException("DOCTOR_CODE_EXISTS", "Mã bác sĩ đã tồn tại.");
      }
    });
    applyDoctor(entity, req);
    audit.log(actorUserId, "UPDATE_DOCTOR", "DOCTOR", id, entity.getDoctorCode());
    return DoctorAdminResponse.from(entity, activeServiceIds(id));
  }

  @Transactional
  public DoctorAdminResponse deactivateDoctor(Long id, Long actorUserId) {
    DoctorEntity entity = requireDoctor(id);
    entity.setStatus("INACTIVE");
    audit.log(actorUserId, "DEACTIVATE_DOCTOR", "DOCTOR", id, entity.getDoctorCode());
    return DoctorAdminResponse.from(entity, activeServiceIds(id));
  }

  private void applyDoctor(DoctorEntity entity, UpsertDoctorRequest req) {
    entity.setClinicId(req.clinicId() != null ? req.clinicId() : 1L);
    entity.setDoctorCode(req.doctorCode().trim().toUpperCase());
    entity.setFullName(req.fullName().trim());
    entity.setSpecialty(req.specialty().trim());
    entity.setBio(req.bio());
    entity.setUserId(req.userId());
    entity.setStatus(normalizeStatus(req.status()));
  }

  // ── Doctor ↔ Service mapping ──────────────────────────────

  @Transactional(readOnly = true)
  public List<Long> listDoctorServiceIds(Long doctorId) {
    requireDoctor(doctorId);
    return activeServiceIds(doctorId);
  }

  @Transactional
  public List<Long> replaceDoctorServices(Long doctorId, ReplaceDoctorServicesRequest req, Long actorUserId) {
    requireDoctor(doctorId);
    Set<Long> desired = new HashSet<>(req.serviceIds() == null ? List.of() : req.serviceIds());
    for (Long serviceId : desired) {
      services.findById(serviceId)
          .orElseThrow(() -> new DomainException("SERVICE_NOT_FOUND", "Không tìm thấy dịch vụ id=" + serviceId));
    }

    List<DoctorServiceEntity> existing = doctorServices.findByDoctorId(doctorId);
    Set<Long> seen = new HashSet<>();
    for (DoctorServiceEntity row : existing) {
      if (desired.contains(row.getServiceId())) {
        row.setStatus("ACTIVE");
        seen.add(row.getServiceId());
      } else {
        row.setStatus("INACTIVE");
      }
    }
    for (Long serviceId : desired) {
      if (!seen.contains(serviceId)) {
        DoctorServiceEntity row = new DoctorServiceEntity();
        row.setDoctorId(doctorId);
        row.setServiceId(serviceId);
        row.setStatus("ACTIVE");
        doctorServices.save(row);
      }
    }
    audit.log(actorUserId, "REPLACE_DOCTOR_SERVICES", "DOCTOR", doctorId, desired.toString());
    return activeServiceIds(doctorId);
  }

  // ── Schedules ─────────────────────────────────────────────

  @Transactional(readOnly = true)
  public List<ScheduleAdminResponse> listSchedules(Long doctorId) {
    requireDoctor(doctorId);
    return schedules.findByDoctorIdOrderByDayOfWeekAscStartTimeAsc(doctorId).stream()
        .map(ScheduleAdminResponse::from)
        .toList();
  }

  @Transactional
  public ScheduleAdminResponse createSchedule(
      Long doctorId, UpsertDoctorScheduleRequest req, Long actorUserId) {
    requireDoctor(doctorId);
    validateScheduleTimes(req);
    assertNoOverlap(doctorId, null, req);
    DoctorScheduleEntity entity = new DoctorScheduleEntity();
    entity.setDoctorId(doctorId);
    applySchedule(entity, req);
    schedules.save(entity);
    audit.log(actorUserId, "CREATE_SCHEDULE", "DOCTOR_SCHEDULE", entity.getId(),
        "doctor=" + doctorId + " dow=" + req.dayOfWeek());
    return ScheduleAdminResponse.from(entity);
  }

  @Transactional
  public ScheduleAdminResponse updateSchedule(
      Long scheduleId, UpsertDoctorScheduleRequest req, Long actorUserId) {
    DoctorScheduleEntity entity = schedules.findById(scheduleId)
        .orElseThrow(() -> new DomainException("SCHEDULE_NOT_FOUND", "Không tìm thấy lịch làm việc."));
    validateScheduleTimes(req);
    assertNoOverlap(entity.getDoctorId(), scheduleId, req);
    applySchedule(entity, req);
    audit.log(actorUserId, "UPDATE_SCHEDULE", "DOCTOR_SCHEDULE", scheduleId, null);
    return ScheduleAdminResponse.from(entity);
  }

  @Transactional
  public void deactivateSchedule(Long scheduleId, Long actorUserId) {
    DoctorScheduleEntity entity = schedules.findById(scheduleId)
        .orElseThrow(() -> new DomainException("SCHEDULE_NOT_FOUND", "Không tìm thấy lịch làm việc."));
    entity.setStatus("INACTIVE");
    audit.log(actorUserId, "DEACTIVATE_SCHEDULE", "DOCTOR_SCHEDULE", scheduleId, null);
  }

  private void applySchedule(DoctorScheduleEntity entity, UpsertDoctorScheduleRequest req) {
    entity.setDayOfWeek(req.dayOfWeek());
    entity.setStartTime(req.startTime());
    entity.setEndTime(req.endTime());
    entity.setEffectiveFrom(req.effectiveFrom());
    entity.setEffectiveTo(req.effectiveTo());
    entity.setStatus(req.status() == null || req.status().isBlank()
        ? "ACTIVE" : normalizeStatus(req.status()));
  }

  private void validateScheduleTimes(UpsertDoctorScheduleRequest req) {
    if (!req.startTime().isBefore(req.endTime())) {
      throw new DomainException("INVALID_SCHEDULE", "startTime phải trước endTime.");
    }
    if (req.effectiveFrom() != null && req.effectiveTo() != null
        && req.effectiveTo().isBefore(req.effectiveFrom())) {
      throw new DomainException("INVALID_SCHEDULE", "effectiveTo phải >= effectiveFrom.");
    }
  }

  private void assertNoOverlap(Long doctorId, Long excludeId, UpsertDoctorScheduleRequest req) {
    List<DoctorScheduleEntity> sameDay = schedules
        .findByDoctorIdAndStatusOrderByDayOfWeekAscStartTimeAsc(doctorId, "ACTIVE")
        .stream()
        .filter(s -> s.getDayOfWeek() == req.dayOfWeek())
        .filter(s -> excludeId == null || !s.getId().equals(excludeId))
        .filter(s -> dateRangesOverlap(s, req))
        .toList();
    for (DoctorScheduleEntity existing : sameDay) {
      if (timesOverlap(existing.getStartTime(), existing.getEndTime(), req.startTime(), req.endTime())) {
        throw new DomainException("SCHEDULE_OVERLAP", "Khung giờ làm việc bị trùng.");
      }
    }
  }

  private static boolean dateRangesOverlap(DoctorScheduleEntity existing, UpsertDoctorScheduleRequest req) {
    var fromA = existing.getEffectiveFrom();
    var toA = existing.getEffectiveTo();
    var fromB = req.effectiveFrom();
    var toB = req.effectiveTo();
    boolean aEndsBeforeB = toA != null && fromB != null && toA.isBefore(fromB);
    boolean bEndsBeforeA = toB != null && fromA != null && toB.isBefore(fromA);
    return !(aEndsBeforeB || bEndsBeforeA);
  }

  private static boolean timesOverlap(LocalTime s1, LocalTime e1, LocalTime s2, LocalTime e2) {
    return s1.isBefore(e2) && s2.isBefore(e1);
  }

  private List<Long> activeServiceIds(Long doctorId) {
    return doctorServices.findByDoctorIdAndStatus(doctorId, "ACTIVE").stream()
        .map(DoctorServiceEntity::getServiceId)
        .toList();
  }

  private DoctorEntity requireDoctor(Long id) {
    return doctors.findById(id)
        .orElseThrow(() -> new DomainException("DOCTOR_NOT_FOUND", "Không tìm thấy bác sĩ."));
  }

  private static String normalizeStatus(String status) {
    String s = status.trim().toUpperCase();
    if (!s.equals("ACTIVE") && !s.equals("INACTIVE")) {
      throw new DomainException("INVALID_STATUS", "status phải là ACTIVE hoặc INACTIVE.");
    }
    return s;
  }
}
