import { useAdminAuditLogs } from '@features/admin/application/useAdminCatalog';
import { Card } from '@shared/ui/Card';
import { PageLoader } from '@shared/ui/Spinner';
import styles from './AdminAuditPage.module.css';

export function AdminAuditPage() {
  const { data, isLoading, isError, error } = useAdminAuditLogs();

  return (
    <div className={styles.page}>
      <header>
        <h1 className={styles.title}>Nhật ký audit</h1>
        <p className={styles.subtitle}>100 sự kiện gần nhất từ hệ thống.</p>
      </header>

      {isLoading && <PageLoader />}
      {isError && (
        <p className={styles.error} role="alert">
          {error.message}
        </p>
      )}

      <div className={styles.list}>
        {data?.map((log) => (
          <Card key={log.id}>
            <strong>
              #{log.id} · {log.action ?? '—'}
            </strong>
            <p className={styles.meta}>
              {log.entityType ?? 'entity'} {log.entityId ?? ''} · actor {log.actorUserId ?? '—'}
              {log.createdAt ? ` · ${new Date(log.createdAt).toLocaleString('vi-VN')}` : ''}
            </p>
          </Card>
        ))}
        {data?.length === 0 && <p className={styles.empty}>Chưa có log.</p>}
      </div>
    </div>
  );
}
