import { RegisterForm } from '@features/auth/ui/RegisterForm';
import styles from './AuthPages.module.css';

export function RegisterPage() {
  return (
    <div className={styles.page}>
      <h2 className={styles.title}>Tạo tài khoản</h2>
      <p className={styles.subtitle}>
        Bắt đầu hành trình chăm sóc thai kỳ cùng Doctor Ri.
      </p>
      <RegisterForm />
    </div>
  );
}
