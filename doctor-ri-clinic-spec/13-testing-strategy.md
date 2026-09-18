# 13. Chiến lược kiểm thử

## 13.1 Unit test

- Tính slot khả dụng.
- Kiểm tra overlap.
- Quy tắc hủy/đổi lịch.
- Tính estimated start.
- Notification retry.

## 13.2 Integration test

- Tạo appointment thành công.
- Hai request cùng đặt một slot.
- Đổi lịch và giải phóng slot cũ.
- Outbox được tạo cùng transaction.
- Check-in và gán phòng.

## 13.3 Concurrency test

Kịch bản bắt buộc:

1. Gửi 20 request cùng đặt một slot.
2. Chỉ một request thành công nếu capacity = 1.
3. Các request còn lại trả `SLOT_NOT_AVAILABLE`.

## 13.4 E2E test

- Patient đặt lịch.
- Reception check-in.
- Gán phòng.
- Doctor bắt đầu và hoàn tất khám.
- Patient nhận notification.

## 13.5 Test edge cases

- Bác sĩ nghỉ đột xuất.
- Phòng hỏng.
- Bệnh nhân đến muộn.
- Đổi dịch vụ sau check-in.
- Notification provider timeout.
- Request retry với cùng idempotency key.
