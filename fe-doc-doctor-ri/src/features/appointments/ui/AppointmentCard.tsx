import type { Appointment } from '../domain/types';
import { formatSlotDate, formatSlotTime } from '@shared/lib/formatSlotTime';
import { canFreeCancel } from '@shared/lib/bookingPolicy';
import { Button } from '@shared/ui/Button';
import { Card } from '@shared/ui/Card';
import styles from './AppointmentCard.module.css';

const STATUS_LABELS: Record<string, string> = {
  HELD: 'Giữ chỗ',
  PENDING: 'Chờ xác nhận',
  CONFIRMED: 'Đã xác nhận',
  ATTENDANCE_CONFIRMED: 'Đã xác nhận đến',
  CANCELLED: 'Đã hủy',
  COMPLETED: 'Hoàn thành',
  NO_SHOW: 'Vắng mặt',
};

const ACTIVE = new Set(['HELD', 'PENDING', 'CONFIRMED', 'ATTENDANCE_CONFIRMED']);

type AppointmentCardProps = {
  appointment: Appointment;
  onConfirm?: (id: string) => void;
  onCancel?: (id: string) => void;
  onReschedule?: (id: string) => void;
  isConfirming?: boolean;
  isCancelling?: boolean;
  isRescheduling?: boolean;
};

export function AppointmentCard({
  appointment,
  onConfirm,
  onCancel,
  onReschedule,
  isConfirming,
  isCancelling,
  isRescheduling,
}: AppointmentCardProps) {
  const statusLabel = STATUS_LABELS[appointment.status] ?? appointment.status;
  const freeCancel = canFreeCancel(appointment.startAt);
  const statusClass = appointment.status.toLowerCase().replace(/_/g, '');
  const canAct = ACTIVE.has(appointment.status);

  return (
    <Card>
      <div className={styles.header}>
        <span className={`${styles.status} ${styles[statusClass] ?? ''}`}>
          {statusLabel}
        </span>
        <time className={styles.date} dateTime={appointment.startAt}>
          {formatSlotDate(appointment.startAt)}
        </time>
      </div>

      <h3 className={styles.time}>
        {formatSlotTime(appointment.startAt)}
        {appointment.endAt && ` – ${formatSlotTime(appointment.endAt)}`}
      </h3>

      {appointment.appointmentCode && (
        <p className={styles.detail}>
          <span className={styles.label}>Mã:</span> {appointment.appointmentCode}
        </p>
      )}
      {appointment.serviceName && (
        <p className={styles.detail}>
          <span className={styles.label}>Dịch vụ:</span> {appointment.serviceName}
        </p>
      )}
      {appointment.doctorName && (
        <p className={styles.detail}>
          <span className={styles.label}>Bác sĩ:</span> {appointment.doctorName}
        </p>
      )}
      {appointment.reason && <p className={styles.reason}>{appointment.reason}</p>}

      <div className={styles.actions}>
        {(appointment.status === 'HELD' || appointment.status === 'PENDING') && onConfirm && (
          <Button size="sm" onClick={() => onConfirm(appointment.id)} disabled={isConfirming}>
            {isConfirming ? 'Đang xác nhận...' : 'Xác nhận'}
          </Button>
        )}
        {canAct && onReschedule && (
          <Button
            variant="outline"
            size="sm"
            onClick={() => onReschedule(appointment.id)}
            disabled={isRescheduling}
          >
            Đổi lịch
          </Button>
        )}
        {canAct && onCancel && (
          <Button
            variant="outline"
            size="sm"
            onClick={() => onCancel(appointment.id)}
            disabled={isCancelling}
          >
            {isCancelling ? 'Đang hủy...' : 'Hủy lịch'}
          </Button>
        )}
      </div>

      {!freeCancel && canAct && (
        <p className={styles.note}>
          Lưu ý: Lịch hẹn trong vòng 24 giờ có thể phát sinh phí hủy.
        </p>
      )}
    </Card>
  );
}
