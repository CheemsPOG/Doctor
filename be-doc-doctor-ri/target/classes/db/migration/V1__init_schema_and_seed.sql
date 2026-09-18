-- Doctor Ri Clinic – core schema + seed (MVP)

CREATE TABLE users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    phone           VARCHAR(32)  NULL,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(255) NOT NULL,
    status          VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE patients (
    id                         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id                    BIGINT       NOT NULL UNIQUE,
    patient_code               VARCHAR(32)  NOT NULL UNIQUE,
    full_name                  VARCHAR(255) NOT NULL,
    date_of_birth              DATE         NULL,
    gender                     VARCHAR(16)  NULL,
    phone                      VARCHAR(32)  NULL,
    email                      VARCHAR(255) NULL,
    emergency_contact_name     VARCHAR(255) NULL,
    emergency_contact_phone    VARCHAR(32)  NULL,
    CONSTRAINT fk_patients_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE clinics (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    code       VARCHAR(32)  NOT NULL UNIQUE,
    name       VARCHAR(255) NOT NULL,
    address    VARCHAR(512) NULL,
    status     VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE doctors (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    clinic_id     BIGINT       NOT NULL,
    doctor_code   VARCHAR(32)  NOT NULL UNIQUE,
    full_name     VARCHAR(255) NOT NULL,
    specialty     VARCHAR(128) NOT NULL DEFAULT 'Sản phụ khoa',
    bio           VARCHAR(1024) NULL,
    status        VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT fk_doctors_clinic FOREIGN KEY (clinic_id) REFERENCES clinics (id)
);

CREATE TABLE services (
    id                        BIGINT AUTO_INCREMENT PRIMARY KEY,
    code                      VARCHAR(32)  NOT NULL UNIQUE,
    name                      VARCHAR(255) NOT NULL,
    category                  VARCHAR(64)  NOT NULL,
    description               VARCHAR(1024) NULL,
    default_duration_minutes  INT          NOT NULL,
    buffer_before_minutes     INT          NOT NULL DEFAULT 5,
    buffer_after_minutes      INT          NOT NULL DEFAULT 5,
    hold_room_on_booking      TINYINT(1)   NOT NULL DEFAULT 0,
    requires_ultrasound       TINYINT(1)   NOT NULL DEFAULT 0,
    booking_policy            VARCHAR(64)  NOT NULL DEFAULT 'STANDARD',
    status                    VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE clinic_settings (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    clinic_id   BIGINT       NOT NULL,
    setting_key VARCHAR(128) NOT NULL,
    setting_value VARCHAR(512) NOT NULL,
    UNIQUE KEY uk_clinic_setting (clinic_id, setting_key),
    CONSTRAINT fk_settings_clinic FOREIGN KEY (clinic_id) REFERENCES clinics (id)
);

CREATE TABLE appointments (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_code     VARCHAR(32)  NOT NULL UNIQUE,
    patient_id           BIGINT       NOT NULL,
    clinic_id            BIGINT       NOT NULL,
    doctor_id            BIGINT       NOT NULL,
    service_id           BIGINT       NOT NULL,
    scheduled_start_at   DATETIME(3)  NOT NULL,
    scheduled_end_at     DATETIME(3)  NOT NULL,
    estimated_start_at   DATETIME(3)  NULL,
    actual_start_at      DATETIME(3)  NULL,
    actual_end_at        DATETIME(3)  NULL,
    status               VARCHAR(32)  NOT NULL,
    operational_status   VARCHAR(32)  NULL,
    source               VARCHAR(32)  NOT NULL DEFAULT 'ONLINE',
    reason               VARCHAR(512) NULL,
    late_cancel          TINYINT(1)   NOT NULL DEFAULT 0,
    hold_expires_at      DATETIME(3)  NULL,
    created_by           BIGINT       NULL,
    created_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version              BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT fk_appt_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_appt_clinic  FOREIGN KEY (clinic_id)  REFERENCES clinics (id),
    CONSTRAINT fk_appt_doctor  FOREIGN KEY (doctor_id)  REFERENCES doctors (id),
    CONSTRAINT fk_appt_service FOREIGN KEY (service_id) REFERENCES services (id),
    KEY idx_appt_doctor_start (doctor_id, scheduled_start_at),
    KEY idx_appt_patient (patient_id)
);

INSERT INTO clinics (id, code, name, address, status) VALUES
(1, 'DR-HN', 'Doctor Ri Clinic', 'Hà Nội', 'ACTIVE');

INSERT INTO clinic_settings (clinic_id, setting_key, setting_value) VALUES
(1, 'slot_hold_minutes', '5'),
(1, 'booking_window_days', '30'),
(1, 'free_cancel_hours', '24'),
(1, 'late_grace_minutes', '15'),
(1, 'reminder_hours', '24,2'),
(1, 'attendance_confirm_enabled', 'true'),
(1, 'mvp_channels', 'EMAIL,PUSH');

INSERT INTO doctors (id, clinic_id, doctor_code, full_name, specialty, bio, status) VALUES
(1, 1, 'DR-RI', 'Bác sĩ Ri', 'Sản phụ khoa', 'Đồng hành cùng mẹ bầu với sự tận tâm và nhẹ nhàng.', 'ACTIVE'),
(2, 1, 'DR-LAN', 'Bác sĩ Lan Anh', 'Sản khoa', 'Chuyên khám thai và siêu âm thai.', 'ACTIVE');

INSERT INTO services (code, name, category, description, default_duration_minutes, buffer_before_minutes, buffer_after_minutes, hold_room_on_booking, requires_ultrasound, status) VALUES
('OB_FIRST', 'Khám thai lần đầu', 'OBSTETRIC', 'Tư vấn toàn diện cho mẹ bầu lần đầu đến phòng khám.', 40, 5, 5, 0, 0, 'ACTIVE'),
('OB_FOLLOWUP', 'Khám thai định kỳ', 'OBSTETRIC', 'Theo dõi sức khỏe mẹ và bé theo lịch thai kỳ.', 25, 5, 5, 0, 0, 'ACTIVE'),
('US_OB', 'Siêu âm thai', 'ULTRASOUND', 'Siêu âm đánh giá sự phát triển của thai nhi.', 30, 5, 10, 1, 1, 'ACTIVE'),
('RESULT_CONSULT', 'Tư vấn kết quả', 'CONSULT', 'Trao đổi kết quả siêu âm / xét nghiệm với bác sĩ.', 20, 5, 5, 0, 0, 'ACTIVE'),
('GYN_BASIC', 'Khám phụ khoa cơ bản', 'GYNECOLOGY', 'Khám phụ khoa định kỳ, nhẹ nhàng và riêng tư.', 25, 5, 5, 0, 0, 'ACTIVE');
