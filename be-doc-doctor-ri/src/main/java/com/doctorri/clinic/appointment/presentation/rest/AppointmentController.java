package com.doctorri.clinic.appointment.presentation.rest;

import com.doctorri.clinic.appointment.application.dto.AppointmentResponse;
import com.doctorri.clinic.appointment.application.dto.CreateAppointmentRequest;
import com.doctorri.clinic.appointment.application.dto.RescheduleAppointmentRequest;
import com.doctorri.clinic.appointment.application.usecase.AppointmentApplicationService;
import com.doctorri.clinic.audit.application.AuditService;
import com.doctorri.clinic.shared.infrastructure.security.SecurityUtils;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class AppointmentController {

  private final AppointmentApplicationService appointments;
  private final AuditService audit;

  public AppointmentController(AppointmentApplicationService appointments, AuditService audit) {
    this.appointments = appointments;
    this.audit = audit;
  }

  @PostMapping("/appointments")
  @ResponseStatus(HttpStatus.CREATED)
  public AppointmentResponse create(
      @Valid @RequestBody CreateAppointmentRequest request,
      @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
    var user = SecurityUtils.requireUser();
    Long patientId = request.patientId() != null ? request.patientId() : user.getPatientId();
    var req = new CreateAppointmentRequest(
        patientId, request.serviceId(), request.doctorId(), request.startAt(), request.reason());
    return appointments.create(req, user.getUserId(), "ONLINE", idempotencyKey);
  }

  @GetMapping("/appointments/{id}")
  public AppointmentResponse get(@PathVariable Long id) {
    return appointments.get(id);
  }

  @GetMapping("/me/appointments")
  public List<AppointmentResponse> mine(@RequestParam(required = false) Long patientId) {
    var user = SecurityUtils.requireUser();
    Long pid = patientId != null ? patientId : user.getPatientId();
    return appointments.listMine(pid);
  }

  @PostMapping("/appointments/{id}/confirm")
  public AppointmentResponse confirm(@PathVariable Long id) {
    var user = SecurityUtils.requireUser();
    return appointments.confirmAttendance(id, user.getUserId());
  }

  @PostMapping("/appointments/{id}/cancel")
  public AppointmentResponse cancel(@PathVariable Long id) {
    var user = SecurityUtils.requireUser();
    var res = appointments.cancel(id, user.getUserId());
    audit.log(user.getUserId(), "CANCEL_APPOINTMENT", "APPOINTMENT", id, null);
    return res;
  }

  @PostMapping("/appointments/{id}/reschedule")
  public AppointmentResponse reschedule(
      @PathVariable Long id, @Valid @RequestBody RescheduleAppointmentRequest request) {
    var user = SecurityUtils.requireUser();
    return appointments.reschedule(id, request, user.getUserId());
  }
}
