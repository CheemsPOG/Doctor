export type ServiceCategory =
  | 'OBSTETRIC'
  | 'ULTRASOUND'
  | 'CONSULT'
  | 'GYNECOLOGY'
  | string;

export type Service = {
  id: string;
  code: string;
  name: string;
  category: ServiceCategory;
  description?: string;
  durationMinutes?: number;
  holdRoomOnBooking?: boolean;
  requiresUltrasound?: boolean;
  price?: number;
};

export type ServiceContentExtra = {
  highlights: string[];
  preparation: string[];
  notes?: string[];
  supportsResultMedia?: boolean;
};

type ServiceDto = {
  id: number | string;
  code?: string;
  name: string;
  category?: string;
  description?: string | null;
  durationMinutes?: number;
  holdRoomOnBooking?: boolean;
  requiresUltrasound?: boolean;
  price?: number;
};

export function mapService(dto: ServiceDto): Service {
  return {
    id: String(dto.id),
    code: dto.code ?? '',
    name: dto.name,
    category: dto.category ?? '',
    description: dto.description ?? undefined,
    durationMinutes: dto.durationMinutes,
    holdRoomOnBooking: dto.holdRoomOnBooking,
    requiresUltrasound: dto.requiresUltrasound,
    price: dto.price,
  };
}

/** Structured copy keyed by service code (complements API description). */
export const SERVICE_CONTENT: Record<string, ServiceContentExtra> = {
  OB_FIRST: {
    highlights: [
      'Khai thác tiền sử sản khoa và bệnh lý liên quan',
      'Khám lâm sàng, tư vấn dinh dưỡng và lịch theo dõi',
      'Giải đáp thắc mắc lần đầu đến phòng khám',
    ],
    preparation: [
      'Mang theo giấy tờ tùy thân và hồ sơ khám cũ (nếu có)',
      'Ghi chú các thuốc đang dùng / dị ứng',
    ],
  },
  OB_FOLLOWUP: {
    highlights: [
      'Theo dõi cân nặng, huyết áp, tim thai',
      'Đánh giá triệu chứng theo tuần thai',
      'Điều chỉnh kế hoạch chăm sóc nếu cần',
    ],
    preparation: ['Đến đúng giờ hẹn', 'Chuẩn bị câu hỏi muốn trao đổi với bác sĩ'],
  },
  US_OB: {
    highlights: [
      'Siêu âm đánh giá phát triển thai nhi',
      'Quan sát nhịp tim, kích thước và vị trí',
      'Có thể lưu hình/clip vào phần kết quả',
    ],
    preparation: [
      'Uống đủ nước nếu được hướng dẫn trước siêu âm',
      'Mặc trang phục dễ khám vùng bụng',
    ],
    supportsResultMedia: true,
  },
  RESULT_CONSULT: {
    highlights: [
      'Bác sĩ giải thích kết quả siêu âm / xét nghiệm',
      'Thảo luận hướng theo dõi hoặc can thiệp tiếp theo',
      'Xem lại hình ảnh và video đính kèm (nếu có)',
    ],
    preparation: ['Mang theo kết quả giấy (nếu chưa có trên hệ thống)'],
    notes: [
      'Mục Hình ảnh / Video chỉ hiện khi phòng khám đã tải tệp kết quả.',
    ],
    supportsResultMedia: true,
  },
  GYN_BASIC: {
    highlights: [
      'Khám phụ khoa định kỳ, riêng tư',
      'Tầm soát cơ bản và tư vấn triệu chứng',
    ],
    preparation: ['Tránh quan hệ tình dục 24 giờ trước nếu được hướng dẫn'],
  },
};
