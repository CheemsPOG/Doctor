# 06. Trường hợp ngách

## 6.1 Bác sĩ khám quá giờ

- Đánh dấu lịch sau có nguy cơ trễ.
- Tự tính lại giờ dự kiến.
- Cảnh báo lễ tân.
- Không thay đổi giờ hẹn gốc.

## 6.2 Bệnh nhân đến sớm

- Cho check-in.
- Đưa vào queue.
- Chỉ gọi sớm khi bác sĩ, phòng và tài nguyên đều trống.

## 6.3 Bệnh nhân đến muộn

- Cấu hình grace period, mặc định 15 phút.
- Trong grace period: cho check-in nhưng có thể phải chờ.
- Quá grace period: lễ tân chọn chèn cuối buổi, đổi slot hoặc đánh dấu no-show.

## 6.4 Bác sĩ nghỉ đột xuất

- Khóa slot còn trống.
- Tìm bác sĩ thay thế.
- Tạo đề xuất đổi lịch.
- Chờ bệnh nhân xác nhận.
- Không tự ý chuyển mà không ghi nhận.

## 6.5 Phòng hoặc máy siêu âm hỏng

- Đánh dấu `OUT_OF_SERVICE`.
- Ngừng trả slot liên quan.
- Quét các lịch tương lai bị ảnh hưởng.
- Đề xuất phòng/thiết bị thay thế.

## 6.6 Bệnh nhân đặt trùng hai lịch

- Kiểm tra overlap theo patient_id.
- Cảnh báo hoặc chặn nếu không đủ thời gian di chuyển.

## 6.7 Lễ tân và bệnh nhân cùng đặt một slot

- Lock trong database.
- Một request thành công, request còn lại nhận `SLOT_NOT_AVAILABLE`.

## 6.8 Đổi dịch vụ sau khi đến

- Không ghi đè dịch vụ cũ.
- Tạo appointment activity mới.
- Tìm tài nguyên tương ứng.
- Đưa vào queue mới nếu cần.

## 6.9 Trường hợp cần đánh giá y tế ngay

- Hệ thống chỉ cho phép nhân viên có quyền đánh dấu `URGENT_REVIEW`.
- Gửi cảnh báo realtime cho lễ tân và bác sĩ.
- Mọi thay đổi thứ tự phải được audit.
- Hệ thống không tự đưa ra quyết định chẩn đoán.

## 6.10 Mất kết nối khi đặt lịch

- Client gửi `idempotency-key`.
- Backend trả lại kết quả cũ nếu request đã được xử lý.
- Tránh tạo hai appointment khi người dùng bấm lại.

## 6.11 Notification gửi lỗi

- Appointment vẫn hợp lệ.
- Delivery chuyển `FAILED`.
- Retry theo exponential backoff.
- Cảnh báo lễ tân sau số lần retry tối đa.
