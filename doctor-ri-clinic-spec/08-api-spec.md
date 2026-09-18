# 08. REST API đề xuất

Base URL:

```text
/api/v1
```

## 8.1 Authentication

```http
POST /auth/register
POST /auth/login
POST /auth/refresh
POST /auth/logout
```

## 8.2 Dịch vụ và bác sĩ

```http
GET /services
GET /services/{id}
GET /doctors
GET /doctors/{id}
GET /doctors/{id}/services
```

## 8.3 Slot

```http
GET /availability/slots?serviceId=1&doctorId=2&date=2026-07-25
```

Response mẫu:

```json
{
  "date": "2026-07-25",
  "slots": [
    {
      "slotKey": "DR2-20260725-0900",
      "startAt": "2026-07-25T09:00:00+07:00",
      "endAt": "2026-07-25T09:30:00+07:00",
      "available": true
    }
  ]
}
```

## 8.4 Appointment

```http
POST /appointments
GET /appointments/{id}
GET /me/appointments
POST /appointments/{id}/confirm
POST /appointments/{id}/cancel
POST /appointments/{id}/reschedule
```

Request tạo lịch:

```json
{
  "patientId": 1001,
  "serviceId": 10,
  "doctorId": 2,
  "startAt": "2026-07-25T09:00:00+07:00",
  "reason": "Khám thai định kỳ"
}
```

Header khuyến nghị:

```http
Idempotency-Key: 5e1ddc2a-...
```

## 8.1b Patient profile

```http
GET /me/profile
PUT /me/profile
```

## 8.5 Lễ tân

```http
POST /reception/appointments
POST /reception/appointments?walkIn=true
POST /appointments/{id}/check-in
POST /appointments/{id}/assign-room
POST /appointments/{id}/mark-no-show
GET /clinic/queue?date=2026-07-25
```

Check-in body (optional):

```json
{ "medicalUrgent": true }
```

Priority (thấp hơn = ưu tiên hơn): urgent=10, on-time=20, early=30, late-in-grace=40, walk-in=50.

## 8.6 Queue

```http
POST /queue/{id}/call
POST /queue/{id}/start
POST /queue/{id}/complete
POST /queue/{id}/reprioritize
```

## 8.7 Doctor

```http
GET /doctor/me/schedule
GET /doctor/me/queue
POST /appointments/{id}/start-exam
POST /appointments/{id}/complete-exam
POST /appointments/{id}/activities
```

## 8.8 Notification

```http
GET /me/notifications
PATCH /me/notifications/{id}/read
PATCH /me/notifications/read-all
GET /me/notification-preferences
PUT /me/notification-preferences
```

## 8.10 Admin catalog

```http
GET    /admin/services
POST   /admin/services
PUT    /admin/services/{id}
DELETE /admin/services/{id}

GET    /admin/doctors
POST   /admin/doctors
PUT    /admin/doctors/{id}
DELETE /admin/doctors/{id}
GET    /admin/doctors/{id}/services
PUT    /admin/doctors/{id}/services
GET    /admin/doctors/{id}/schedules
POST   /admin/doctors/{id}/schedules
PUT    /admin/schedules/{scheduleId}
DELETE /admin/schedules/{scheduleId}

GET    /admin/rooms
GET    /admin/equipments
GET    /admin/audit-logs
```

Roles: `CLINIC_ADMIN`, `SYSTEM_ADMIN`. Soft-delete qua `status=INACTIVE`.

## 8.9 Error response

```json
{
  "code": "SLOT_NOT_AVAILABLE",
  "message": "Khung giờ vừa được người khác đặt.",
  "traceId": "abc-123",
  "details": null
}
```
