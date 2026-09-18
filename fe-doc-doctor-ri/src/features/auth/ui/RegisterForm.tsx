import { useForm } from 'react-hook-form';
import { Link } from 'react-router-dom';
import { Input } from '@shared/ui/Input';
import { Button } from '@shared/ui/Button';
import { useRegister } from '../application/useAuthMutations';
import styles from './AuthForms.module.css';

type RegisterFormValues = {
  fullName: string;
  email: string;
  phone: string;
  password: string;
};

export function RegisterForm() {
  const { register, handleSubmit, formState: { errors } } = useForm<RegisterFormValues>({
    defaultValues: { fullName: '', email: '', phone: '', password: '' },
  });
  const registerMutation = useRegister();

  const onSubmit = handleSubmit((values) => {
    registerMutation.mutate(values);
  });

  return (
    <form className={styles.form} onSubmit={onSubmit} noValidate>
      <Input
        label="Họ và tên"
        autoComplete="name"
        error={errors.fullName?.message}
        {...register('fullName', { required: 'Vui lòng nhập họ tên' })}
      />
      <Input
        label="Email"
        type="email"
        autoComplete="email"
        error={errors.email?.message}
        {...register('email', {
          required: 'Vui lòng nhập email',
          pattern: { value: /\S+@\S+\.\S+/, message: 'Email không hợp lệ' },
        })}
      />
      <Input
        label="Số điện thoại"
        type="tel"
        autoComplete="tel"
        error={errors.phone?.message}
        {...register('phone', { required: 'Vui lòng nhập số điện thoại' })}
      />
      <Input
        label="Mật khẩu"
        type="password"
        autoComplete="new-password"
        error={errors.password?.message}
        {...register('password', {
          required: 'Vui lòng nhập mật khẩu',
          minLength: { value: 8, message: 'Mật khẩu tối thiểu 8 ký tự' },
        })}
      />

      {registerMutation.isError && (
        <p className={styles.error} role="alert">
          {registerMutation.error.message}
        </p>
      )}

      <Button type="submit" fullWidth disabled={registerMutation.isPending}>
        {registerMutation.isPending ? 'Đang đăng ký...' : 'Tạo tài khoản'}
      </Button>

      <p className={styles.footer}>
        Đã có tài khoản? <Link to="/auth/login">Đăng nhập</Link>
      </p>
    </form>
  );
}
