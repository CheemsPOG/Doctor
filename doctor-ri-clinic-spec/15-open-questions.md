# 15. Các quyết định nghiệp vụ đã chốt (MVP)

> **Status:** Accepted (MVP defaults) — 2026-07-20  
> Giá trị có thể cấu hình trong admin sau; số dưới đây là **seed / default** để triển khai Phase 1–5.  
> Thay đổi sau khi go-live phải ghi ADR nghiệp vụ mới.

---

## 15.1 Dịch vụ

| Câu hỏi | Quyết định MVP |
|---|---|
| Mỗi dịch vụ kéo dài bao lâu? | Cấu hình theo `services.default_duration_minutes`. Seed: |
| Có buffer trước/sau không? | Có. Seed: `buffer_before = 5`, `buffer_after = 5` (phút). Siêu âm: `buffer_after = 10`. |
| Dịch vụ nào giữ phòng ngay khi đặt? | **Siêu âm thai** (và thủ thuật nếu có). Khám tư vấn: giữ *loại phòng*, gán phòng lúc check-in. |
| Dịch vụ nào cần máy siêu âm? | Chỉ **Siêu âm thai**. |

### Seed duration (phút)

| Mã dịch vụ (gợi ý) | Tên | Duration | Giữ phòng ngay | Cần máy siêu âm |
|---|---|---:|---|---|
| `OB_FIRST` | Khám thai lần đầu | 40 | Không | Không |
| `OB_FOLLOWUP` | Khám thai định kỳ | 25 | Không | Không |
| `US_OB` | Siêu âm thai | 30 | Có (phòng siêu âm) | Có |
| `RESULT_CONSULT` | Tư vấn kết quả | 20 | Không | Không |
| `GYN_BASIC` | Khám phụ khoa cơ bản | 25 | Không | Không |

Tham chiếu: [04-booking-and-resource-rules.md](./04-booking-and-resource-rules.md).

---

## 15.2 Đặt lịch

| Câu hỏi | Quyết định MVP |
|---|---|
| Lịch online xác nhận ngay hay lễ tân duyệt? | **Xác nhận ngay** khi giữ slot thành công → status `CONFIRMED`. Lễ tân vẫn tạo/đổi/hủy hộ; không chặn flow duyệt thủ công. Hold tạm `HELD` 5 phút trong lúc thanh toán form (xem §4.6). |
| Chọn đúng bác sĩ hay bất kỳ? | Bệnh nhân **chọn bác sĩ cụ thể**. Tuỳ chọn `doctorId = null` / “Bác sĩ bất kỳ” → hệ thống gán bác sĩ còn slot sớm nhất trong ngày. |
| Đặt trước tối đa bao nhiêu ngày? | **30 ngày** (`booking_window_days`, cấu hình được). |
| Hủy miễn phí trước bao lâu? | **≥ 24 giờ** trước `scheduled_start_at`. Dưới 24 giờ: vẫn hủy được nhưng gắn cờ `late_cancel = true` (báo cáo vận hành; chưa thu phí online). |

Luồng trạng thái rút gọn:

```text
HELD (≤5 phút) → CONFIRMED → (optional) ATTENDANCE_CONFIRMED
                          ↘ CANCELLED / NO_SHOW / COMPLETED
```

---

## 15.3 Vận hành

| Câu hỏi | Quyết định MVP |
|---|---|
| Grace period đến muộn? | **15 phút** sau `scheduled_start_at` / `estimated_start_at`. Quá hạn: lễ tân quyết định `NO_SHOW` hoặc xếp lại (không auto). |
| Walk-in xếp theo quy tắc nào? | Theo [§5.4](./05-queue-and-clinic-operations.md): sau bệnh nhân có lịch (đúng giờ → sớm → muộn trong grace). Walk-in tạo appointment `source = WALK_IN`, vào queue ngay sau check-in. |
| Bao nhiêu phòng cùng loại? | Seed **1 cơ sở**: 2 phòng khám tư vấn + 1 phòng siêu âm (+ 1 máy siêu âm). Số lượng quản lý qua admin, không hard-code. |
| Ai đổi queue priority? | `RECEPTIONIST`, `CLINIC_ADMIN`, `DOCTOR` (ưu tiên y tế). Mọi đổi bắt buộc `reason` + audit. |

---

## 15.4 Notification

| Câu hỏi | Quyết định MVP |
|---|---|
| Kênh MVP? | **Email + Push**. Tiếp theo mở rộng **SMS**. Zalo sau. Schema `notification_deliveries.channel` đa kênh sẵn; worker MVP bật `EMAIL` và `PUSH` (local email: MailHog). |
| Nhắc lịch trước bao lâu? | **24 giờ** và **2 giờ** trước giờ hẹn ([§9.5](./09-notification-design.md)). |
| Yêu cầu xác nhận sẽ đến? | **Có.** Sau nhắc 24h, bệnh nhân xác nhận qua portal / deep link → `ATTENDANCE_CONFIRMED`. Không xác nhận: chỉ cảnh báo lễ tân dashboard, **không** auto-cancel. |

---

## 15.5 Dữ liệu Sản khoa

| Câu hỏi | Quyết định MVP |
|---|---|
| Chỉ số MVP? | Trên `pregnancies`: LMP, EDD, `pregnancy_status`, `risk_level`, bác sĩ phụ trách. Trên `pregnancy_visits`: gestational week, cân nặng, huyết áp, nhịp tim thai, ghi chú bác sĩ, `next_visit_at`. |
| Nhập kết quả siêu âm? | **Ghi chú văn bản** + liên kết `appointment_id` / `encounter`. Không PACS/DICOM (ngoài phạm vi MVP). |
| Lưu PDF / hình ảnh? | **Có, tùy chọn** qua MinIO từ Phase 5: đính kèm tối đa vài file / visit (PDF, JPEG/PNG). Không bắt buộc để hoàn tất khám. |

---

## 15.6 Tổ chức hệ thống

| Câu hỏi | Quyết định MVP |
|---|---|
| Một hay nhiều cơ sở? | **Một cơ sở** vận hành MVP. Schema `clinic_id` bắt buộc để mở rộng sau ([§1.1](./01-product-scope.md)). |
| Một hay nhiều bác sĩ? | **Nhiều bác sĩ** trong model. Seed tối thiểu: Doctor Ri + 1 bác sĩ phụ (có thể tắt lịch). |
| Đa ngôn ngữ? | **Chỉ tiếng Việt** trên UI MVP. Chuỗi UI tách key để bổ sung EN sau; không i18n runtime Phase 1. |

---

## 15.7 Tham số cấu hình hệ thống (seed)

Đưa vào bảng `clinic_settings` (hoặc config admin) — không hard-code trong business rule:

| Key | Default |
|---|---|
| `slot_hold_minutes` | `5` |
| `booking_window_days` | `30` |
| `free_cancel_hours` | `24` |
| `late_grace_minutes` | `15` |
| `reminder_hours` | `24,2` |
| `attendance_confirm_enabled` | `true` |
| `mvp_channels` | `EMAIL,PUSH` (bật `SMS` khi mở rộng) |

---

## 15.8 Việc đã đóng / không còn mở

Các mục trong bản draft trước đã được trả lời ở trên. Nếu phòng khám muốn đổi số (duration, grace, cancel window), cập nhật seed + ghi chú supersede tại đây; không mở lại câu hỏi dạng “chưa biết”.
