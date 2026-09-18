package com.doctorri.clinic.audit.presentation.rest;

import com.doctorri.clinic.audit.infrastructure.persistence.AuditLogEntity;
import com.doctorri.clinic.audit.infrastructure.persistence.AuditLogJpaRepository;
import com.doctorri.clinic.resource.application.ResourceBookingService;
import com.doctorri.clinic.resource.infrastructure.persistence.EquipmentEntity;
import com.doctorri.clinic.resource.infrastructure.persistence.RoomEntity;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

  private final ResourceBookingService resources;
  private final AuditLogJpaRepository auditLogs;

  public AdminController(ResourceBookingService resources, AuditLogJpaRepository auditLogs) {
    this.resources = resources;
    this.auditLogs = auditLogs;
  }

  @GetMapping("/rooms")
  public List<Map<String, Object>> rooms() {
    return resources.listRooms().stream()
        .map(r -> Map.<String, Object>of(
            "id", r.getId(), "code", r.getCode(), "name", r.getName(), "roomType", r.getRoomType()))
        .toList();
  }

  @GetMapping("/equipments")
  public List<Map<String, Object>> equipments() {
    return resources.listEquipments().stream()
        .map(e -> Map.<String, Object>of(
            "id", e.getId(), "code", e.getCode(), "name", e.getName(), "equipmentType", e.getEquipmentType()))
        .toList();
  }

  @GetMapping("/audit-logs")
  public List<AuditLogEntity> auditLogs() {
    return auditLogs.findTop100ByOrderByCreatedAtDesc();
  }
}
