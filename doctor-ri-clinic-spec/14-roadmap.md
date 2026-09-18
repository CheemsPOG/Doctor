# 14. Roadmap phát triển

## Phase 1 – Foundation

- Spring Boot project.
- React project.
- Authentication.
- User, patient, doctor, service.
- Flyway và Docker Compose.

## Phase 2 – Appointment core

- Doctor schedule.
- Slot generation.
- Resource booking.
- Tạo, đổi, hủy lịch.
- Chống double booking.

## Phase 3 – Clinic operations

- Check-in.
- Queue.
- Gán phòng.
- Actual start/end.
- Delay warning.

## Phase 4 – Notification

- Outbox + worker.
- Email qua MailHog.
- Push notification (FCM/APNs hoặc Web Push — chốt provider khi implement).
- Reminder 24 giờ và 2 giờ.
- Retry.
- Lưu `notifications` để xem trên portal.
- Mở rộng SMS (provider + channel adapter) ngay sau khi email/push ổn định.

## Phase 5 – Obstetric module

- Pregnancy profile.
- Pregnancy visit.
- Follow-up appointment.
- Lịch khám thai theo thai kỳ.

## Phase 6 – Hardening

- Audit.
- Security review.
- Performance test.
- Backup/restore.
- Monitoring.
- CI/CD.
