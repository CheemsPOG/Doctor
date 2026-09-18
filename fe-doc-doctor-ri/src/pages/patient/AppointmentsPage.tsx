import { useMemo, useState } from 'react';
import { getAuthSession } from '@shared/lib/authSession';
import {
  useAppointments,
  useAvailabilitySlots,
  useCancelAppointment,
  useConfirmAppointment,
  useRescheduleAppointment,
} from '@features/appointments/application/useAppointments';
import { AppointmentCard } from '@features/appointments/ui/AppointmentCard';
import { Button } from '@shared/ui/Button';
import { Card } from '@shared/ui/Card';
import { Input } from '@shared/ui/Input';
import { PageLoader } from '@shared/ui/Spinner';
import { formatSlotTime } from '@shared/lib/formatSlotTime';
import styles from './AppointmentsPage.module.css';

export function AppointmentsPage() {
  const session = getAuthSession();
  const { data: appointments, isLoading, isError, error } = useAppointments(session?.patientId);
  const confirmMutation = useConfirmAppointment();
  const cancelMutation = useCancelAppointment();
  const rescheduleMutation = useRescheduleAppointment();

  const [rescheduleId, setRescheduleId] = useState<string | null>(null);
  const [date, setDate] = useState('');

  const target = useMemo(
    () => appointments?.find((a) => a.id === rescheduleId),
    [appointments, rescheduleId],
  );

  const slotParams =
    target && date
      ? { serviceId: target.serviceId, doctorId: target.doctorId, date }
      : null;
  const { data: availability, isLoading: loadingSlots } = useAvailabilitySlots(slotParams);

  const sorted = appointments
    ?.slice()
    .sort((a, b) => new Date(b.startAt).getTime() - new Date(a.startAt).getTime());

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <h1 className={styles.title}>Lịch hẹn của tôi</h1>
        <p className={styles.subtitle}>
          Quản lý, xác nhận, đổi lịch hoặc hủy các lịch khám đã đặt.
        </p>
      </header>

      {isLoading && <PageLoader />}

      {isError && (
        <p className={styles.error} role="alert">
          Không thể tải lịch hẹn: {error.message}
        </p>
      )}

      {rescheduleId && target && (
        <Card>
          <h2 className={styles.panelTitle}>Đổi lịch — mã {target.appointmentCode ?? target.id}</h2>
          <Input
            label="Ngày mới"
            type="date"
            value={date}
            onChange={(e) => setDate(e.target.value)}
          />
          {loadingSlots && <PageLoader />}
          <div className={styles.slots}>
            {availability?.slots
              .filter((s) => s.available)
              .map((slot) => (
                <Button
                  key={slot.slotKey}
                  size="sm"
                  variant="outline"
                  disabled={rescheduleMutation.isPending}
                  onClick={() =>
                    rescheduleMutation.mutate(
                      { id: rescheduleId, payload: { startAt: slot.startAt } },
                      {
                        onSuccess: () => {
                          setRescheduleId(null);
                          setDate('');
                        },
                      },
                    )
                  }
                >
                  {formatSlotTime(slot.startAt)}
                </Button>
              ))}
          </div>
          {rescheduleMutation.isError && (
            <p className={styles.error} role="alert">
              {rescheduleMutation.error.message}
            </p>
          )}
          <Button
            variant="ghost"
            size="sm"
            onClick={() => {
              setRescheduleId(null);
              setDate('');
            }}
          >
            Đóng
          </Button>
        </Card>
      )}

      {sorted && sorted.length > 0 && (
        <div className={styles.list}>
          {sorted.map((appointment) => (
            <AppointmentCard
              key={appointment.id}
              appointment={appointment}
              onConfirm={(id) => confirmMutation.mutate(id)}
              onCancel={(id) => cancelMutation.mutate(id)}
              onReschedule={(id) => {
                setRescheduleId(id);
                setDate(appointment.startAt.slice(0, 10));
              }}
              isConfirming={
                confirmMutation.isPending && confirmMutation.variables === appointment.id
              }
              isCancelling={
                cancelMutation.isPending && cancelMutation.variables === appointment.id
              }
              isRescheduling={rescheduleId === appointment.id}
            />
          ))}
        </div>
      )}

      {sorted?.length === 0 && (
        <p className={styles.empty}>Bạn chưa có lịch hẹn nào.</p>
      )}
    </div>
  );
}
