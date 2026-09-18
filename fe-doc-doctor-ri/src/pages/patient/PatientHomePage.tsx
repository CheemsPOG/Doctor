import { Link } from 'react-router-dom';
import { getAuthSession } from '@shared/lib/authSession';
import { useAppointments } from '@features/appointments/application/useAppointments';
import { formatSlotDate, formatSlotTime } from '@shared/lib/formatSlotTime';
import { Button } from '@shared/ui/Button';
import { Card } from '@shared/ui/Card';
import { PageLoader } from '@shared/ui/Spinner';
import styles from './PatientHomePage.module.css';

export function PatientHomePage() {
  const session = getAuthSession();
  const { data: appointments, isLoading } = useAppointments(session?.patientId);

  const upcoming = appointments
    ?.filter((a) => a.status !== 'CANCELLED' && a.status !== 'COMPLETED')
    .sort((a, b) => new Date(a.startAt).getTime() - new Date(b.startAt).getTime())[0];

  const greeting = getGreeting();

  return (
    <div className={styles.page}>
      <section className={styles.welcome}>
        <h1 className={styles.greeting}>
          {greeting}, {session?.fullName?.split(' ').slice(-1)[0] ?? 'mẹ bé'}!
        </h1>
        <p className={styles.subtext}>
          Hôm nay bạn muốn làm gì? Chúng tôi luôn sẵn sàng hỗ trợ bạn.
        </p>
      </section>

      <section className={styles.nextAppointment}>
        <h2 className={styles.sectionTitle}>Lịch hẹn sắp tới</h2>
        {isLoading ? (
          <PageLoader />
        ) : upcoming ? (
          <Card>
            <p className={styles.apptDate}>{formatSlotDate(upcoming.startAt)}</p>
            <p className={styles.apptTime}>{formatSlotTime(upcoming.startAt)}</p>
            {upcoming.serviceName && (
              <p className={styles.apptDetail}>{upcoming.serviceName}</p>
            )}
            {upcoming.doctorName && (
              <p className={styles.apptDetail}>Bác sĩ {upcoming.doctorName}</p>
            )}
            <Link to="/patient/appointments" className={styles.viewAll}>
              Xem tất cả lịch hẹn →
            </Link>
          </Card>
        ) : (
          <Card>
            <p className={styles.emptyText}>
              Bạn chưa có lịch hẹn nào. Hãy đặt lịch khám để được chăm sóc tốt nhất nhé!
            </p>
            <Link to="/patient/book">
              <Button>Đặt lịch ngay</Button>
            </Link>
          </Card>
        )}
      </section>

      <section className={styles.quickActions}>
        <h2 className={styles.sectionTitle}>Thao tác nhanh</h2>
        <div className={styles.actionGrid}>
          <Link to="/patient/book" className={styles.actionCard}>
            <span className={styles.actionIcon}>📅</span>
            <span className={styles.actionLabel}>Đặt lịch khám</span>
          </Link>
          <Link to="/patient/services" className={styles.actionCard}>
            <span className={styles.actionIcon}>🩺</span>
            <span className={styles.actionLabel}>Xem dịch vụ</span>
          </Link>
          <Link to="/patient/appointments" className={styles.actionCard}>
            <span className={styles.actionIcon}>📋</span>
            <span className={styles.actionLabel}>Lịch hẹn của tôi</span>
          </Link>
        </div>
      </section>
    </div>
  );
}

function getGreeting(): string {
  const hour = new Date().getHours();
  if (hour < 12) return 'Chào buổi sáng';
  if (hour < 18) return 'Chào buổi chiều';
  return 'Chào buổi tối';
}
