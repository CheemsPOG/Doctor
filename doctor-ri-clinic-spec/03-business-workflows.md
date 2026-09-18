# 03. Luồng nghiệp vụ

## 3.1 Đặt lịch online

```mermaid
sequenceDiagram
    actor P as Bệnh nhân
    participant FE as ReactJS
    participant API as Spring Boot
    participant DB as MySQL
    participant N as Notification

    P->>FE: Chọn dịch vụ, bác sĩ, giờ
    FE->>API: GET available-slots
    API-->>FE: Danh sách slot khả dụng
    P->>FE: Xác nhận đặt lịch
    FE->>API: POST appointments
    API->>DB: Lock slot và kiểm tra tài nguyên
    API->>DB: Tạo appointment + resource bookings + outbox
    DB-->>API: Commit
    API-->>FE: Đặt lịch thành công
    N-->>P: Gửi xác nhận
```

## 3.2 Check-in và khám

```mermaid
stateDiagram-v2
    [*] --> CONFIRMED
    CONFIRMED --> ARRIVED: Bệnh nhân đến
    ARRIVED --> CHECKED_IN: Lễ tân xác nhận
    CHECKED_IN --> WAITING_ROOM
    WAITING_ROOM --> READY_FOR_EXAM
    READY_FOR_EXAM --> IN_PROGRESS
    IN_PROGRESS --> COMPLETED
    CONFIRMED --> NO_SHOW
    CONFIRMED --> CANCELLED
```

## 3.3 Đổi lịch

1. Bệnh nhân hoặc lễ tân chọn lịch mới.
2. Hệ thống giữ tài nguyên mới trong transaction.
3. Sau khi giữ thành công, lịch cũ chuyển `RESCHEDULED`.
4. Các reminder cũ bị hủy.
5. Tạo reminder mới.
6. Lưu lịch sử thay đổi và gửi thông báo.

## 3.4 Hủy lịch

1. Kiểm tra quyền hủy và thời hạn hủy.
2. Chuyển trạng thái lịch sang `CANCELLED`.
3. Giải phóng resource booking.
4. Hủy scheduled notifications chưa chạy.
5. Thông báo bệnh nhân và phòng khám.

## 3.5 Walk-in

1. Lễ tân tìm hoặc tạo hồ sơ bệnh nhân.
2. Tạo appointment nguồn `WALK_IN`.
3. Không cần slot online nhưng vẫn phải kiểm tra tài nguyên.
4. Đưa vào queue theo ưu tiên nghiệp vụ.
