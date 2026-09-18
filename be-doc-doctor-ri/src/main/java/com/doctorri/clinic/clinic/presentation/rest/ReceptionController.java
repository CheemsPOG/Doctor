package com.doctorri.clinic.clinic.presentation.rest;

import com.doctorri.clinic.appointment.application.dto.AppointmentResponse;
import com.doctorri.clinic.appointment.application.dto.CreateAppointmentRequest;
import com.doctorri.clinic.appointment.application.usecase.AppointmentApplicationService;
import com.doctorri.clinic.clinic.application.ClinicOperationsService;
import com.doctorri.clinic.queue.infrastructure.persistence.VisitQueueEntity;
import com.doctorri.clinic.resource.application.ResourceBookingService;
import com.doctorri.clinic.shared.infrastructure.security.SecurityUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ReceptionController {
  private final ClinicOperationsService ops;
  private final AppointmentApplicationService appointments;
  private final ResourceBookingService resources;

  public ReceptionController(
      ClinicOperationsService ops,
      AppointmentApplicationService appointments,
      ResourceBookingService resources) {
    this.ops = ops;
    this.appointments = appointments;
    this.resources = resources;
  }

  @PostMapping("/reception/appointments")
  @ResponseStatus(HttpStatus.CREATED)
  public AppointmentResponse create(
      @Valid @RequestBody CreateAppointmentRequest request,
      @RequestParam(defaultValue = "false") boolean walkIn) {
    var user = SecurityUtils.requireUser();
    return ops.createReception(request, user.getUserId(), walkIn);
  }

  @PostMapping("/appointments/{id}/check-in")
  public Map<String, Object> checkIn(
      @PathVariable Long id,
      @RequestBody(required = false) CheckInRequest body) {
    var user = SecurityUtils.requireUser();
    boolean urgent = body != null && body.medicalUrgent();
    VisitQueueEntity q = ops.checkIn(id, user.getUserId(), urgent);
    return Map.of(
        "queueId", q.getId(),
        "queueNumber", q.getQueueNumber(),
        "status", q.getStatus(),
        "priority", q.getPriority());
  }

  @PostMapping("/appointments/{id}/assign-room")
  public Map<String, Object> assignRoom(@PathVariable Long id, @RequestBody AssignRoomRequest body) {
    var user = SecurityUtils.requireUser();
    VisitQueueEntity q = ops.assignRoom(id, body.roomId(), user.getUserId());
    return Map.of("queueId", q.getId(), "assignedRoomId", q.getAssignedRoomId());
  }

  @PostMapping("/appointments/{id}/mark-no-show")
  public AppointmentResponse noShow(@PathVariable Long id) {
    var user = SecurityUtils.requireUser();
    return appointments.markNoShow(id, user.getUserId());
  }

  @GetMapping("/clinic/queue")
  public List<Map<String, Object>> queue(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return ops.queueByDate(date).stream()
        .map(q -> Map.<String, Object>of(
            "id", q.getId(),
            "appointmentId", q.getAppointmentId(),
            "queueNumber", q.getQueueNumber(),
            "priority", q.getPriority(),
            "status", q.getStatus(),
            "assignedRoomId", q.getAssignedRoomId() == null ? 0 : q.getAssignedRoomId()))
        .toList();
  }

  /** Scheduled appointments for the day (includes not-yet-checked-in). */
  @GetMapping("/clinic/appointments")
  public List<Map<String, Object>> appointments(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    // With JVM TZ Asia/Ho_Chi_Minh, LocalDateTime is clinic wall-clock — attach +07.
    ZoneOffset vn = ZoneOffset.ofHours(7);
    return ops.appointmentsByDate(date).stream()
        .map(a -> {
          Map<String, Object> row = new HashMap<>();
          row.put("id", a.getId());
          row.put("appointmentCode", a.getAppointmentCode() == null ? "" : a.getAppointmentCode());
          row.put("patientId", a.getPatientId());
          row.put("doctorId", a.getDoctorId());
          row.put("serviceId", a.getServiceId());
          row.put("startAt", a.getScheduledStartAt().atOffset(vn).toString());
          row.put("endAt", a.getScheduledEndAt().atOffset(vn).toString());
          row.put("status", a.getStatus().name());
          row.put("reason", a.getReason() == null ? "" : a.getReason());
          row.put("checkedIn", ops.isCheckedIn(a.getId()));
          row.put("source", a.getSource() == null ? "" : a.getSource());
          return row;
        })
        .toList();
  }

  /** Rooms for reception ops (assign-room). Admin catalog stays under /admin/rooms. */
  @GetMapping("/clinic/rooms")
  public List<Map<String, Object>> rooms() {
    return resources.listRooms().stream()
        .map(r -> Map.<String, Object>of(
            "id", r.getId(),
            "code", r.getCode(),
            "name", r.getName(),
            "roomType", r.getRoomType()))
        .toList();
  }

  public record AssignRoomRequest(@NotNull Long roomId) {}

  public record CheckInRequest(boolean medicalUrgent) {}
}
