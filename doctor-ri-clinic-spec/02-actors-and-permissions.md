# 02. Vai trò và phân quyền

## 2.1 Vai trò

| Vai trò | Mô tả |
|---|---|
| PATIENT | Bệnh nhân sử dụng cổng đặt lịch |
| RECEPTIONIST | Lễ tân và điều phối |
| DOCTOR | Bác sĩ khám bệnh |
| NURSE | Điều dưỡng, hỗ trợ queue và thủ thuật |
| CLINIC_ADMIN | Quản trị vận hành |
| SYSTEM_ADMIN | Quản trị hệ thống |

## 2.2 Quyền chính

| Chức năng | Patient | Receptionist | Doctor | Admin |
|---|---:|---:|---:|---:|
| Xem lịch của bản thân | ✓ | ✓ | ✓ | ✓ |
| Tạo lịch cho bản thân | ✓ |  |  |  |
| Tạo lịch hộ |  | ✓ |  | ✓ |
| Đổi/hủy lịch | Lịch của mình | ✓ | Đề xuất | ✓ |
| Check-in |  | ✓ |  | ✓ |
| Gán phòng |  | ✓ |  | ✓ |
| Xem queue |  | ✓ | ✓ | ✓ |
| Bắt đầu/kết thúc khám |  |  | ✓ |  |
| Quản lý bác sĩ/phòng |  |  |  | ✓ |
| Xem audit log |  |  |  | ✓ |

## 2.3 Nguyên tắc phân quyền dữ liệu

- Bệnh nhân chỉ xem hồ sơ của chính mình.
- Lễ tân chỉ xem thông tin cần cho đặt lịch và tiếp nhận.
- Bác sĩ chỉ xem bệnh nhân được phân công hoặc có quyền chuyên môn tương ứng.
- Quản trị viên không mặc định có quyền xem chi tiết bệnh án.
- Mọi thao tác xem/sửa dữ liệu nhạy cảm phải ghi audit.
