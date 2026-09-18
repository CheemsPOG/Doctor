import { useState } from 'react';
import {
  useAddActivity,
  useCompleteExam,
  useDoctorSchedule,
  useStartExam,
} from '@features/doctor-portal/application/useDoctorPortal';
import { Button } from '@shared/ui/Button';
import { Card } from '@shared/ui/Card';
import { Input } from '@shared/ui/Input';
import { PageLoader } from '@shared/ui/Spinner';
import { formatSlotDate, formatSlotTime } from '@shared/lib/formatSlotTime';
import styles from './DoctorSchedulePage.module.css';

export function DoctorSchedulePage() {
  const [date, setDate] = useState(new Date().toISOString().slice(0, 10));
  const [summary, setSummary] = useState('');
  const [activeId, setActiveId] = useState<string | null>(null);
  const [note, setNote] = useState('');

  const { data, isLoading, isError, error } = useDoctorSchedule(date);
  const startExam = useStartExam();
  const completeExam = useCompleteExam();
  const addActivity = useAddActivity();

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <div>
          <h1 className={styles.title}>Lịch khám hôm nay</h1>
          <p className={styles.subtitle}>Bắt đầu / kết thúc khám và ghi hoạt động.</p>
        </div>
        <Input label="Ngày" type="date" value={date} onChange={(e) => setDate(e.target.value)} />
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
                {formatSlotTime(item.startAt)} – {formatSlotTime(item.endAt)}
              </strong>
              <span className={styles.badge}>{item.status}</span>
            </div>
            <p className={styles.meta}>
              {item.appointmentCode} · BN {item.patientId} · {formatSlotDate(item.startAt)}
            </p>
            <div className={styles.actions}>
              <Button size="sm" onClick={() => startExam.mutate(String(item.id))}>
                Bắt đầu khám
              </Button>
              <Button
                size="sm"
                variant="outline"
                onClick={() => setActiveId(String(item.id))}
              >
                Kết thúc / Ghi chú
              </Button>
            </div>

            {activeId === String(item.id) && (
              <div className={styles.panel}>
                <Input
                  label="Tóm tắt khám"
                  value={summary}
                  onChange={(e) => setSummary(e.target.value)}
                />
                <Button
                  size="sm"
                  disabled={!summary.trim() || completeExam.isPending}
                  onClick={() =>
                    completeExam.mutate(
                      { appointmentId: String(item.id), summary },
                      {
                        onSuccess: () => {
                          setSummary('');
                          setActiveId(null);
                        },
                      },
                    )
                  }
                >
                  Hoàn tất khám
                </Button>
                <Input
                  label="Ghi hoạt động"
                  value={note}
                  onChange={(e) => setNote(e.target.value)}
                  placeholder="VD: Siêu âm, tư vấn..."
                />
                <Button
                  size="sm"
                  variant="outline"
                  disabled={!note.trim() || addActivity.isPending}
                  onClick={() =>
                    addActivity.mutate(
                      {
                        appointmentId: String(item.id),
                        activityType: 'NOTE',
                        note,
                      },
                      { onSuccess: () => setNote('') },
                    )
                  }
                >
                  Thêm hoạt động
                </Button>
              </div>
            )}
          </Card>
        ))}
        {data?.length === 0 && <p className={styles.empty}>Không có lịch trong ngày.</p>}
      </div>
    </div>
  );
}
