import { Link, Outlet } from 'react-router-dom';
import styles from './AuthLayout.module.css';

export function AuthLayout() {
  return (
    <div className={styles.layout}>
      <div className={styles.decor1} aria-hidden="true" />
      <div className={styles.decor2} aria-hidden="true" />

      <div className={styles.container}>
        <Link to="/" className={styles.brand}>
          Doctor Ri
        </Link>
        <p className={styles.tagline}>Cổng thông tin bệnh nhân</p>

        <div className={styles.card}>
          <Outlet />
        </div>

        <Link to="/" className={styles.backLink}>
          ← Về trang chủ
        </Link>
      </div>
    </div>
  );
}
