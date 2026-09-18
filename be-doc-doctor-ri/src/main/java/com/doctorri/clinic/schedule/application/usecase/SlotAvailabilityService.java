package com.doctorri.clinic.schedule.application.usecase;

import com.doctorri.clinic.appointment.domain.model.AppointmentStatus;
import com.doctorri.clinic.appointment.infrastructure.persistence.AppointmentEntity;
import com.doctorri.clinic.appointment.infrastructure.persistence.AppointmentJpaRepository;
import com.doctorri.clinic.doctor.infrastructure.persistence.DoctorScheduleEntity;
import com.doctorri.clinic.doctor.infrastructure.persistence.DoctorScheduleJpaRepository;
import com.doctorri.clinic.doctor.infrastructure.persistence.DoctorServiceJpaRepository;
import com.doctorri.clinic.servicecatalog.infrastructure.persistence.ServiceEntity;
import com.doctorri.clinic.servicecatalog.infrastructure.persistence.ServiceJpaRepository;
import com.doctorri.clinic.shared.domain.exception.DomainException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class SlotAvailabilityService {

  private static final Set<AppointmentStatus> BLOCKING = EnumSet.of(
      AppointmentStatus.HELD,
      AppointmentStatus.CONFIRMED,
      AppointmentStatus.ATTENDANCE_CONFIRMED);

  private final ServiceJpaRepository services;
  private final AppointmentJpaRepository appointments;
  private final DoctorScheduleJpaRepository schedules;
  private final DoctorServiceJpaRepository doctorServices;

  public SlotAvailabilityService(
      ServiceJpaRepository services,
      AppointmentJpaRepository appointments,
      DoctorScheduleJpaRepository schedules,
      DoctorServiceJpaRepository doctorServices) {
    this.services = services;
    this.appointments = appointments;
    this.schedules = schedules;
    this.doctorServices = doctorServices;
  }

  public SlotDayResponse listSlots(Long serviceId, Long doctorId, LocalDate date) {
    ServiceEntity service = services.findById(serviceId)
        .orElseThrow(() -> new DomainException("SERVICE_NOT_FOUND", "Không tìm thấy dịch vụ."));
    if (!"ACTIVE".equals(service.getStatus())) {
      throw new DomainException("SERVICE_INACTIVE", "Dịch vụ không còn hoạt động.");
    }
    if (!doctorServices.existsByDoctorIdAndServiceIdAndStatus(doctorId, serviceId, "ACTIVE")) {
      throw new DomainException("DOCTOR_SERVICE_NOT_OFFERED", "Bác sĩ không cung cấp dịch vụ này.");
    }

    int duration = service.getDefaultDurationMinutes();
    int bufferBefore = service.getBufferBeforeMinutes();
    int bufferAfter = service.getBufferAfterMinutes();

    List<DoctorScheduleEntity> windows = schedules.findActiveForDoctorOnDate(
        doctorId, date.getDayOfWeek().getValue(), date);
    if (windows.isEmpty()) {
      return new SlotDayResponse(date.toString(), List.of());
    }

    LocalTime earliest = windows.stream().map(DoctorScheduleEntity::getStartTime).min(LocalTime::compareTo).orElse(LocalTime.of(8, 0));
    LocalTime latest = windows.stream().map(DoctorScheduleEntity::getEndTime).max(LocalTime::compareTo).orElse(LocalTime.of(17, 0));
    LocalDateTime dayStart = date.atTime(earliest);
    LocalDateTime dayEnd = date.atTime(latest);

    List<AppointmentEntity> busy = appointments.findOverlaps(
        doctorId, dayStart.minusMinutes(bufferBefore), dayEnd.plusMinutes(bufferAfter), BLOCKING);
    Map<Long, ServiceEntity> serviceById = services.findAllById(
            busy.stream().map(AppointmentEntity::getServiceId).distinct().toList())
        .stream()
        .collect(Collectors.toMap(ServiceEntity::getId, Function.identity()));

    List<SlotItem> slots = new ArrayList<>();
    for (DoctorScheduleEntity window : windows) {
      LocalDateTime cursor = date.atTime(window.getStartTime());
      LocalDateTime windowEnd = date.atTime(window.getEndTime());
      while (!cursor.plusMinutes(duration).isAfter(windowEnd)) {
        final LocalDateTime slotStart = cursor;
        final LocalDateTime slotEnd = cursor.plusMinutes(duration);
        LocalDateTime slotWindowStart = slotStart.minusMinutes(bufferBefore);
        LocalDateTime slotWindowEnd = slotEnd.plusMinutes(bufferAfter);
        boolean available = busy.stream().noneMatch(a -> {
          ServiceEntity existing = serviceById.get(a.getServiceId());
          int existingBefore = existing == null ? 0 : existing.getBufferBeforeMinutes();
          int existingAfter = existing == null ? 0 : existing.getBufferAfterMinutes();
          LocalDateTime busyStart = a.getScheduledStartAt().minusMinutes(existingBefore);
          LocalDateTime busyEnd = a.getScheduledEndAt().plusMinutes(existingAfter);
          return overlaps(slotWindowStart, slotWindowEnd, busyStart, busyEnd);
        });
        String slotKey = "DR" + doctorId + "-" + date.format(DateTimeFormatter.BASIC_ISO_DATE)
            + "-" + slotStart.toLocalTime().format(DateTimeFormatter.ofPattern("HHmm"));
        slots.add(new SlotItem(
            slotKey,
            slotStart.atOffset(ZoneOffset.ofHours(7)).toString(),
            slotEnd.atOffset(ZoneOffset.ofHours(7)).toString(),
            available));
        cursor = slotEnd;
      }
    }
    return new SlotDayResponse(date.toString(), slots);
  }

  private static boolean overlaps(LocalDateTime s1, LocalDateTime e1, LocalDateTime s2, LocalDateTime e2) {
    return s1.isBefore(e2) && s2.isBefore(e1);
  }

  public record SlotDayResponse(String date, List<SlotItem> slots) {}

  public record SlotItem(String slotKey, String startAt, String endAt, boolean available) {}
}
