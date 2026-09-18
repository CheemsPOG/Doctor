package com.doctorri.clinic.appointment.application.usecase;

import com.doctorri.clinic.appointment.application.dto.AppointmentResponse;
import com.doctorri.clinic.appointment.application.dto.CreateAppointmentRequest;
import com.doctorri.clinic.appointment.application.dto.RescheduleAppointmentRequest;
import com.doctorri.clinic.appointment.domain.model.AppointmentStatus;
import com.doctorri.clinic.appointment.domain.service.BookingPolicy;
import com.doctorri.clinic.appointment.infrastructure.idempotency.IdempotencyService;
import com.doctorri.clinic.appointment.infrastructure.persistence.AppointmentEntity;
import com.doctorri.clinic.appointment.infrastructure.persistence.AppointmentJpaRepository;
import com.doctorri.clinic.appointment.infrastructure.persistence.AppointmentStatusHistoryEntity;
import com.doctorri.clinic.appointment.infrastructure.persistence.AppointmentStatusHistoryJpaRepository;
import com.doctorri.clinic.doctor.infrastructure.persistence.DoctorJpaRepository;
import com.doctorri.clinic.doctor.infrastructure.persistence.DoctorServiceJpaRepository;
import com.doctorri.clinic.notification.application.OutboxPublisher;
import com.doctorri.clinic.patient.infrastructure.persistence.PatientJpaRepository;
import com.doctorri.clinic.resource.application.ResourceBookingService;
import com.doctorri.clinic.resource.infrastructure.lock.SlotHoldService;
import com.doctorri.clinic.servicecatalog.infrastructure.persistence.ServiceEntity;
import com.doctorri.clinic.servicecatalog.infrastructure.persistence.ServiceJpaRepository;
import com.doctorri.clinic.shared.domain.exception.DomainException;
import com.doctorri.clinic.shared.infrastructure.config.BookingProperties;
import com.doctorri.clinic.shared.infrastructure.config.NotificationProperties;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentApplicationService {

  private static final ZoneOffset VN = ZoneOffset.ofHours(7);

  private final AppointmentJpaRepository appointments;
  private final AppointmentStatusHistoryJpaRepository history;
  private final ServiceJpaRepository services;
  private final DoctorJpaRepository doctors;
  private final DoctorServiceJpaRepository doctorServices;
  private final PatientJpaRepository patients;
  private final BookingPolicy bookingPolicy;
  private final SlotHoldService slotHoldService;
  private final ResourceBookingService resourceBookingService;
  private final OutboxPublisher outboxPublisher;
  private final IdempotencyService idempotencyService;
  private final NotificationProperties notificationProperties;
  private final Clock clock;

  public AppointmentApplicationService(
      AppointmentJpaRepository appointments,
      AppointmentStatusHistoryJpaRepository history,
      ServiceJpaRepository services,
      DoctorJpaRepository doctors,
      DoctorServiceJpaRepository doctorServices,
      PatientJpaRepository patients,
      BookingProperties props,
      Clock clock,
      SlotHoldService slotHoldService,
      ResourceBookingService resourceBookingService,
      OutboxPublisher outboxPublisher,
      IdempotencyService idempotencyService,
      NotificationProperties notificationProperties) {
    this.appointments = appointments;
    this.history = history;
    this.services = services;
    this.doctors = doctors;
    this.doctorServices = doctorServices;
    this.patients = patients;
    this.bookingPolicy = new BookingPolicy(props.windowDays(), props.freeCancelHours(), clock);
    this.slotHoldService = slotHoldService;
    this.resourceBookingService = resourceBookingService;
    this.outboxPublisher = outboxPublisher;
    this.idempotencyService = idempotencyService;
    this.notificationProperties = notificationProperties;
    this.clock = clock;
  }

  @Transactional
  public AppointmentResponse create(
      CreateAppointmentRequest request, Long actorUserId, String source, String idempotencyKey) {
    if (idempotencyKey != null && !idempotencyKey.isBlank()) {
      Long existingId = idempotencyService.findExisting(actorUserId, idempotencyKey).orElse(null);
      if (existingId != null) {
        return toResponse(require(existingId));
      }
      Long createdId = idempotencyService.executeOnce(
          actorUserId, idempotencyKey, () -> createInternal(request, actorUserId, source).id());
      return toResponse(require(createdId));
    }
    return createInternal(request, actorUserId, source);
  }

  @Transactional
  public AppointmentResponse create(CreateAppointmentRequest request, Long actorUserId, String source) {
    return create(request, actorUserId, source, null);
  }

  private AppointmentResponse createInternal(
      CreateAppointmentRequest request, Long actorUserId, String source) {
    bookingPolicy.assertWithinBookingWindow(request.startAt());
    patients.findById(request.patientId())
        .orElseThrow(() -> new DomainException("PATIENT_NOT_FOUND", "Không tìm thấy bệnh nhân."));
    ServiceEntity service = services.findById(request.serviceId())
        .orElseThrow(() -> new DomainException("SERVICE_NOT_FOUND", "Không tìm thấy dịch vụ."));
    if (!"ACTIVE".equals(service.getStatus())) {
      throw new DomainException("SERVICE_INACTIVE", "Dịch vụ không còn hoạt động.");
    }
    Long doctorId = resolveDoctorId(request.doctorId());
    if (!doctorServices.existsByDoctorIdAndServiceIdAndStatus(doctorId, service.getId(), "ACTIVE")) {
      throw new DomainException("DOCTOR_SERVICE_NOT_OFFERED", "Bác sĩ không cung cấp dịch vụ này.");
    }
    LocalDateTime start = request.startAt().withOffsetSameInstant(VN).toLocalDateTime();
    LocalDateTime end = start.plusMinutes(service.getDefaultDurationMinutes());

    slotHoldService.hold(doctorId, start, "user:" + actorUserId);
    try {
      assertDoctorFree(doctorId, start, end, null, service);
      AppointmentEntity entity = new AppointmentEntity();
      entity.setAppointmentCode("AP" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
      entity.setPatientId(request.patientId());
      entity.setClinicId(1L);
      entity.setDoctorId(doctorId);
      entity.setServiceId(service.getId());
      entity.setScheduledStartAt(start);
      entity.setScheduledEndAt(end);
      entity.setStatus(AppointmentStatus.CONFIRMED);
      entity.setSource(source == null ? "ONLINE" : source);
      entity.setReason(request.reason());
      entity.setCreatedBy(actorUserId);
      appointments.save(entity);
      resourceBookingService.reserveForAppointment(entity.getId(), doctorId, service, start, end);
      recordHistory(entity.getId(), null, AppointmentStatus.CONFIRMED.name(), actorUserId, "created");
      Map<String, Object> payload = basePayload(entity);
      outboxPublisher.publish("APPOINTMENT_CREATED", entity.getId(), payload);
      scheduleReminders(entity, payload);
      return toResponse(entity);
    } catch (RuntimeException ex) {
      slotHoldService.release(doctorId, start);
      throw ex;
    }
  }

  @Transactional
  public AppointmentResponse reschedule(Long id, RescheduleAppointmentRequest request, Long actorUserId) {
    AppointmentEntity entity = require(id);
    if (entity.getStatus() == AppointmentStatus.CANCELLED || entity.getStatus() == AppointmentStatus.COMPLETED) {
      throw new DomainException("INVALID_STATUS", "Không thể đổi lịch ở trạng thái hiện tại.");
    }
    bookingPolicy.assertWithinBookingWindow(request.startAt());
    ServiceEntity service = services.findById(entity.getServiceId()).orElseThrow();
    Long doctorId = request.doctorId() != null ? request.doctorId() : entity.getDoctorId();
    LocalDateTime start = request.startAt().withOffsetSameInstant(VN).toLocalDateTime();
    LocalDateTime end = start.plusMinutes(service.getDefaultDurationMinutes());
    slotHoldService.hold(doctorId, start, "reschedule:" + id);
    try {
      assertDoctorFree(doctorId, start, end, id, service);
      resourceBookingService.releaseAppointment(id);
      outboxPublisher.cancelPendingReminders(id);
      LocalDateTime oldStart = entity.getScheduledStartAt();
      Long oldDoctor = entity.getDoctorId();
      entity.setDoctorId(doctorId);
      entity.setScheduledStartAt(start);
      entity.setScheduledEndAt(end);
      entity.setOperationalStatus(null);
      entity.touch();
      resourceBookingService.reserveForAppointment(id, doctorId, service, start, end);
      slotHoldService.release(oldDoctor, oldStart);
      recordHistory(id, entity.getStatus().name(), entity.getStatus().name(), actorUserId, "rescheduled");
      Map<String, Object> payload = basePayload(entity);
      outboxPublisher.publish("APPOINTMENT_RESCHEDULED", id, payload);
      scheduleReminders(entity, payload);
      return toResponse(entity);
    } catch (RuntimeException ex) {
      slotHoldService.release(doctorId, start);
      throw ex;
    }
  }

  @Transactional(readOnly = true)
  public List<AppointmentResponse> listMine(Long patientId) {
    return appointments.findByPatientIdOrderByScheduledStartAtDesc(patientId).stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public AppointmentResponse get(Long id) {
    return toResponse(require(id));
  }

  @Transactional
  public AppointmentResponse confirmAttendance(Long id, Long actorUserId) {
    AppointmentEntity entity = require(id);
    if (entity.getStatus() != AppointmentStatus.CONFIRMED) {
      throw new DomainException("INVALID_STATUS", "Chỉ xác nhận được lịch đã CONFIRMED.");
    }
    String old = entity.getStatus().name();
    entity.setStatus(AppointmentStatus.ATTENDANCE_CONFIRMED);
    entity.touch();
    recordHistory(id, old, entity.getStatus().name(), actorUserId, "attendance");
    outboxPublisher.publish("APPOINTMENT_ATTENDANCE_CONFIRMED", id, basePayload(entity));
    return toResponse(entity);
  }

  @Transactional
  public AppointmentResponse cancel(Long id, Long actorUserId) {
    AppointmentEntity entity = require(id);
    if (entity.getStatus() == AppointmentStatus.CANCELLED || entity.getStatus() == AppointmentStatus.COMPLETED) {
      throw new DomainException("INVALID_STATUS", "Không thể hủy lịch ở trạng thái hiện tại.");
    }
    String old = entity.getStatus().name();
    entity.setLateCancel(bookingPolicy.isLateCancel(entity.getScheduledStartAt().atOffset(VN)));
    entity.setStatus(AppointmentStatus.CANCELLED);
    entity.touch();
    resourceBookingService.releaseAppointment(id);
    slotHoldService.release(entity.getDoctorId(), entity.getScheduledStartAt());
    outboxPublisher.cancelPendingReminders(id);
    recordHistory(id, old, AppointmentStatus.CANCELLED.name(), actorUserId, "cancelled");
    outboxPublisher.publish("APPOINTMENT_CANCELLED", id, basePayload(entity));
    return toResponse(entity);
  }

  @Transactional
  public AppointmentResponse markNoShow(Long id, Long actorUserId) {
    AppointmentEntity entity = require(id);
    if (entity.getStatus() == AppointmentStatus.CANCELLED || entity.getStatus() == AppointmentStatus.COMPLETED) {
      throw new DomainException("INVALID_STATUS", "Không thể đánh dấu no-show ở trạng thái hiện tại.");
    }
    String old = entity.getStatus().name();
    entity.setStatus(AppointmentStatus.NO_SHOW);
    entity.touch();
    resourceBookingService.releaseAppointment(id);
    outboxPublisher.cancelPendingReminders(id);
    recordHistory(id, old, AppointmentStatus.NO_SHOW.name(), actorUserId, "no-show");
    return toResponse(entity);
  }

  private void scheduleReminders(AppointmentEntity entity, Map<String, Object> payload) {
    Instant start = entity.getScheduledStartAt().toInstant(VN);
    Instant now = clock.instant();
    for (Integer hours : notificationProperties.reminderHourList()) {
      Instant availableAt = start.minusSeconds(hours * 3600L);
      if (!availableAt.isAfter(now)) {
        continue;
      }
      String type = switch (hours) {
        case 24 -> "APPOINTMENT_REMINDER_24H";
        case 2 -> "APPOINTMENT_REMINDER_2H";
        default -> "APPOINTMENT_REMINDER_" + hours + "H";
      };
      outboxPublisher.publishAt(type, entity.getId(), payload, availableAt);
    }
  }

  private Map<String, Object> basePayload(AppointmentEntity entity) {
    return Map.of(
        "appointmentId", entity.getId(),
        "patientId", entity.getPatientId(),
        "code", entity.getAppointmentCode(),
        "startAt", entity.getScheduledStartAt().atOffset(VN).toString());
  }

  private void assertDoctorFree(
      Long doctorId, LocalDateTime start, LocalDateTime end, Long excludeId, ServiceEntity service) {
    LocalDateTime windowStart = start.minusMinutes(service.getBufferBeforeMinutes());
    LocalDateTime windowEnd = end.plusMinutes(service.getBufferAfterMinutes());
    boolean busy = appointments.findOverlaps(doctorId, windowStart, windowEnd,
            EnumSet.of(AppointmentStatus.HELD, AppointmentStatus.CONFIRMED, AppointmentStatus.ATTENDANCE_CONFIRMED))
        .stream()
        .anyMatch(a -> excludeId == null || !a.getId().equals(excludeId));
    if (busy) {
      throw new DomainException("SLOT_NOT_AVAILABLE", "Khung giờ vừa được người khác đặt.");
    }
  }

  private Long resolveDoctorId(Long doctorId) {
    if (doctorId != null) {
      doctors.findById(doctorId).orElseThrow(() -> new DomainException("DOCTOR_NOT_FOUND", "Không tìm thấy bác sĩ."));
      return doctorId;
    }
    return doctors.findByStatusOrderByFullNameAsc("ACTIVE").stream().findFirst()
        .orElseThrow(() -> new DomainException("DOCTOR_NOT_FOUND", "Chưa có bác sĩ khả dụng.")).getId();
  }

  private AppointmentEntity require(Long id) {
    return appointments.findById(id)
        .orElseThrow(() -> new DomainException("APPOINTMENT_NOT_FOUND", "Không tìm thấy lịch hẹn."));
  }

  private void recordHistory(Long appointmentId, String oldStatus, String newStatus, Long by, String reason) {
    AppointmentStatusHistoryEntity h = new AppointmentStatusHistoryEntity();
    h.setAppointmentId(appointmentId);
    h.setOldStatus(oldStatus);
    h.setNewStatus(newStatus);
    h.setChangedBy(by);
    h.setReason(reason);
    history.save(h);
  }

  private AppointmentResponse toResponse(AppointmentEntity e) {
    return new AppointmentResponse(
        e.getId(), e.getAppointmentCode(), e.getPatientId(), e.getDoctorId(), e.getServiceId(),
        e.getScheduledStartAt().atOffset(VN), e.getScheduledEndAt().atOffset(VN),
        e.getStatus(), e.getReason(), e.isLateCancel());
  }
}
