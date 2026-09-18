import { useServices } from '@features/services/application/useServices';
import { ServiceCard } from '@features/services/ui/ServiceCard';
import { PageLoader } from '@shared/ui/Spinner';
import { Link } from 'react-router-dom';
import styles from './ServicesPage.module.css';

export function ServicesPage() {
  const { data: services, isLoading, isError, error } = useServices();

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <h1 className={styles.title}>Dịch vụ khám</h1>
        <p className={styles.subtitle}>
          Mô tả chi tiết từng gói khám. Với tư vấn kết quả / siêu âm, có thể xem hình ảnh và video
          đính kèm tại{' '}
          <Link to="/patient/results">Đánh giá kết quả</Link>.
        </p>
      </header>

      {isLoading && <PageLoader />}

      {isError && (
        <p className={styles.error} role="alert">
          Không thể tải danh sách dịch vụ: {error.message}
        </p>
      )}

      {services && (
        <div className={styles.grid}>
          {services.map((service) => (
            <ServiceCard key={service.id} service={service} detailed />
          ))}
        </div>
      )}

      {services?.length === 0 && (
        <p className={styles.empty}>Chưa có dịch vụ nào được cập nhật.</p>
      )}
    </div>
  );
}
