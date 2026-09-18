package com.doctorri.clinic.notification.application.usecase;

import com.doctorri.clinic.appointment.application.dto.AppointmentEventPayload;

/** Renders Vietnamese notification title/body text for outbox event types. */
final class NotificationContentRenderer {

  private NotificationContentRenderer() {}

  record Content(String title, String body) {}

  static Content render(String eventType, AppointmentEventPayload payload) {
    String code = payload.appointmentCode() != null ? payload.appointmentCode() : "";
    return switch (eventType) {
      case "APPOINTMENT_CREATED" -> new Content(
          "Lịch hẹn đã được đặt",
          "Lịch hẹn " + code + " của bạn vào " + payload.startAt() + " đã được xác nhận.");
      case "APPOINTMENT_CANCELLED" -> new Content(
          "Lịch hẹn đã bị hủy",
          "Lịch hẹn " + code + " vào " + payload.startAt() + " đã bị hủy.");
      case "APPOINTMENT_RESCHEDULED" -> new Content(
          "Lịch hẹn đã được đổi giờ",
          "Lịch hẹn " + code + " đã được đổi sang " + payload.startAt() + ".");
      case "ATTENDANCE_CONFIRMED" -> new Content(
          "Xác nhận tham dự",
          "Lịch hẹn " + code + " vào " + payload.startAt() + " đã được xác nhận tham dự.");
      default -> new Content("Thông báo lịch hẹn", "Lịch hẹn " + code + " có cập nhật mới.");
    };
  }
}
