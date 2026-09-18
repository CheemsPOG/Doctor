# 04. Quy tắc đặt lịch và tài nguyên

## 4.1 Tài nguyên có thể được giữ

- Bác sĩ.
- Phòng khám.
- Máy siêu âm.
- Thiết bị khác.
- Điều dưỡng hoặc kỹ thuật viên.

## 4.2 Hai chiến lược phân phòng

### Phân phòng ngay khi đặt

Dùng cho:

- Siêu âm.
- Thủ thuật.
- Dịch vụ cần thiết bị cố định.

### Phân phòng lúc check-in

Dùng cho:

- Khám tư vấn thông thường.
- Các phòng có chức năng tương đương.

## 4.3 Quy tắc Doctor Ri Clinic

- Luôn giữ bác sĩ ngay khi đặt.
- Khám thường: giữ loại phòng, gán phòng thực tế khi check-in.
- Siêu âm: giữ phòng siêu âm và máy siêu âm ngay khi đặt.
- Mỗi dịch vụ có `duration`, `buffer_before`, `buffer_after`.
- Không cho phép trùng bác sĩ, phòng hoặc thiết bị.

## 4.4 Tính slot khả dụng

```text
Slot khả dụng =
Slot bác sĩ trống
∩ Slot phòng/loại phòng trống
∩ Slot thiết bị trống
∩ Slot nhân sự hỗ trợ trống
```

## 4.5 Chống double booking

- Transaction bắt buộc.
- Pessimistic lock hoặc optimistic lock.
- Unique constraint ở database.
- Không chỉ kiểm tra bằng code ở service.

Ví dụ constraint logic:

```sql
UNIQUE(resource_type, resource_id, start_at, end_at, active_flag)
```

Do MySQL không hỗ trợ tốt kiểm tra khoảng thời gian chồng lấn chỉ bằng unique key, service phải lock các booking liên quan và kiểm tra overlap trong transaction.

## 4.6 Slot hold

Khi bệnh nhân đang hoàn tất đặt lịch:

- Slot giữ tạm **5 phút** (`slot_hold_minutes`, xem [15-open-questions.md](./15-open-questions.md)).
- Trạng thái `HELD`.
- Có `hold_expires_at`.
- Scheduler giải phóng hold hết hạn.
- Hold không được xem là appointment đã xác nhận.
- Hold thành công + hoàn tất form → chuyển `CONFIRMED` ngay (không chờ lễ tân duyệt ở kênh online).

## 4.7 Cửa sổ đặt / hủy (MVP)

- Đặt trước tối đa **30 ngày**.
- Hủy miễn phí nếu còn ≥ **24 giờ** trước giờ hẹn; hủy sát giờ gắn `late_cancel = true`.
- Bệnh nhân chọn bác sĩ cụ thể; hỗ trợ “bác sĩ bất kỳ” khi không truyền `doctorId`.
