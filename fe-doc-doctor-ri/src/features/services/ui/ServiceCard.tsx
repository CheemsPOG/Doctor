import { Link } from 'react-router-dom';
import type { Service } from '../domain/types';
import { SERVICE_CONTENT } from '../domain/types';
import { Card } from '@shared/ui/Card';
import styles from './ServiceCard.module.css';

const CATEGORY_LABEL: Record<string, string> = {
  OBSTETRIC: 'Sản khoa',
  ULTRASOUND: 'Siêu âm',
  CONSULT: 'Tư vấn',
  GYNECOLOGY: 'Phụ khoa',
};

type ServiceCardProps = {
  service: Service;
  selected?: boolean;
  onSelect?: () => void;
  detailed?: boolean;
};

export function ServiceCard({ service, selected, onSelect, detailed = false }: ServiceCardProps) {
  const extra = SERVICE_CONTENT[service.code];
  const category = CATEGORY_LABEL[service.category] ?? service.category;

  return (
    <Card selected={selected} onClick={onSelect}>
      <div className={styles.content}>
        <div className={styles.top}>
          {category && <span className={styles.category}>{category}</span>}
          {service.requiresUltrasound && <span className={styles.flag}>Có siêu âm</span>}
        </div>
        <h3 className={styles.name}>{service.name}</h3>
        {service.description && <p className={styles.description}>{service.description}</p>}

        {detailed && extra && (
          <>
            <div className={styles.block}>
              <h4>Nội dung buổi khám</h4>
              <ul>
                {extra.highlights.map((item) => (
                  <li key={item}>{item}</li>
                ))}
              </ul>
            </div>
            <div className={styles.block}>
              <h4>Chuẩn bị trước khi đến</h4>
              <ul>
                {extra.preparation.map((item) => (
                  <li key={item}>{item}</li>
                ))}
              </ul>
            </div>
            {extra.notes?.map((note) => (
              <p key={note} className={styles.note}>
                {note}
              </p>
            ))}
            {extra.supportsResultMedia && (
              <p className={styles.mediaHint}>
                Kết quả có thể kèm <strong>hình ảnh / video</strong> — xem tại{' '}
                <Link to="/patient/results" onClick={(e) => e.stopPropagation()}>
                  Đánh giá kết quả
                </Link>
                .
              </p>
            )}
          </>
        )}

        <div className={styles.meta}>
          {service.durationMinutes != null && (
            <span className={styles.badge}>{service.durationMinutes} phút</span>
          )}
          {service.price != null && (
            <span className={styles.price}>
              {service.price.toLocaleString('vi-VN')}đ
            </span>
          )}
        </div>

        {!onSelect && (
          <div className={styles.actions}>
            <Link className={styles.bookLink} to={`/patient/book?serviceId=${service.id}`}>
              Đặt lịch dịch vụ này →
            </Link>
          </div>
        )}
      </div>
    </Card>
  );
}
