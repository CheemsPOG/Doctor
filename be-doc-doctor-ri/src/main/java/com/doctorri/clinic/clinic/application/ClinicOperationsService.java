package com.doctorri.clinic.clinic.application;

import com.doctorri.clinic.appointment.application.dto.AppointmentResponse;
import com.doctorri.clinic.appointment.application.dto.CreateAppointmentRequest;
import com.doctorri.clinic.appointment.application.usecase.AppointmentApplicationService;
import com.doctorri.clinic.appointment.domain.model.AppointmentStatus;
import com.doctorri.clinic.appointment.infrastructure.persistence.AppointmentEntity;
import com.doctorri.clinic.appointment.infrastructure.persistence.AppointmentJpaRepository;
import com.doctorri.clinic.audit.application.AuditService;
import com.doctorri.clinic.queue.domain.service.QueuePriorityPolicy;
import com.doctorri.clinic.queue.domain.service.QueueStatusMachine;
import com.doctorri.clinic.queue.infrastructure.persistence.VisitQueueEntity;
import com.doctorri.clinic.queue.infrastructure.persistence.VisitQueueJpaRepository;
import com.doctorri.clinic.resource.infrastructure.persistence.RoomJpaRepository;
import com.doctorri.clinic.shared.domain.exception.DomainException;
import com.doctorri.clinic.shared.infrastructure.config.BookingProperties;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClinicOperationsService {
  private static final Set<AppointmentStatus> CHECK_IN_ALLOWED = EnumSet.of(
      AppointmentStatus.CONFIRMED, AppointmentStatus.ATTENDANCE_CONFIRMED);

  private final AppointmentApplicationService appointments;
  private final AppointmentJpaRepository appointmentRepo;
  private final VisitQueueJpaRepository queues;
  private final RoomJpaRepository rooms;
  private final AuditService audit;
  private final BookingProperties bookingProperties;

  public ClinicOperationsService(
      AppointmentApplicationService appointments,
      AppointmentJpaRepository appointmentRepo,
      VisitQueueJpaRepository queues,
      RoomJpaRepository rooms,
      AuditService audit,
      BookingProperties bookingProperties) {
    this.appointments = appointments;
    this.appointmentRepo = appointmentRepo;
    this.queues = queues;
    this.rooms = rooms;
    this.audit = audit;
    this.bookingProperties = bookingProperties;
  }

  @Transactional
  public AppointmentResponse createReception(
      CreateAppointmentRequest request, Long actorUserId, boolean walkIn) {
    String source = walkIn ? "WALK_IN" : "RECEPTION";
    return appointments.create(request, actorUserId, source);
  }

  @Transactional
  public VisitQueueEntity checkIn(Long appointmentId, Long actorUserId, boolean medicalUrgent) {
    AppointmentEntity appt = appointmentRepo.findById(appointmentId)
        .orElseThrow(() -> new DomainException("APPOINTMENT_NOT_FOUND", "Không tìm thấy lịch hẹn."));
    if (!CHECK_IN_ALLOWED.contains(appt.getStatus())) {
      throw new DomainException("INVALID_STATUS", "Chỉ check-in được lịch CONFIRMED / ATTENDANCE_CONFIRMED.");
    }
    if (queues.findByAppointmentId(appointmentId).isPresent()) {
      throw new DomainException("ALREADY_CHECKED_IN", "Đã check-in.");
    }
    LocalDateTime now = LocalDateTime.now();
    LocalDate today = now.toLocalDate();
    LocalDateTime from = today.atStartOfDay();
    LocalDateTime to = today.plusDays(1).atStartOfDay();
    int next = queues.maxQueueNumber(from, to) + 1;
    int priority = QueuePriorityPolicy.compute(
        appt.getSource(),
        appt.getScheduledStartAt(),
        now,
        bookingProperties.lateGraceMinutes(),
        medicalUrgent);

    VisitQueueEntity q = new VisitQueueEntity();
    q.setAppointmentId(appointmentId);
    q.setQueueNumber(next);
    q.setPriority(priority);
    q.setStatus("WAITING");
    q.setCheckedInAt(now);
    queues.save(q);
    appt.setOperationalStatus("CHECKED_IN");
    appt.touch();
    audit.log(actorUserId, "CHECK_IN", "APPOINTMENT", appointmentId,
        "queue#" + next + " priority=" + priority + (medicalUrgent ? " urgent" : ""));
    return q;
  }

  @Transactional
  public VisitQueueEntity assignRoom(Long appointmentId, Long roomId, Long actorUserId) {
    VisitQueueEntity q = queues.findByAppointmentId(appointmentId)
        .orElseThrow(() -> new DomainException("NOT_CHECKED_IN", "Chưa check-in."));
    rooms.findByIdAndStatus(roomId, "ACTIVE")
        .orElseThrow(() -> new DomainException("ROOM_NOT_FOUND", "Không tìm thấy phòng."));
    q.setAssignedRoomId(roomId);
    appointmentRepo.findById(appointmentId).ifPresent(a -> {
      a.setOperationalStatus("ROOM_ASSIGNED");
      a.touch();
    });
    audit.log(actorUserId, "ASSIGN_ROOM", "VISIT_QUEUE", q.getId(), "room=" + roomId);
    return q;
  }

  @Transactional(readOnly = true)
  public List<VisitQueueEntity> queueByDate(LocalDate date) {
    return queues.findByDay(date.atStartOfDay(), date.plusDays(1).atStartOfDay());
  }

  /** All appointments scheduled on the clinic day (not only checked-in queue). */
  @Transactional(readOnly = true)
  public List<AppointmentEntity> appointmentsByDate(LocalDate date) {
    return appointmentRepo.findByScheduledStartAtBetweenOrderByScheduledStartAtAsc(
        date.atStartOfDay(), date.plusDays(1).atStartOfDay());
  }

  @Transactional(readOnly = true)
  public boolean isCheckedIn(Long appointmentId) {
    return queues.findByAppointmentId(appointmentId).isPresent();
  }

  @Transactional
  public VisitQueueEntity transition(Long queueId, String toStatus, Long actorUserId) {
    VisitQueueEntity q = queues.findById(queueId)
        .orElseThrow(() -> new DomainException("QUEUE_NOT_FOUND", "Không tìm thấy hàng đợi."));
    QueueStatusMachine.assertTransition(q.getStatus(), toStatus);
    q.setStatus(toStatus);
    LocalDateTime now = LocalDateTime.now();
    if ("CALLED".equals(toStatus)) q.setCalledAt(now);
    if ("IN_SERVICE".equals(toStatus)) q.setStartedAt(now);
    if ("COMPLETED".equals(toStatus)) q.setCompletedAt(now);
    return q;
  }

  @Transactional
  public VisitQueueEntity reprioritize(Long queueId, int priority, String reason, Long actorUserId) {
    if (reason == null || reason.isBlank()) {
      throw new DomainException("REASON_REQUIRED", "Đổi ưu tiên bắt buộc có lý do.");
    }
    VisitQueueEntity q = queues.findById(queueId)
        .orElseThrow(() -> new DomainException("QUEUE_NOT_FOUND", "Không tìm thấy hàng đợi."));
    int old = q.getPriority();
    q.setPriority(priority);
    audit.log(actorUserId, "REPRIORITIZE", "VISIT_QUEUE", queueId,
        "from=" + old + " to=" + priority + " reason=" + reason.trim());
    return q;
  }
}
