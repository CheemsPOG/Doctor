import { useState } from 'react';
import { useCreateReceptionAppointment } from '@features/clinic/application/useClinicOps';
import { useDoctors } from '@features/doctors/application/useDoctors';
import { useServices } from '@features/services/application/useServices';
import {
  useAvailabilitySlots,
} from '@features/appointments/application/useAppointments';
import { Button } from '@shared/ui/Button';
import { Card } from '@shared/ui/Card';
import { Input } from '@shared/ui/Input';
import { PageLoader } from '@shared/ui/Spinner';
import { formatSlotTime } from '@shared/lib/formatSlotTime';
import styles from './ClinicWalkInPage.module.css';

export function ClinicWalkInPage() {
  const [patientId, setPatientId] = useState('');
  const [serviceId, setServiceId] = useState('');
  const [doctorId, setDoctorId] = useState('');
  const [date, setDate] = useState(new Date().toISOString().slice(0, 10));
  const [reason, setReason] = useState('Walk-in');
  const [startAt, setStartAt] = useState('');

  const { data: services } = useServices();
  const { data: doctors } = useDoctors();
  const slots = useAvailabilitySlots(
    serviceId && doctorId && date ? { serviceId, doctorId, date } : null,
  );
  const create = useCreateReceptionAppointment();

  const submit = () => {
    if (!patientId || !serviceId || !doctorId || !startAt) return;
    create.mutate({
      patientId: Number(patientId),
      serviceId: Number(serviceId),
      doctorId: Number(doctorId),
      startAt,
      reason,
      walkIn: true,
    });
  };

  return (
    <div className={styles.page}>
      <header>
        <h1 className={styles.title}>Đăng ký walk-in</h1>
        <p className={styles.subtitle}>Tạo lịch ưu tiên walk-in cho bệnh nhân tại quầy.</p>
      </header>

      <Card>
        <div className={styles.form}>
          <Input
            label="Patient ID"
            value={patientId}
            onChange={(e) => setPatientId(e.target.value)}
          />
          <label className={styles.select}>
            <span>Dịch vụ</span>
            <select value={serviceId} onChange={(e) => setServiceId(e.target.value)}>
              <option value="">Chọn...</option>
              {services?.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name}
                </option>
              ))}
            </select>
          </label>
          <label className={styles.select}>
            <span>Bác sĩ</span>
            <select value={doctorId} onChange={(e) => setDoctorId(e.target.value)}>
              <option value="">Chọn...</option>
              {doctors?.map((d) => (
                <option key={d.id} value={d.id}>
                  {d.fullName}
                </option>
              ))}
            </select>
          </label>
          <Input label="Ngày" type="date" value={date} onChange={(e) => setDate(e.target.value)} />
          <Input
            label="Lý do"
            value={reason}
            onChange={(e) => setReason(e.target.value)}
          />

          {slots.isLoading && <PageLoader />}
          <div className={styles.slots}>
            {slots.data?.slots
              .filter((s) => s.available)
              .map((s) => (
                <Button
                  key={s.slotKey}
                  size="sm"
                  variant={startAt === s.startAt ? 'primary' : 'outline'}
                  onClick={() => setStartAt(s.startAt)}
                >
                  {formatSlotTime(s.startAt)}
                </Button>
              ))}
          </div>

          {create.isError && (
            <p className={styles.error} role="alert">
              {create.error.message}
            </p>
          )}
          {create.isSuccess && (
            <p className={styles.success} role="status">
              Đã tạo walk-in thành công.
            </p>
          )}

          <Button onClick={submit} disabled={create.isPending || !startAt}>
            {create.isPending ? 'Đang tạo...' : 'Tạo walk-in'}
          </Button>
        </div>
      </Card>
    </div>
  );
}
