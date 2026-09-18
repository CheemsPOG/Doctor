package com.doctorri.clinic.queue.presentation.rest;

import com.doctorri.clinic.clinic.application.ClinicOperationsService;
import com.doctorri.clinic.shared.infrastructure.security.SecurityUtils;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/queue")
public class QueueController {

  private final ClinicOperationsService ops;

  public QueueController(ClinicOperationsService ops) {
    this.ops = ops;
  }

  @PostMapping("/{id}/call")
  public Map<String, Object> call(@PathVariable Long id) {
    var q = ops.transition(id, "CALLED", SecurityUtils.requireUser().getUserId());
    return Map.of("id", q.getId(), "status", q.getStatus());
  }

  @PostMapping("/{id}/start")
  public Map<String, Object> start(@PathVariable Long id) {
    var q = ops.transition(id, "IN_SERVICE", SecurityUtils.requireUser().getUserId());
    return Map.of("id", q.getId(), "status", q.getStatus());
  }

  @PostMapping("/{id}/complete")
  public Map<String, Object> complete(@PathVariable Long id) {
    var q = ops.transition(id, "COMPLETED", SecurityUtils.requireUser().getUserId());
    return Map.of("id", q.getId(), "status", q.getStatus());
  }

  @PostMapping("/{id}/reprioritize")
  public Map<String, Object> reprioritize(@PathVariable Long id, @Valid @RequestBody ReprioritizeRequest body) {
    var user = SecurityUtils.requireUser();
    var q = ops.reprioritize(id, body.priority(), body.reason(), user.getUserId());
    return Map.of("id", q.getId(), "priority", q.getPriority());
  }

  public record ReprioritizeRequest(int priority, @jakarta.validation.constraints.NotBlank String reason) {}
}
