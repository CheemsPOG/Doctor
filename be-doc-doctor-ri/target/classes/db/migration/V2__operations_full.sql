-- Full operations schema (queue, encounter, obstetric, notification, audit, resources)

ALTER TABLE users
    ADD COLUMN role VARCHAR(32) NOT NULL DEFAULT 'PATIENT' AFTER full_name;

ALTER TABLE doctors
    ADD COLUMN user_id BIGINT NULL AFTER clinic_id;

CREATE TABLE rooms (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    clinic_id  BIGINT       NOT NULL,
    code       VARCHAR(32)  NOT NULL,
    name       VARCHAR(255) NOT NULL,
    room_type  VARCHAR(64)  NOT NULL,
    status     VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
    UNIQUE KEY uk_room_clinic_code (clinic_id, code),
    CONSTRAINT fk_rooms_clinic FOREIGN KEY (clinic_id) REFERENCES clinics (id)
);

CREATE TABLE equipments (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    clinic_id       BIGINT       NOT NULL,
    code            VARCHAR(32)  NOT NULL,
    name            VARCHAR(255) NOT NULL,
    equipment_type  VARCHAR(64)  NOT NULL,
    room_id         BIGINT       NULL,
    status          VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
    UNIQUE KEY uk_eq_clinic_code (clinic_id, code),
    CONSTRAINT fk_eq_clinic FOREIGN KEY (clinic_id) REFERENCES clinics (id),
    CONSTRAINT fk_eq_room FOREIGN KEY (room_id) REFERENCES rooms (id)
);

CREATE TABLE resource_bookings (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_id  BIGINT       NOT NULL,
    resource_type   VARCHAR(32)  NOT NULL,
    resource_id     BIGINT       NOT NULL,
    start_at        DATETIME(3)  NOT NULL,
    end_at          DATETIME(3)  NOT NULL,
    status          VARCHAR(32)  NOT NULL,
    hold_expires_at DATETIME(3)  NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rb_appt FOREIGN KEY (appointment_id) REFERENCES appointments (id),
    KEY idx_rb_resource (resource_type, resource_id, start_at, end_at)
);

CREATE TABLE appointment_status_history (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_id  BIGINT       NOT NULL,
    old_status      VARCHAR(32)  NULL,
    new_status      VARCHAR(32)  NOT NULL,
    changed_by      BIGINT       NULL,
    reason          VARCHAR(512) NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ash_appt FOREIGN KEY (appointment_id) REFERENCES appointments (id)
);

CREATE TABLE visit_queue (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_id    BIGINT       NOT NULL,
    queue_type        VARCHAR(32)  NOT NULL DEFAULT 'CONSULT',
    priority          INT          NOT NULL DEFAULT 100,
    queue_number      INT          NOT NULL,
    status            VARCHAR(32)  NOT NULL DEFAULT 'WAITING',
    assigned_room_id  BIGINT       NULL,
    checked_in_at     DATETIME(3)  NULL,
    called_at         DATETIME(3)  NULL,
    started_at        DATETIME(3)  NULL,
    completed_at      DATETIME(3)  NULL,
    CONSTRAINT fk_vq_appt FOREIGN KEY (appointment_id) REFERENCES appointments (id),
    CONSTRAINT fk_vq_room FOREIGN KEY (assigned_room_id) REFERENCES rooms (id),
    KEY idx_vq_day (status, priority)
);

CREATE TABLE encounters (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_id  BIGINT       NOT NULL UNIQUE,
    patient_id      BIGINT       NOT NULL,
    doctor_id       BIGINT       NOT NULL,
    encounter_type  VARCHAR(64)  NOT NULL DEFAULT 'CONSULT',
    started_at      DATETIME(3)  NOT NULL,
    ended_at        DATETIME(3)  NULL,
    summary         VARCHAR(2000) NULL,
    CONSTRAINT fk_enc_appt FOREIGN KEY (appointment_id) REFERENCES appointments (id),
    CONSTRAINT fk_enc_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_enc_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id)
);

CREATE TABLE encounter_activities (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    encounter_id  BIGINT       NOT NULL,
    activity_type VARCHAR(64)  NOT NULL,
    note          VARCHAR(2000) NULL,
    created_by    BIGINT       NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ea_enc FOREIGN KEY (encounter_id) REFERENCES encounters (id)
);

CREATE TABLE pregnancies (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id           BIGINT       NOT NULL,
    pregnancy_code       VARCHAR(32)  NOT NULL UNIQUE,
    last_menstrual_period DATE        NULL,
    estimated_due_date   DATE         NULL,
    pregnancy_status     VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
    risk_level           VARCHAR(32)  NOT NULL DEFAULT 'NORMAL',
    assigned_doctor_id   BIGINT       NULL,
    CONSTRAINT fk_preg_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_preg_doctor FOREIGN KEY (assigned_doctor_id) REFERENCES doctors (id)
);

CREATE TABLE pregnancy_visits (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    pregnancy_id      BIGINT       NOT NULL,
    appointment_id    BIGINT       NULL,
    gestational_week  INT          NULL,
    weight            DECIMAL(6,2) NULL,
    blood_pressure    VARCHAR(32)  NULL,
    fetal_heart_rate  INT          NULL,
    doctor_note       VARCHAR(2000) NULL,
    next_visit_at     DATETIME(3)  NULL,
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pv_preg FOREIGN KEY (pregnancy_id) REFERENCES pregnancies (id),
    CONSTRAINT fk_pv_appt FOREIGN KEY (appointment_id) REFERENCES appointments (id)
);

CREATE TABLE notifications (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    type            VARCHAR(64)  NOT NULL,
    title           VARCHAR(255) NOT NULL,
    content         VARCHAR(2000) NOT NULL,
    reference_type  VARCHAR(64)  NULL,
    reference_id    BIGINT       NULL,
    priority        VARCHAR(16)  NOT NULL DEFAULT 'NORMAL',
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at      TIMESTAMP    NULL
);

CREATE TABLE notification_recipients (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    notification_id     BIGINT       NOT NULL,
    recipient_user_id   BIGINT       NOT NULL,
    is_read             TINYINT(1)   NOT NULL DEFAULT 0,
    read_at             DATETIME(3)  NULL,
    is_archived         TINYINT(1)   NOT NULL DEFAULT 0,
    CONSTRAINT fk_nr_notif FOREIGN KEY (notification_id) REFERENCES notifications (id),
    CONSTRAINT fk_nr_user FOREIGN KEY (recipient_user_id) REFERENCES users (id),
    KEY idx_nr_user (recipient_user_id, is_read)
);

CREATE TABLE notification_deliveries (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    notification_id       BIGINT       NOT NULL,
    recipient_user_id     BIGINT       NOT NULL,
    channel               VARCHAR(16)  NOT NULL,
    destination           VARCHAR(255) NULL,
    status                VARCHAR(32)  NOT NULL DEFAULT 'PENDING',
    provider              VARCHAR(64)  NULL,
    provider_message_id   VARCHAR(128) NULL,
    attempt_count         INT          NOT NULL DEFAULT 0,
    next_retry_at         DATETIME(3)  NULL,
    sent_at               DATETIME(3)  NULL,
    delivered_at          DATETIME(3)  NULL,
    failed_at             DATETIME(3)  NULL,
    error_code            VARCHAR(64)  NULL,
    CONSTRAINT fk_nd_notif FOREIGN KEY (notification_id) REFERENCES notifications (id)
);

CREATE TABLE notification_preferences (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id            BIGINT       NOT NULL,
    notification_type  VARCHAR(64)  NOT NULL,
    in_app_enabled     TINYINT(1)   NOT NULL DEFAULT 1,
    email_enabled      TINYINT(1)   NOT NULL DEFAULT 1,
    sms_enabled        TINYINT(1)   NOT NULL DEFAULT 0,
    push_enabled       TINYINT(1)   NOT NULL DEFAULT 1,
    quiet_hours_start  TIME         NULL,
    quiet_hours_end    TIME         NULL,
    UNIQUE KEY uk_pref (user_id, notification_type),
    CONSTRAINT fk_np_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE outbox_events (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    aggregate_type  VARCHAR(64)  NOT NULL,
    aggregate_id    BIGINT       NOT NULL,
    event_type      VARCHAR(64)  NOT NULL,
    payload         JSON         NOT NULL,
    status          VARCHAR(32)  NOT NULL DEFAULT 'PENDING',
    retry_count     INT          NOT NULL DEFAULT 0,
    available_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    processed_at    DATETIME(3)  NULL,
    KEY idx_outbox_pending (status, available_at)
);

CREATE TABLE refresh_tokens (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    token_hash  VARCHAR(128) NOT NULL UNIQUE,
    expires_at  DATETIME(3)  NOT NULL,
    revoked     TINYINT(1)   NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rt_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE audit_logs (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_user_id BIGINT       NULL,
    action        VARCHAR(64)  NOT NULL,
    entity_type   VARCHAR(64)  NOT NULL,
    entity_id     BIGINT       NULL,
    detail        VARCHAR(2000) NULL,
    ip_address    VARCHAR(64)  NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_audit_entity (entity_type, entity_id)
);

CREATE TABLE push_devices (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    device_token VARCHAR(512) NOT NULL,
    platform    VARCHAR(32)  NOT NULL DEFAULT 'WEB',
    active      TINYINT(1)   NOT NULL DEFAULT 1,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_push_token (device_token),
    CONSTRAINT fk_pd_user FOREIGN KEY (user_id) REFERENCES users (id)
);

INSERT INTO rooms (clinic_id, code, name, room_type, status) VALUES
(1, 'R-C01', 'Phòng khám 1', 'CONSULT', 'ACTIVE'),
(1, 'R-C02', 'Phòng khám 2', 'CONSULT', 'ACTIVE'),
(1, 'R-US1', 'Phòng siêu âm', 'ULTRASOUND', 'ACTIVE');

INSERT INTO equipments (clinic_id, code, name, equipment_type, room_id, status)
SELECT 1, 'EQ-US1', 'Máy siêu âm 1', 'ULTRASOUND', id, 'ACTIVE' FROM rooms WHERE code = 'R-US1';
