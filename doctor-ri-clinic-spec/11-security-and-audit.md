# 11. Bảo mật và audit

## 11.1 Authentication

- Access token ngắn hạn.
- Refresh token có thể thu hồi.
- Password hash bằng BCrypt hoặc Argon2.
- Rate limit login và OTP.

## 11.2 Authorization

- RBAC theo vai trò.
- Kiểm tra ownership ở service layer.
- Không dựa chỉ vào việc ẩn nút trên frontend.

## 11.3 Dữ liệu nhạy cảm

- Không ghi chẩn đoán trong push/SMS.
- Không log full request chứa dữ liệu y tế.
- File kết quả không đặt public URL vĩnh viễn.
- Dùng signed URL có thời hạn.

## 11.4 Audit log

Ghi nhận:

- Ai xem hồ sơ.
- Ai sửa hồ sơ.
- Ai đổi/hủy lịch.
- Ai thay đổi queue priority.
- Ai gán lại phòng.
- Ai xuất dữ liệu.

Trường dữ liệu:

- actor_user_id
- action
- resource_type
- resource_id
- old_value
- new_value
- ip_address
- user_agent
- created_at

## 11.5 Idempotency

Các API cần idempotency:

- Tạo appointment.
- Đổi lịch.
- Thanh toán trong tương lai.
- Gửi notification theo event.
