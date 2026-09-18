import { LoginForm } from '@features/auth/ui/LoginForm';
import styles from './AuthPages.module.css';

export function LoginPage() {
  return (
    <div className={styles.page}>
      <h2 className={styles.title}>Chào mừng trở lại</h2>
      <p className={styles.subtitle}>Đăng nhập để xem lịch hẹn và đặt khám mới.</p>
      <LoginForm />
    </div>
  );
}
