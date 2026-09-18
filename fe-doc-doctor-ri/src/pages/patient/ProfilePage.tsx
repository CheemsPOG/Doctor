import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import {
  usePatientProfile,
  useUpdatePatientProfile,
} from '@features/patients/application/usePatientProfile';
import { getAuthSession, setAuthSession } from '@shared/lib/authSession';
import { Button } from '@shared/ui/Button';
import { Input } from '@shared/ui/Input';
import { PageLoader } from '@shared/ui/Spinner';
import styles from './ProfilePage.module.css';

type FormValues = {
  fullName: string;
  dateOfBirth: string;
  gender: string;
  phone: string;
  email: string;
  emergencyContactName: string;
  emergencyContactPhone: string;
};

export function ProfilePage() {
  const { data, isLoading, isError, error } = usePatientProfile();
  const update = useUpdatePatientProfile();
  const { register, handleSubmit, reset, formState: { errors, isDirty } } = useForm<FormValues>();

  useEffect(() => {
    if (!data) return;
    reset({
      fullName: data.fullName ?? '',
      dateOfBirth: data.dateOfBirth ?? '',
      gender: data.gender ?? '',
      phone: data.phone ?? '',
      email: data.email ?? '',
      emergencyContactName: data.emergencyContactName ?? '',
      emergencyContactPhone: data.emergencyContactPhone ?? '',
    });
  }, [data, reset]);

  const onSubmit = handleSubmit((values) => {
    update.mutate(
      {
        fullName: values.fullName,
        dateOfBirth: values.dateOfBirth || null,
        gender: values.gender || null,
        phone: values.phone || null,
        email: values.email || null,
        emergencyContactName: values.emergencyContactName || null,
        emergencyContactPhone: values.emergencyContactPhone || null,
      },
      {
        onSuccess: (profile) => {
          const session = getAuthSession();
          if (session) {
            setAuthSession({ ...session, fullName: profile.fullName });
          }
        },
      },
    );
  });

  if (isLoading) return <PageLoader />;
  if (isError) {
    return (
      <p className={styles.error} role="alert">
        Không tải được hồ sơ: {error.message}
      </p>
    );
  }

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <h1 className={styles.title}>Hồ sơ cá nhân</h1>
        <p className={styles.subtitle}>
          Mã bệnh nhân <strong>{data?.patientCode}</strong>
        </p>
      </header>

      <form className={styles.form} onSubmit={onSubmit} noValidate>
        <Input
          label="Họ và tên"
          error={errors.fullName?.message}
          {...register('fullName', { required: 'Vui lòng nhập họ tên' })}
        />
        <Input label="Ngày sinh" type="date" {...register('dateOfBirth')} />
        <Input label="Giới tính" placeholder="FEMALE / MALE" {...register('gender')} />
        <Input label="Số điện thoại" {...register('phone')} />
        <Input label="Email liên hệ" type="email" {...register('email')} />
        <Input label="Người liên hệ khẩn" {...register('emergencyContactName')} />
        <Input label="SĐT người liên hệ" {...register('emergencyContactPhone')} />

        {update.isError && (
          <p className={styles.error} role="alert">
            {update.error.message}
          </p>
        )}
        {update.isSuccess && !isDirty && (
          <p className={styles.success} role="status">
            Đã lưu hồ sơ.
          </p>
        )}

        <Button type="submit" disabled={update.isPending || !isDirty}>
          {update.isPending ? 'Đang lưu...' : 'Lưu thay đổi'}
        </Button>
      </form>
    </div>
  );
}
