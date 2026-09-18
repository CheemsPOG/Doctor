package com.doctorri.clinic.admin.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.doctorri.clinic.admin.application.dto.UpsertDoctorScheduleRequest;
import com.doctorri.clinic.admin.application.dto.UpsertServiceRequest;
import com.doctorri.clinic.audit.application.AuditService;
import com.doctorri.clinic.doctor.infrastructure.persistence.DoctorEntity;
import com.doctorri.clinic.doctor.infrastructure.persistence.DoctorJpaRepository;
import com.doctorri.clinic.doctor.infrastructure.persistence.DoctorScheduleEntity;
import com.doctorri.clinic.doctor.infrastructure.persistence.DoctorScheduleJpaRepository;
import com.doctorri.clinic.doctor.infrastructure.persistence.DoctorServiceJpaRepository;
import com.doctorri.clinic.servicecatalog.infrastructure.persistence.ServiceEntity;
import com.doctorri.clinic.servicecatalog.infrastructure.persistence.ServiceJpaRepository;
import com.doctorri.clinic.shared.domain.exception.DomainException;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminCatalogServiceTest {

  @Mock ServiceJpaRepository services;
  @Mock DoctorJpaRepository doctors;
  @Mock DoctorScheduleJpaRepository schedules;
  @Mock DoctorServiceJpaRepository doctorServices;
  @Mock AuditService audit;

  AdminCatalogService catalog;

  @BeforeEach
  void setUp() {
    catalog = new AdminCatalogService(services, doctors, schedules, doctorServices, audit);
  }

  @Test
  void createServicePersistsNormalizedCode() {
    when(services.findByCode("OB_NEW")).thenReturn(Optional.empty());
    when(services.save(any(ServiceEntity.class))).thenAnswer(inv -> {
      ServiceEntity s = inv.getArgument(0);
      setId(s, 99L);
      return s;
    });

    var req = new UpsertServiceRequest(
        "ob_new", "Khám mới", "obstetric", "desc", 30, 5, 5, false, false, null, "ACTIVE");
    var res = catalog.createService(req, 1L);

    assertEquals("OB_NEW", res.code());
    assertEquals(99L, res.id());
    verify(audit).log(eq(1L), eq("CREATE_SERVICE"), eq("SERVICE"), eq(99L), eq("OB_NEW"));
  }

  @Test
  void createScheduleRejectsOverlap() {
    DoctorEntity doctor = new DoctorEntity();
    setId(doctor, 1L);
    when(doctors.findById(1L)).thenReturn(Optional.of(doctor));

    DoctorScheduleEntity existing = new DoctorScheduleEntity();
    setId(existing, 10L);
    existing.setDoctorId(1L);
    existing.setDayOfWeek(1);
    existing.setStartTime(LocalTime.of(8, 0));
    existing.setEndTime(LocalTime.of(12, 0));
    existing.setStatus("ACTIVE");
    when(schedules.findByDoctorIdAndStatusOrderByDayOfWeekAscStartTimeAsc(1L, "ACTIVE"))
        .thenReturn(List.of(existing));

    var req = new UpsertDoctorScheduleRequest(
        1, LocalTime.of(11, 0), LocalTime.of(15, 0), null, null, "ACTIVE");

    DomainException ex = assertThrows(DomainException.class, () -> catalog.createSchedule(1L, req, 1L));
    assertEquals("SCHEDULE_OVERLAP", ex.getCode());
  }

  @Test
  void createScheduleSavesWhenNoOverlap() {
    DoctorEntity doctor = new DoctorEntity();
    setId(doctor, 1L);
    when(doctors.findById(1L)).thenReturn(Optional.of(doctor));
    when(schedules.findByDoctorIdAndStatusOrderByDayOfWeekAscStartTimeAsc(1L, "ACTIVE"))
        .thenReturn(List.of());
    when(schedules.save(any(DoctorScheduleEntity.class))).thenAnswer(inv -> {
      DoctorScheduleEntity s = inv.getArgument(0);
      setId(s, 20L);
      return s;
    });

    var req = new UpsertDoctorScheduleRequest(
        1, LocalTime.of(8, 0), LocalTime.of(12, 0), null, null, "ACTIVE");
    var res = catalog.createSchedule(1L, req, 7L);

    assertEquals(20L, res.id());
    ArgumentCaptor<DoctorScheduleEntity> captor = ArgumentCaptor.forClass(DoctorScheduleEntity.class);
    verify(schedules).save(captor.capture());
    assertEquals(LocalTime.of(8, 0), captor.getValue().getStartTime());
  }

  private static void setId(Object target, Long value) {
    try {
      var field = target.getClass().getDeclaredField("id");
      field.setAccessible(true);
      field.set(target, value);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
