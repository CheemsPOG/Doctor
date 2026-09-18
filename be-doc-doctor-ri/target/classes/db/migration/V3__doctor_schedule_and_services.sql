-- Admin catalog: doctor working hours + doctor↔service mapping

CREATE TABLE doctor_services (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id   BIGINT       NOT NULL,
    service_id  BIGINT       NOT NULL,
    status      VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_doctor_service (doctor_id, service_id),
    CONSTRAINT fk_ds_doctor  FOREIGN KEY (doctor_id)  REFERENCES doctors (id),
    CONSTRAINT fk_ds_service FOREIGN KEY (service_id) REFERENCES services (id)
);

CREATE TABLE doctor_schedules (
    id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id              BIGINT       NOT NULL,
    day_of_week            TINYINT      NOT NULL COMMENT 'ISO: 1=Mon .. 7=Sun',
    start_time             TIME         NOT NULL,
    end_time               TIME         NOT NULL,
    effective_from         DATE         NULL,
    effective_to           DATE         NULL,
    status                 VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
    created_at             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_schedule_doctor_day (doctor_id, day_of_week, status),
    CONSTRAINT fk_dsch_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT chk_schedule_day CHECK (day_of_week BETWEEN 1 AND 7),
    CONSTRAINT chk_schedule_time CHECK (start_time < end_time)
);

-- Seed: every active doctor offers every active service
INSERT INTO doctor_services (doctor_id, service_id, status)
SELECT d.id, s.id, 'ACTIVE'
FROM doctors d
CROSS JOIN services s
WHERE d.status = 'ACTIVE' AND s.status = 'ACTIVE';

-- Seed: Mon–Fri 08:00–17:00 for all active doctors
INSERT INTO doctor_schedules (doctor_id, day_of_week, start_time, end_time, status)
SELECT d.id, dow.day_of_week, '08:00:00', '17:00:00', 'ACTIVE'
FROM doctors d
CROSS JOIN (
    SELECT 1 AS day_of_week UNION ALL SELECT 2 UNION ALL SELECT 3
    UNION ALL SELECT 4 UNION ALL SELECT 5
) dow
WHERE d.status = 'ACTIVE';
