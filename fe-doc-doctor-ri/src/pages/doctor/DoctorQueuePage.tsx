import { useDoctorQueue } from '@features/doctor-portal/application/useDoctorPortal';
import { useStartExam } from '@features/doctor-portal/application/useDoctorPortal';
import { Button } from '@shared/ui/Button';
import { Card } from '@shared/ui/Card';
import { PageLoader } from '@shared/ui/Spinner';
import styles from './DoctorQueuePage.module.css';

export function DoctorQueuePage() {
  const { data, isLoading, isError, error, refetch } = useDoctorQueue();
  const startExam = useStartExam();

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <div>
          <h1 className={styles.title}>Hàng đợi của tôi</h1>
          <p className={styles.subtitle}>Bệnh nhân đã check-in đang chờ khám.</p>
        </div>
        <Button variant="outline" size="sm" onClick={() => void refetch()}>
          Làm mới
        </Button>
      </header>

      {isLoading && <PageLoader />}
      {isError && (
        <p className={styles.error} role="alert">
          {error.message}
        </p>
      )}

      <div className={styles.list}>
        {data?.map((item) => (
          <Card key={item.id}>
            <div className={styles.itemHead}>
              <strong>
                #{item.queueNumber} · P{item.priority}
              </strong>
              <span className={styles.badge}>{item.status}</span>
            </div>
            <p className={styles.meta}>Appointment {item.appointmentId}</p>
            <Button size="sm" onClick={() => startExam.mutate(String(item.appointmentId))}>
              Bắt đầu khám
            </Button>
          </Card>
        ))}
        {data?.length === 0 && <p className={styles.empty}>Hàng đợi trống.</p>}
      </div>
    </div>
  );
}
