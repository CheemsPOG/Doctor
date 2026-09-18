-- Richer public service blurbs + clinical result media table.
-- Demo media rows are seeded in DemoPatientSeeder (after patients exist).

ALTER TABLE services
    MODIFY COLUMN description VARCHAR(2048) NULL;

UPDATE services SET description = 'Buổi khám đầu tiên gồm khai thác tiền sử, khám lâm sàng, tư vấn dinh dưỡng và lịch theo dõi thai. Phù hợp mẹ bầu mới đến phòng khám.'
WHERE code = 'OB_FIRST';

UPDATE services SET description = 'Tái khám định kỳ: đo cân nặng, huyết áp, nghe tim thai, đánh giá triệu chứng và điều chỉnh kế hoạch chăm sóc theo tuần thai.'
WHERE code = 'OB_FOLLOWUP';

UPDATE services SET description = 'Siêu âm thai đánh giá kích thước, vị trí, nhịp tim và một số chỉ số phát triển. Có thể kèm hình/clip siêu âm trong phần kết quả (nếu được lưu).'
WHERE code = 'US_OB';

UPDATE services SET description = 'Bác sĩ giải thích kết quả siêu âm / xét nghiệm, trả lời thắc mắc và hướng dẫn bước tiếp theo. Có thể xem lại hình ảnh, video đính kèm nếu đã được tải lên.'
WHERE code = 'RESULT_CONSULT';

UPDATE services SET description = 'Khám phụ khoa định kỳ trong không gian riêng tư: tầm soát cơ bản, tư vấn triệu chứng và hướng xử trí phù hợp.'
WHERE code = 'GYN_BASIC';

CREATE TABLE IF NOT EXISTS clinical_result_assets (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id      BIGINT        NOT NULL,
    appointment_id  BIGINT        NULL,
    service_id      BIGINT        NULL,
    media_type      VARCHAR(16)   NOT NULL COMMENT 'IMAGE | VIDEO',
    title           VARCHAR(255)  NOT NULL,
    caption         VARCHAR(1024) NULL,
    url             VARCHAR(1024) NOT NULL,
    thumbnail_url   VARCHAR(1024) NULL,
    status          VARCHAR(32)   NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cra_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_cra_appt FOREIGN KEY (appointment_id) REFERENCES appointments (id),
    CONSTRAINT fk_cra_service FOREIGN KEY (service_id) REFERENCES services (id),
    KEY idx_cra_patient (patient_id, status, created_at)
);
