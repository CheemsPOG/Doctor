# 01. Phạm vi sản phẩm

## 1.1 Bối cảnh

Doctor Ri Clinic là phòng khám tập trung vào Sản khoa và Phụ khoa. Hệ thống ban đầu phục vụ một cơ sở, nhưng dữ liệu phải hỗ trợ nhiều cơ sở và nhiều chuyên khoa trong tương lai.

## 1.2 Mục tiêu phiên bản đầu

### Bệnh nhân

- Đăng ký, đăng nhập.
- Quản lý hồ sơ cá nhân.
- Xem dịch vụ, bác sĩ và lịch còn trống.
- Đặt, đổi, hủy lịch.
- Xác nhận sẽ đến.
- Nhận thông báo và nhắc lịch.
- Xem lịch sử lịch hẹn.

### Lễ tân

- Tạo lịch hộ bệnh nhân.
- Xác nhận, đổi, hủy lịch.
- Check-in.
- Điều phối phòng và hàng đợi.
- Xử lý bệnh nhân đến sớm, muộn, walk-in.
- Xem cảnh báo trễ và xung đột tài nguyên.

### Bác sĩ

- Xem lịch theo ngày.
- Xem danh sách bệnh nhân đang chờ.
- Bắt đầu và kết thúc lượt khám.
- Ghi chú khám cơ bản và chỉ định tái khám.

### Quản trị viên

- Quản lý dịch vụ, bác sĩ, phòng, thiết bị.
- Quản lý lịch làm việc.
- Quản lý mẫu thông báo.
- Xem audit log và báo cáo vận hành.

## 1.3 Dịch vụ ưu tiên

- Khám thai lần đầu.
- Khám thai định kỳ.
- Siêu âm thai.
- Tư vấn kết quả.
- Khám phụ khoa cơ bản.

## 1.4 Ngoài phạm vi MVP

- Thanh toán online.
- Bảo hiểm y tế.
- Hồ sơ bệnh án điện tử hoàn chỉnh.
- PACS/DICOM.
- Kê đơn điện tử chuẩn quốc gia.
- Tích hợp xét nghiệm bên ngoài.
- Telemedicine.

## 1.5 Nguyên tắc mở rộng

- Không hard-code riêng Sản khoa trong cấu trúc appointment chung.
- Dịch vụ được cấu hình bằng `service`, `resource requirement` và `workflow`.
- Dữ liệu thai kỳ được tách thành module riêng.
- Có thể bổ sung Nhi, Nội, Da liễu mà không thay đổi lõi đặt lịch.
