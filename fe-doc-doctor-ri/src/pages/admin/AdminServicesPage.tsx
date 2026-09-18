import { useState } from 'react';
import {
  useAdminServices,
  useDeactivateService,
  useUpsertService,
} from '@features/admin/application/useAdminCatalog';
import type { UpsertServicePayload } from '@features/admin/infrastructure/adminApi';
import { Button } from '@shared/ui/Button';
import { Card } from '@shared/ui/Card';
import { Input } from '@shared/ui/Input';
import { PageLoader } from '@shared/ui/Spinner';
import styles from './AdminServicesPage.module.css';

const empty: UpsertServicePayload = {
  code: '',
  name: '',
  category: 'OBSTETRIC',
  description: '',
  durationMinutes: 30,
  bufferBeforeMinutes: 0,
  bufferAfterMinutes: 5,
  holdRoomOnBooking: false,
  requiresUltrasound: false,
  bookingPolicy: 'STANDARD',
  status: 'ACTIVE',
};

export function AdminServicesPage() {
  const { data, isLoading, isError, error } = useAdminServices();
  const upsert = useUpsertService();
  const deactivate = useDeactivateService();
  const [form, setForm] = useState<UpsertServicePayload>(empty);
  const [editId, setEditId] = useState<number | undefined>();

  const set = <K extends keyof UpsertServicePayload>(key: K, value: UpsertServicePayload[K]) =>
    setForm((f) => ({ ...f, [key]: value }));

  const submit = () => {
    upsert.mutate(
      { id: editId, payload: form },
      {
        onSuccess: () => {
          setForm(empty);
          setEditId(undefined);
        },
      },
    );
  };

  return (
    <div className={styles.page}>
      <header>
        <h1 className={styles.title}>Dịch vụ</h1>
        <p className={styles.subtitle}>CRUD catalog dịch vụ khám.</p>
      </header>

      <Card>
        <h2 className={styles.section}>{editId ? `Sửa #${editId}` : 'Thêm dịch vụ'}</h2>
        <div className={styles.form}>
          <Input label="Mã" value={form.code} onChange={(e) => set('code', e.target.value)} />
          <Input label="Tên" value={form.name} onChange={(e) => set('name', e.target.value)} />
          <Input
            label="Nhóm"
            value={form.category}
            onChange={(e) => set('category', e.target.value)}
          />
          <label className={styles.textareaField}>
            <span>Mô tả chi tiết</span>
            <textarea
              rows={4}
              value={form.description ?? ''}
              onChange={(e) => set('description', e.target.value)}
              placeholder="Nội dung buổi khám, lưu ý cho bệnh nhân..."
            />
          </label>
          <Input
            label="Thời lượng (phút)"
            type="number"
            value={String(form.durationMinutes)}
            onChange={(e) => set('durationMinutes', Number(e.target.value))}
          />
          <Input
            label="Buffer trước"
            type="number"
            value={String(form.bufferBeforeMinutes)}
            onChange={(e) => set('bufferBeforeMinutes', Number(e.target.value))}
          />
          <Input
            label="Buffer sau"
            type="number"
            value={String(form.bufferAfterMinutes)}
            onChange={(e) => set('bufferAfterMinutes', Number(e.target.value))}
          />
          <label className={styles.check}>
            <input
              type="checkbox"
              checked={form.requiresUltrasound}
              onChange={(e) => set('requiresUltrasound', e.target.checked)}
            />
            Cần siêu âm
          </label>
          <Button onClick={submit} disabled={upsert.isPending || !form.code || !form.name}>
            {editId ? 'Cập nhật' : 'Tạo mới'}
          </Button>
          {editId && (
            <Button
              variant="ghost"
              onClick={() => {
                setEditId(undefined);
                setForm(empty);
              }}
            >
              Huỷ sửa
            </Button>
          )}
        </div>
        {upsert.isError && <p className={styles.error}>{upsert.error.message}</p>}
      </Card>

      {isLoading && <PageLoader />}
      {isError && <p className={styles.error}>{error.message}</p>}

      <div className={styles.list}>
        {data?.map((s) => (
          <Card key={s.id}>
            <div className={styles.row}>
              <div>
                <strong>
                  {s.code} — {s.name}
                </strong>
                <p className={styles.meta}>
                  {s.category} · {s.durationMinutes} phút · {s.status}
                </p>
              </div>
              <div className={styles.actions}>
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => {
                    setEditId(s.id);
                    setForm({
                      code: s.code,
                      name: s.name,
                      category: s.category,
                      description: s.description ?? '',
                      durationMinutes: s.durationMinutes,
                      bufferBeforeMinutes: s.bufferBeforeMinutes,
                      bufferAfterMinutes: s.bufferAfterMinutes,
                      holdRoomOnBooking: s.holdRoomOnBooking,
                      requiresUltrasound: s.requiresUltrasound,
                      bookingPolicy: s.bookingPolicy ?? 'STANDARD',
                      status: s.status,
                    });
                  }}
                >
                  Sửa
                </Button>
                {s.status === 'ACTIVE' && (
                  <Button
                    size="sm"
                    variant="ghost"
                    onClick={() => deactivate.mutate(s.id)}
                  >
                    Ngưng
                  </Button>
                )}
              </div>
            </div>
          </Card>
        ))}
      </div>
    </div>
  );
}
