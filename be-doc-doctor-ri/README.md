# Doctor Ri Clinic – Backend (Full MVP)

Modular monolith Spring Boot – Clean Architecture. Chạy được end-to-end theo spec API 08.

## Chạy

```bash
export JAVA_HOME=... # JDK 20+
# MySQL db_clinic + Redis bắt buộc
mvn test
mvn spring-boot:run
# hoặc: SERVER_PORT=8081 mvn spring-boot:run
```

Health: `/actuator/health`

## Demo accounts (Password123!)

| Email | Role |
|---|---|
| `mebau@doctorri.local` | PATIENT |
| `receptionist@doctorri.local` | RECEPTIONIST |
| `doctor.ri@doctorri.local` | DOCTOR (DR-RI) |
| `admin@doctorri.local` | CLINIC_ADMIN |

## Đã có

- JWT access + refresh, RBAC
- Services / Doctors / Slots (có buffer before/after)
- Appointments: create, confirm attendance, cancel, reschedule
- `Idempotency-Key` header trên `POST /appointments`
- Redis slot hold + resource booking (doctor / ultrasound room+machine, kèm buffer)
- Reception: create, check-in (`operational_status=CHECKED_IN`), assign-room, no-show, clinic queue
- Queue: call / start / complete / reprioritize
- Doctor: schedule, queue, start/complete exam, activities
- Notification: outbox worker → EMAIL + PUSH (push stub log), inbox APIs
- Reminder outbox `APPOINTMENT_REMINDER_24H` / `_2H` (hủy khi cancel/reschedule)
- Obstetric: pregnancies + visits
- Patient profile: `GET/PUT /api/v1/me/profile`
- Walk-in: `POST /reception/appointments?walkIn=true` + priority §5.4 khi check-in
- Push: `doctorri.push.provider=FCM` + credentials → Firebase; mặc định STUB
- Admin CRUD: `/api/v1/admin/services`, `/doctors`, `/doctors/{id}/services`, `/doctors/{id}/schedules`
- Slot generation theo `doctor_schedules` + mapping `doctor_services`
- Flyway V1–V3, unit + integration/concurrency tests (Testcontainers)

## Channels

`mvp_channels = EMAIL,PUSH` (SMS sau).

Push thật (FCM):

```bash
export PUSH_PROVIDER=FCM
export FCM_CREDENTIALS_PATH=/absolute/path/to/firebase-service-account.json
export FCM_PROJECT_ID=your-firebase-project
```
