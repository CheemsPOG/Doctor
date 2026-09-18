import { useForm } from 'react-hook-form';
import { Link } from 'react-router-dom';
import { Input } from '@shared/ui/Input';
import { Button } from '@shared/ui/Button';
import { useLogin } from '../application/useAuthMutations';
import styles from './AuthForms.module.css';

type LoginFormValues = {
  email: string;
  password: string;
};

const DEMO = {
  email: 'mebau@doctorri.local',
  password: 'Password123!',
} as const;

export function LoginForm() {
  const { register, handleSubmit, setValue, formState: { errors } } = useForm<LoginFormValues>({
    defaultValues: { email: '', password: '' },
  });
  const login = useLogin();

  const onSubmit = handleSubmit((values) => {
    login.mutate({
      email: values.email.trim(),
      password: values.password,
    });
  });

  return (
    <form className={styles.form} onSubmit={onSubmit} noValidate>
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
        label="Mật khẩu"
        type="password"
        autoComplete="current-password"
        error={errors.password?.message}
        {...register('password', {
          required: 'Vui lòng nhập mật khẩu',
          minLength: { value: 8, message: 'Mật khẩu tối thiểu 8 ký tự' },
        })}
      />

      <p className={styles.hint}>
        Tài khoản demo: <strong>{DEMO.email}</strong> / <strong>{DEMO.password}</strong>
        {' · '}
        <button
          type="button"
          className={styles.fillDemo}
          onClick={() => {
            setValue('email', DEMO.email, { shouldValidate: true });
            setValue('password', DEMO.password, { shouldValidate: true });
          }}
        >
          Điền sẵn
        </button>
      </p>

      {login.isError && (
        <p className={styles.error} role="alert">
          {login.error.message}
        </p>
      )}

      <Button type="submit" fullWidth disabled={login.isPending}>
        {login.isPending ? 'Đang đăng nhập...' : 'Đăng nhập'}
      </Button>

      <p className={styles.footer}>
        Chưa có tài khoản? <Link to="/auth/register">Đăng ký ngay</Link>
      </p>
    </form>
  );
}
