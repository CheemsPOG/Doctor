package com.doctorri.clinic.encounter.application;

import com.doctorri.clinic.appointment.domain.model.AppointmentStatus;
import com.doctorri.clinic.appointment.infrastructure.persistence.AppointmentEntity;
import com.doctorri.clinic.appointment.infrastructure.persistence.AppointmentJpaRepository;
import com.doctorri.clinic.audit.application.AuditService;
import com.doctorri.clinic.encounter.infrastructure.persistence.EncounterActivityEntity;
import com.doctorri.clinic.encounter.infrastructure.persistence.EncounterActivityJpaRepository;
import com.doctorri.clinic.encounter.infrastructure.persistence.EncounterEntity;
import com.doctorri.clinic.encounter.infrastructure.persistence.EncounterJpaRepository;
import com.doctorri.clinic.queue.infrastructure.persistence.VisitQueueEntity;
import com.doctorri.clinic.queue.infrastructure.persistence.VisitQueueJpaRepository;
import com.doctorri.clinic.shared.domain.exception.DomainException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EncounterService {
  private final AppointmentJpaRepository appointments;
  private final EncounterJpaRepository encounters;
  private final EncounterActivityJpaRepository activities;
  private final VisitQueueJpaRepository queues;
  private final AuditService audit;

  public EncounterService(
      AppointmentJpaRepository appointments,
      EncounterJpaRepository encounters,
      EncounterActivityJpaRepository activities,
      VisitQueueJpaRepository queues,
      AuditService audit) {
    this.appointments = appointments;
    this.encounters = encounters;
    this.activities = activities;
    this.queues = queues;
    this.audit = audit;
  }

  @Transactional(readOnly = true)
  public List<AppointmentEntity> doctorSchedule(Long doctorId, LocalDate date) {
    LocalDateTime from = date.atStartOfDay();
    LocalDateTime to = date.plusDays(1).atStartOfDay();
    return appointments.findOverlaps(doctorId, from, to,
        java.util.EnumSet.of(AppointmentStatus.CONFIRMED, AppointmentStatus.ATTENDANCE_CONFIRMED,
            AppointmentStatus.HELD));
  }

  @Transactional(readOnly = true)
  public List<VisitQueueEntity> doctorQueue(Long doctorId) {
    LocalDate today = LocalDate.now();
    return queues.findByDay(today.atStartOfDay(), today.plusDays(1).atStartOfDay()).stream()
        .filter(q -> appointments.findById(q.getAppointmentId())
            .map(a -> a.getDoctorId().equals(doctorId))
            .orElse(false))
        .toList();
  }

  @Transactional
  public EncounterEntity startExam(Long appointmentId, Long doctorId, Long actorUserId) {
    AppointmentEntity appt = appointments.findById(appointmentId)
        .orElseThrow(() -> new DomainException("APPOINTMENT_NOT_FOUND", "Không tìm thấy lịch."));
    if (!appt.getDoctorId().equals(doctorId)) {
      throw new DomainException("FORBIDDEN", "Không phải lịch của bác sĩ này.");
    }
    if (encounters.findByAppointmentId(appointmentId).isPresent()) {
      throw new DomainException("EXAM_STARTED", "Đã bắt đầu khám.");
    }
    EncounterEntity e = new EncounterEntity();
    e.setAppointmentId(appointmentId);
    e.setPatientId(appt.getPatientId());
    e.setDoctorId(doctorId);
    e.setStartedAt(LocalDateTime.now());
    encounters.save(e);
    queues.findByAppointmentId(appointmentId).ifPresent(q -> {
      q.setStatus("IN_SERVICE");
      q.setStartedAt(LocalDateTime.now());
    });
    return e;
  }

  @Transactional
  public EncounterEntity completeExam(Long appointmentId, String summary, Long doctorId, Long actorUserId) {
    EncounterEntity e = encounters.findByAppointmentId(appointmentId)
        .orElseThrow(() -> new DomainException("ENCOUNTER_NOT_FOUND", "Chưa bắt đầu khám."));
    if (!e.getDoctorId().equals(doctorId)) {
      throw new DomainException("FORBIDDEN", "Không phải lịch của bác sĩ này.");
    }
    e.setSummary(summary);
    e.setEndedAt(LocalDateTime.now());
    AppointmentEntity appt = appointments.findById(appointmentId).orElseThrow();
    appt.setStatus(AppointmentStatus.COMPLETED);
    appt.touch();
    queues.findByAppointmentId(appointmentId).ifPresent(q -> {
      q.setStatus("COMPLETED");
      q.setCompletedAt(LocalDateTime.now());
    });
    audit.log(actorUserId, "COMPLETE_EXAM", "ENCOUNTER", e.getId(), summary);
    return e;
  }

  @Transactional
  public EncounterActivityEntity addActivity(Long appointmentId, String type, String note, Long actorUserId) {
    EncounterEntity e = encounters.findByAppointmentId(appointmentId)
        .orElseThrow(() -> new DomainException("ENCOUNTER_NOT_FOUND", "Chưa bắt đầu khám."));
    EncounterActivityEntity a = new EncounterActivityEntity();
    a.setEncounterId(e.getId());
    a.setActivityType(type);
    a.setNote(note);
    a.setCreatedBy(actorUserId);
    return activities.save(a);
  }

  @Transactional(readOnly = true)
  public List<EncounterActivityEntity> listActivities(Long appointmentId) {
    EncounterEntity e = encounters.findByAppointmentId(appointmentId)
        .orElseThrow(() -> new DomainException("ENCOUNTER_NOT_FOUND", "Chưa bắt đầu khám."));
    return activities.findByEncounterIdOrderByCreatedAtAsc(e.getId());
  }
}
