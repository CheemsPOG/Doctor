package com.doctorri.clinic.admin.application.dto;

import com.doctorri.clinic.doctor.infrastructure.persistence.DoctorEntity;
import com.doctorri.clinic.doctor.infrastructure.persistence.DoctorScheduleEntity;
import com.doctorri.clinic.servicecatalog.infrastructure.persistence.ServiceEntity;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public final class AdminCatalogDtos {

  private AdminCatalogDtos() {}

  public record ServiceAdminResponse(
      Long id,
      String code,
      String name,
      String category,
      String description,
      int durationMinutes,
      int bufferBeforeMinutes,
      int bufferAfterMinutes,
      boolean holdRoomOnBooking,
      boolean requiresUltrasound,
      String bookingPolicy,
      String status
  ) {
    public static ServiceAdminResponse from(ServiceEntity s) {
      return new ServiceAdminResponse(
          s.getId(), s.getCode(), s.getName(), s.getCategory(), s.getDescription(),
          s.getDefaultDurationMinutes(), s.getBufferBeforeMinutes(), s.getBufferAfterMinutes(),
          s.isHoldRoomOnBooking(), s.isRequiresUltrasound(), s.getBookingPolicy(), s.getStatus());
    }
  }

  public record DoctorAdminResponse(
      Long id,
      Long clinicId,
      Long userId,
      String doctorCode,
      String fullName,
      String specialty,
      String bio,
      String status,
      List<Long> serviceIds
  ) {
    public static DoctorAdminResponse from(DoctorEntity d, List<Long> serviceIds) {
      return new DoctorAdminResponse(
          d.getId(), d.getClinicId(), d.getUserId(), d.getDoctorCode(), d.getFullName(),
          d.getSpecialty(), d.getBio(), d.getStatus(), serviceIds);
    }
  }

  public record ScheduleAdminResponse(
      Long id,
      Long doctorId,
      int dayOfWeek,
      LocalTime startTime,
      LocalTime endTime,
      LocalDate effectiveFrom,
      LocalDate effectiveTo,
      String status
  ) {
    public static ScheduleAdminResponse from(DoctorScheduleEntity s) {
      return new ScheduleAdminResponse(
          s.getId(), s.getDoctorId(), s.getDayOfWeek(), s.getStartTime(), s.getEndTime(),
          s.getEffectiveFrom(), s.getEffectiveTo(), s.getStatus());
    }
  }
}
