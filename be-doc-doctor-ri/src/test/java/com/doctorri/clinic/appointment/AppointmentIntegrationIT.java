package com.doctorri.clinic.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.doctorri.clinic.appointment.application.dto.CreateAppointmentRequest;
import com.doctorri.clinic.appointment.application.dto.RescheduleAppointmentRequest;
import com.doctorri.clinic.appointment.application.usecase.AppointmentApplicationService;
import com.doctorri.clinic.appointment.infrastructure.persistence.AppointmentJpaRepository;
import com.doctorri.clinic.auth.application.dto.RegisterRequest;
import com.doctorri.clinic.auth.application.usecase.AuthService;
import com.doctorri.clinic.auth.domain.model.UserRole;
import com.doctorri.clinic.auth.infrastructure.security.AuthUserPrincipal;
import com.doctorri.clinic.clinic.application.ClinicOperationsService;
import com.doctorri.clinic.doctor.infrastructure.persistence.DoctorJpaRepository;
import com.doctorri.clinic.notification.infrastructure.outbox.OutboxEventJpaRepository;
import com.doctorri.clinic.patient.application.dto.UpdatePatientProfileRequest;
import com.doctorri.clinic.patient.application.usecase.PatientProfileService;
import com.doctorri.clinic.patient.infrastructure.persistence.PatientJpaRepository;
import com.doctorri.clinic.queue.domain.service.QueuePriorityPolicy;
import com.doctorri.clinic.servicecatalog.infrastructure.persistence.ServiceJpaRepository;
import com.doctorri.clinic.shared.domain.exception.DomainException;
import com.doctorri.clinic.support.IntegrationContainers;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@EnabledIf("com.doctorri.clinic.support.IntegrationContainers#dockerAvailable")
class AppointmentIntegrationIT {

  @BeforeAll
  static void requireDocker() {
    IntegrationContainers.ensureStarted();
  }

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry registry) {
    IntegrationContainers.register(registry);
  }

  @Autowired AuthService authService;
  @Autowired AppointmentApplicationService appointments;
  @Autowired AppointmentJpaRepository appointmentRepo;
  @Autowired OutboxEventJpaRepository outbox;
  @Autowired ServiceJpaRepository services;
  @Autowired DoctorJpaRepository doctors;
  @Autowired PatientJpaRepository patients;
  @Autowired PatientProfileService profiles;
  @Autowired ClinicOperationsService clinicOps;

  @AfterEach
  void clearSecurity() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void createAppointmentWritesOutboxAndBlocksDoubleBook() {
    Registered registered = registerPatient("patient-a-" + UUID.randomUUID() + "@test.local");
    Long serviceId = services.findByCode("OB_FOLLOWUP").orElseThrow().getId();
    Long doctorId = doctors.findByDoctorCode("DR-RI").orElseThrow().getId();
    OffsetDateTime start = nextWeekdayAt(9, 0);

    var created = appointments.create(
        new CreateAppointmentRequest(registered.patientId(), serviceId, doctorId, start, "IT create"),
        registered.userId(),
        "ONLINE");

    assertTrue(appointmentRepo.findById(created.id()).isPresent());
    assertTrue(outbox.findAll().stream()
        .anyMatch(e -> e.getAggregateId().equals(created.id()) && "APPOINTMENT_CREATED".equals(e.getEventType())));

    DomainException conflict = assertThrows(DomainException.class, () ->
        appointments.create(
            new CreateAppointmentRequest(registered.patientId(), serviceId, doctorId, start, "duplicate"),
            registered.userId(),
            "ONLINE"));
    assertEquals("SLOT_NOT_AVAILABLE", conflict.getCode());
  }

  @Test
  void rescheduleReleasesOldSlotAndCreatesNewOutbox() {
    Registered registered = registerPatient("patient-b-" + UUID.randomUUID() + "@test.local");
    Long serviceId = services.findByCode("OB_FOLLOWUP").orElseThrow().getId();
    Long doctorId = doctors.findByDoctorCode("DR-RI").orElseThrow().getId();
    OffsetDateTime start = nextWeekdayAt(10, 0);
    OffsetDateTime newStart = nextWeekdayAt(11, 0);

    var created = appointments.create(
        new CreateAppointmentRequest(registered.patientId(), serviceId, doctorId, start, "reschedule"),
        registered.userId(),
        "ONLINE");

    var updated = appointments.reschedule(
        created.id(),
        new RescheduleAppointmentRequest(newStart, doctorId),
        registered.userId());

    assertEquals(newStart.toLocalDateTime(), updated.startAt().toLocalDateTime());
    assertTrue(outbox.findAll().stream()
        .anyMatch(e -> e.getAggregateId().equals(created.id()) && "APPOINTMENT_RESCHEDULED".equals(e.getEventType())));
  }

  @Test
  void patientProfileGetAndUpdate() {
    Registered registered = registerPatient("patient-c-" + UUID.randomUUID() + "@test.local");
    setSecurity(registered);

    var before = profiles.getMine();
    assertEquals(registered.userId(), before.userId());

    var updated = profiles.updateMine(new UpdatePatientProfileRequest(
        "Nguyễn Test",
        LocalDate.of(1995, 5, 1),
        "FEMALE",
        "0901000000",
        registered.email(),
        "Chồng",
        "0902000000"));

    assertEquals("Nguyễn Test", updated.fullName());
    assertEquals(LocalDate.of(1995, 5, 1), updated.dateOfBirth());
    assertEquals("0901000000", updated.phone());
    assertEquals("Chồng", updated.emergencyContactName());
  }

  @Test
  void walkInCheckInGetsWalkInPriority() {
    Registered registered = registerPatient("patient-e-" + UUID.randomUUID() + "@test.local");
    Long serviceId = services.findByCode("GYN_BASIC").orElseThrow().getId();
    Long doctorId = doctors.findByDoctorCode("DR-RI").orElseThrow().getId();
    OffsetDateTime start = OffsetDateTime.now(ZoneOffset.ofHours(7)).plusMinutes(30);

    var created = clinicOps.createReception(
        new CreateAppointmentRequest(registered.patientId(), serviceId, doctorId, start, "walk-in"),
        registered.userId(),
        true);

    var queue = clinicOps.checkIn(created.id(), registered.userId(), false);
    assertEquals(QueuePriorityPolicy.WALK_IN, queue.getPriority());
  }

  @Test
  void concurrentSameSlotOnlyOneSucceeds() throws Exception {
    Registered registered = registerPatient("patient-d-" + UUID.randomUUID() + "@test.local");
    Long serviceId = services.findByCode("GYN_BASIC").orElseThrow().getId();
    Long doctorId = doctors.findByDoctorCode("DR-LAN").orElseThrow().getId();
    OffsetDateTime start = nextWeekdayAt(14, 0);

    int threads = 20;
    ExecutorService pool = Executors.newFixedThreadPool(threads);
    CountDownLatch ready = new CountDownLatch(threads);
    CountDownLatch startGate = new CountDownLatch(1);
    AtomicInteger success = new AtomicInteger();
    AtomicInteger conflicts = new AtomicInteger();

    for (int i = 0; i < threads; i++) {
      pool.submit(() -> {
        ready.countDown();
        try {
          startGate.await(10, TimeUnit.SECONDS);
          appointments.create(
              new CreateAppointmentRequest(registered.patientId(), serviceId, doctorId, start, "race"),
              registered.userId(),
              "ONLINE");
          success.incrementAndGet();
        } catch (DomainException ex) {
          if ("SLOT_NOT_AVAILABLE".equals(ex.getCode())) {
            conflicts.incrementAndGet();
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      });
    }

    assertTrue(ready.await(10, TimeUnit.SECONDS));
    startGate.countDown();
    pool.shutdown();
    assertTrue(pool.awaitTermination(60, TimeUnit.SECONDS));

    assertEquals(1, success.get(), "exactly one booking must succeed");
    assertEquals(threads - 1, conflicts.get());
  }

  private record Registered(Long userId, Long patientId, String email) {}

  private Registered registerPatient(String email) {
    var res = authService.register(new RegisterRequest(email, "Password123!", "IT Patient", "0900000000"));
    return new Registered(res.userId(), res.patientId(), email);
  }

  private void setSecurity(Registered registered) {
    var principal = new AuthUserPrincipal(
        registered.userId(), registered.email(), UserRole.PATIENT, registered.patientId(), null);
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
  }

  private static OffsetDateTime nextWeekdayAt(int hour, int minute) {
    LocalDate date = LocalDate.now().plusDays(2);
    while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
      date = date.plusDays(1);
    }
    return OffsetDateTime.of(date, LocalTime.of(hour, minute), ZoneOffset.ofHours(7));
  }
}
