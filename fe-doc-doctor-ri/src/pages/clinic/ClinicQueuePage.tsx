import { useMemo, useState } from 'react';
import {
  useAssignRoom,
  useCheckIn,
  useClinicAppointments,
  useClinicQueue,
  useClinicRooms,
  useMarkNoShow,
  useQueueCall,
  useQueueComplete,
  useQueueStart,
  useReprioritize,
} from '@features/clinic/application/useClinicOps';
import { Button } from '@shared/ui/Button';
import { Card } from '@shared/ui/Card';
import { Input } from '@shared/ui/Input';
import { PageLoader } from '@shared/ui/Spinner';
import { formatSlotTime } from '@shared/lib/formatSlotTime';
import styles from './ClinicQueuePage.module.css';

function todayLocalIso() {
  const d = new Date();
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

export function ClinicQueuePage() {
  const [date, setDate] = useState(todayLocalIso);
  const [urgent, setUrgent] = useState(false);

  const appointments = useClinicAppointments(date);
  const { data, isLoading, isError, error, refetch } = useClinicQueue(date);
  const rooms = useClinicRooms();
  const checkIn = useCheckIn();
  const assignRoom = useAssignRoom();
  const noShow = useMarkNoShow();
  const callQ = useQueueCall();
  const startQ = useQueueStart();
  const completeQ = useQueueComplete();
  const reprio = useReprioritize();

  const sorted = useMemo(
    () =>
      data
        ?.slice()
        .sort((a, b) => a.priority - b.priority || Number(a.queueNumber) - Number(b.queueNumber)),
    [data],
  );

  const dayAppointments = useMemo(
    () =>
      appointments.data
        ?.slice()
        .sort((a, b) => new Date(a.startAt).getTime() - new Date(b.startAt).getTime()),
    [appointments.data],
  );

  const defaultRoomId = rooms.data?.[0]?.id;
  const refresh = () => {
    void refetch();
    void appointments.refetch();
  };

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <div>
          <h1 className={styles.title}>Lễ tân — lịch & hàng đợi</h1>
          <p className={styles.subtitle}>
            Xem lịch trong ngày, check-in, rồi điều phối hàng đợi.
          </p>
        </div>
        <div className={styles.toolbar}>
          <Input
            label="Ngày"
            type="date"
            value={date}
            onChange={(e) => setDate(e.target.value)}
          />
          <Button variant="outline" size="sm" onClick={refresh}>
            Làm mới
          </Button>
        </div>
      </header>

      <section>
        <h2 className={styles.sectionTitle}>Lịch hẹn trong ngày</h2>
        {(appointments.isLoading || isLoading) && <PageLoader />}
        {appointments.isError && (
          <p className={styles.error} role="alert">
            {appointments.error.message}
          </p>
        )}
        <div className={styles.list}>
          {dayAppointments?.map((appt) => {
            const canCheckIn =
              !appt.checkedIn &&
              (appt.status === 'CONFIRMED' || appt.status === 'ATTENDANCE_CONFIRMED');
            return (
              <Card key={appt.id}>
                <div className={styles.itemHead}>
                  <strong>
                    {formatSlotTime(appt.startAt)} · {appt.appointmentCode || `#${appt.id}`}
                  </strong>
                  <span className={styles.badge}>
                    {appt.checkedIn ? 'ĐÃ CHECK-IN' : appt.status}
                  </span>
                </div>
                <p className={styles.meta}>
                  BN {appt.patientId} · BS {appt.doctorId} · DV {appt.serviceId}
                  {appt.source ? ` · ${appt.source}` : ''}
                  {appt.reason ? ` · ${appt.reason}` : ''}
                </p>
                <div className={styles.actions}>
                  {canCheckIn && (
                    <Button
                      size="sm"
                      disabled={checkIn.isPending}
                      onClick={() =>
                        checkIn.mutate({
                          appointmentId: String(appt.id),
                          medicalUrgent: urgent,
                        })
                      }
                    >
                      Check-in
                    </Button>
                  )}
                  {canCheckIn && (
                    <label className={styles.checkInline}>
                      <input
                        type="checkbox"
                        checked={urgent}
                        onChange={(e) => setUrgent(e.target.checked)}
                      />
                      Ưu tiên y tế
                    </label>
                  )}
                  {!appt.checkedIn && appt.status === 'CONFIRMED' && (
                    <Button
                      size="sm"
                      variant="ghost"
                      onClick={() => noShow.mutate(String(appt.id))}
                    >
                      No-show
                    </Button>
                  )}
                </div>
              </Card>
            );
          })}
          {dayAppointments?.length === 0 && (
            <p className={styles.empty}>Không có lịch hẹn trong ngày này.</p>
          )}
        </div>
        {checkIn.isError && (
          <p className={styles.error} role="alert">
            {checkIn.error.message}
          </p>
        )}
      </section>

      <section>
        <h2 className={styles.sectionTitle}>Hàng đợi (đã check-in)</h2>
        {isError && (
          <p className={styles.error} role="alert">
            {error.message}
          </p>
        )}
        <div className={styles.list}>
          {sorted?.map((item) => (
            <Card key={item.id}>
              <div className={styles.itemHead}>
                <strong>
                  #{item.queueNumber} · ưu tiên {item.priority}
                </strong>
                <span className={styles.badge}>{item.status}</span>
              </div>
              <p className={styles.meta}>
                Appointment {item.appointmentId}
                {item.assignedRoomId ? ` · phòng ${item.assignedRoomId}` : ''}
              </p>
              <div className={styles.actions}>
                <Button size="sm" onClick={() => callQ.mutate(item.id)}>
                  Gọi
                </Button>
                <Button size="sm" variant="outline" onClick={() => startQ.mutate(item.id)}>
                  Bắt đầu
                </Button>
                <Button size="sm" variant="outline" onClick={() => completeQ.mutate(item.id)}>
                  Hoàn tất
                </Button>
                {defaultRoomId && (
                  <Button
                    size="sm"
                    variant="ghost"
                    onClick={() =>
                      assignRoom.mutate({
                        appointmentId: String(item.appointmentId),
                        roomId: defaultRoomId,
                      })
                    }
                  >
                    Gán phòng
                  </Button>
                )}
                <Button
                  size="sm"
                  variant="ghost"
                  onClick={() =>
                    reprio.mutate({
                      queueId: item.id,
                      priority: 10,
                      reason: 'Reception urgent bump',
                    })
                  }
                >
                  Ưu tiên
                </Button>
                <Button
                  size="sm"
                  variant="ghost"
                  onClick={() => noShow.mutate(String(item.appointmentId))}
                >
                  No-show
                </Button>
              </div>
            </Card>
          ))}
          {sorted?.length === 0 && (
            <p className={styles.empty}>Chưa có bệnh nhân trong hàng đợi — hãy check-in lịch ở trên.</p>
          )}
        </div>
      </section>
    </div>
  );
}
