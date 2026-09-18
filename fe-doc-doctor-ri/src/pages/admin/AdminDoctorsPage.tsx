import { useState } from 'react';
import {
  useAdminDoctors,
  useAdminSchedules,
  useAdminServices,
  useCreateSchedule,
  useDeactivateDoctor,
  useDeactivateSchedule,
  useSetDoctorServices,
  useUpsertDoctor,
} from '@features/admin/application/useAdminCatalog';
import type { UpsertDoctorPayload } from '@features/admin/infrastructure/adminApi';
import { Button } from '@shared/ui/Button';
import { Card } from '@shared/ui/Card';
import { Input } from '@shared/ui/Input';
import { PageLoader } from '@shared/ui/Spinner';
import styles from './AdminDoctorsPage.module.css';

const empty: UpsertDoctorPayload = {
  doctorCode: '',
  fullName: '',
  specialty: 'Obstetrics',
  bio: '',
  clinicId: 1,
  userId: null,
  status: 'ACTIVE',
};

const DAY_LABELS = ['', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

export function AdminDoctorsPage() {
  const doctors = useAdminDoctors();
  const services = useAdminServices();
  const upsert = useUpsertDoctor();
  const deactivate = useDeactivateDoctor();
  const setServices = useSetDoctorServices();
  const createSchedule = useCreateSchedule();
  const deactivateSchedule = useDeactivateSchedule();

  const [form, setForm] = useState<UpsertDoctorPayload>(empty);
  const [editId, setEditId] = useState<number | undefined>();
  const [selectedDoctorId, setSelectedDoctorId] = useState<number | null>(null);
  const [servicePick, setServicePick] = useState<number[]>([]);
  const [dow, setDow] = useState(1);
  const [startTime, setStartTime] = useState('08:00');
  const [endTime, setEndTime] = useState('12:00');

  const schedules = useAdminSchedules(selectedDoctorId);

  const set = <K extends keyof UpsertDoctorPayload>(key: K, value: UpsertDoctorPayload[K]) =>
    setForm((f) => ({ ...f, [key]: value }));

  return (
    <div className={styles.page}>
      <header>
        <h1 className={styles.title}>Bác sĩ & lịch làm việc</h1>
        <p className={styles.subtitle}>CRUD bác sĩ, map dịch vụ, quản lý schedule.</p>
      </header>

      <Card>
        <h2 className={styles.section}>{editId ? `Sửa #${editId}` : 'Thêm bác sĩ'}</h2>
        <div className={styles.form}>
          <Input
            label="Mã BS"
            value={form.doctorCode}
            onChange={(e) => set('doctorCode', e.target.value)}
          />
          <Input
            label="Họ tên"
            value={form.fullName}
            onChange={(e) => set('fullName', e.target.value)}
          />
          <Input
            label="Chuyên khoa"
            value={form.specialty}
            onChange={(e) => set('specialty', e.target.value)}
          />
          <Button
            onClick={() =>
              upsert.mutate(
                { id: editId, payload: form },
                {
                  onSuccess: () => {
                    setForm(empty);
                    setEditId(undefined);
                  },
                },
              )
            }
            disabled={upsert.isPending || !form.doctorCode || !form.fullName}
          >
            {editId ? 'Cập nhật' : 'Tạo mới'}
          </Button>
        </div>
        {upsert.isError && <p className={styles.error}>{upsert.error.message}</p>}
      </Card>

      {doctors.isLoading && <PageLoader />}
      {doctors.isError && <p className={styles.error}>{doctors.error.message}</p>}

      <div className={styles.list}>
        {doctors.data?.map((d) => (
          <Card key={d.id}>
            <div className={styles.row}>
              <div>
                <strong>
                  {d.doctorCode} — {d.fullName}
                </strong>
                <p className={styles.meta}>
                  {d.specialty} · {d.status} · services: {d.serviceIds?.join(', ') || '—'}
                </p>
              </div>
              <div className={styles.actions}>
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => {
                    setEditId(d.id);
                    setForm({
                      doctorCode: d.doctorCode,
                      fullName: d.fullName,
                      specialty: d.specialty,
                      bio: d.bio ?? '',
                      clinicId: d.clinicId,
                      userId: d.userId,
                      status: d.status,
                    });
                  }}
                >
                  Sửa
                </Button>
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => {
                    setSelectedDoctorId(d.id);
                    setServicePick(d.serviceIds ?? []);
                  }}
                >
                  Dịch vụ / Lịch
                </Button>
                {d.status === 'ACTIVE' && (
                  <Button size="sm" variant="ghost" onClick={() => deactivate.mutate(d.id)}>
                    Ngưng
                  </Button>
                )}
              </div>
            </div>
          </Card>
        ))}
      </div>

      {selectedDoctorId != null && (
        <Card>
          <h2 className={styles.section}>Bác sĩ #{selectedDoctorId}</h2>
          <p className={styles.meta}>Gán dịch vụ</p>
          <div className={styles.checks}>
            {services.data?.map((s) => (
              <label key={s.id} className={styles.check}>
                <input
                  type="checkbox"
                  checked={servicePick.includes(s.id)}
                  onChange={(e) =>
                    setServicePick((prev) =>
                      e.target.checked
                        ? [...prev, s.id]
                        : prev.filter((id) => id !== s.id),
                    )
                  }
                />
                {s.name}
              </label>
            ))}
          </div>
          <Button
            size="sm"
            onClick={() =>
              setServices.mutate({ id: selectedDoctorId, serviceIds: servicePick })
            }
          >
            Lưu dịch vụ
          </Button>

          <h3 className={styles.sub}>Lịch làm việc</h3>
          {schedules.isLoading && <PageLoader />}
          <ul className={styles.schedules}>
            {schedules.data?.map((sch) => (
              <li key={sch.id}>
                {DAY_LABELS[sch.dayOfWeek]} {sch.startTime}–{sch.endTime} ({sch.status})
                <Button
                  size="sm"
                  variant="ghost"
                  onClick={() => deactivateSchedule.mutate(sch.id)}
                >
                  Xóa
                </Button>
              </li>
            ))}
          </ul>

          <div className={styles.form}>
            <label className={styles.select}>
              <span>Thứ</span>
              <select value={dow} onChange={(e) => setDow(Number(e.target.value))}>
                {[1, 2, 3, 4, 5, 6, 7].map((d) => (
                  <option key={d} value={d}>
                    {DAY_LABELS[d]}
                  </option>
                ))}
              </select>
            </label>
            <Input
              label="Bắt đầu"
              type="time"
              value={startTime}
              onChange={(e) => setStartTime(e.target.value)}
            />
            <Input
              label="Kết thúc"
              type="time"
              value={endTime}
              onChange={(e) => setEndTime(e.target.value)}
            />
            <Button
              size="sm"
              onClick={() =>
                createSchedule.mutate({
                  doctorId: selectedDoctorId,
                  payload: {
                    dayOfWeek: dow,
                    startTime: startTime.length === 5 ? `${startTime}:00` : startTime,
                    endTime: endTime.length === 5 ? `${endTime}:00` : endTime,
                    status: 'ACTIVE',
                  },
                })
              }
            >
              Thêm lịch
            </Button>
          </div>
          {createSchedule.isError && (
            <p className={styles.error}>{createSchedule.error.message}</p>
          )}
        </Card>
      )}
    </div>
  );
}
