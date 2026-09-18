import styles from './Spinner.module.css';

export function Spinner({ label = 'Đang tải...' }: { label?: string }) {
  return (
    <div className={styles.wrapper} role="status" aria-live="polite">
      <div className={styles.spinner} />
      <span className={styles.label}>{label}</span>
    </div>
  );
}

export function PageLoader() {
  return (
    <div className={styles.pageLoader}>
      <Spinner />
    </div>
  );
}
