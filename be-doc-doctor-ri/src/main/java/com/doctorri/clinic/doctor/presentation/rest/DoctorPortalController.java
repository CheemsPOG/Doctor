package com.doctorri.clinic.doctor.presentation.rest;

import com.doctorri.clinic.appointment.infrastructure.persistence.AppointmentEntity;
import com.doctorri.clinic.encounter.application.EncounterService;
import com.doctorri.clinic.encounter.infrastructure.persistence.EncounterActivityEntity;
import com.doctorri.clinic.encounter.infrastructure.persistence.EncounterEntity;
import com.doctorri.clinic.queue.infrastructure.persistence.VisitQueueEntity;
import com.doctorri.clinic.shared.domain.exception.DomainException;
import com.doctorri.clinic.shared.infrastructure.security.SecurityUtils;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class DoctorPortalController {
  private final EncounterService encounters;
  public DoctorPortalController(EncounterService encounters) { this.encounters = encounters; }

  private Long doctorId() {
    Long id = SecurityUtils.requireUser().getDoctorId();
    if (id == null) throw new DomainException("FORBIDDEN", "Tài khoản không gắn bác sĩ.");
    return id;
  }

  @GetMapping("/doctor/me/schedule")
  public List<Map<String, Object>> schedule(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    LocalDate d = date == null ? LocalDate.now() : date;
    return encounters.doctorSchedule(doctorId(), d).stream().map(this::apptMap).toList();
  }

  @GetMapping("/doctor/me/queue")
  public List<Map<String, Object>> queue() {
    return encounters.doctorQueue(doctorId()).stream()
        .map(q -> Map.<String, Object>of(
            "id", q.getId(), "appointmentId", q.getAppointmentId(),
            "queueNumber", q.getQueueNumber(), "status", q.getStatus(), "priority", q.getPriority()))
        .toList();
  }

  @PostMapping("/appointments/{id}/start-exam")
  public Map<String, Object> start(@PathVariable Long id) {
    var user = SecurityUtils.requireUser();
    EncounterEntity e = encounters.startExam(id, doctorId(), user.getUserId());
    return Map.of("encounterId", e.getId(), "startedAt", e.getStartedAt().toString());
  }

  @PostMapping("/appointments/{id}/complete-exam")
  public Map<String, Object> complete(@PathVariable Long id, @RequestBody CompleteExamRequest body) {
    var user = SecurityUtils.requireUser();
    EncounterEntity e = encounters.completeExam(id, body.summary(), doctorId(), user.getUserId());
    return Map.of("encounterId", e.getId(), "endedAt", String.valueOf(e.getEndedAt()));
  }

  @PostMapping("/appointments/{id}/activities")
  public Map<String, Object> activity(@PathVariable Long id, @RequestBody ActivityRequest body) {
    var user = SecurityUtils.requireUser();
    EncounterActivityEntity a = encounters.addActivity(id, body.activityType(), body.note(), user.getUserId());
    return Map.of("id", a.getId(), "activityType", a.getActivityType());
  }

  @GetMapping("/appointments/{id}/activities")
  public List<Map<String, Object>> activities(@PathVariable Long id) {
    return encounters.listActivities(id).stream()
        .map(a -> Map.<String, Object>of("id", a.getId(), "activityType", a.getActivityType(), "note", a.getNote() == null ? "" : a.getNote()))
        .toList();
  }

  private Map<String, Object> apptMap(AppointmentEntity a) {
    ZoneOffset vn = ZoneOffset.ofHours(7);
    return Map.of(
        "id", a.getId(),
        "appointmentCode", a.getAppointmentCode(),
        "patientId", a.getPatientId(),
        "startAt", a.getScheduledStartAt().atOffset(vn).toString(),
        "endAt", a.getScheduledEndAt().atOffset(vn).toString(),
        "status", a.getStatus().name());
  }

  public record CompleteExamRequest(String summary) {}
  public record ActivityRequest(String activityType, String note) {}
}
