import { Link } from 'react-router-dom';
import {
  useMarkAllNotificationsRead,
  useMarkNotificationRead,
  useNotifications,
} from '@features/notifications/application/useNotifications';
import { useLocale } from '@shared/i18n/LocaleProvider';
import { Button } from '@shared/ui/Button';
import { Card } from '@shared/ui/Card';
import { PageLoader } from '@shared/ui/Spinner';
import styles from './NotificationsPage.module.css';

export function NotificationsPage() {
  const { t, locale } = useLocale();
  const { data, isLoading, isError, error } = useNotifications();
  const markRead = useMarkNotificationRead();
  const markAll = useMarkAllNotificationsRead();

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <div>
          <h1 className={styles.title}>{t('Thông báo', 'Notifications')}</h1>
          <p className={styles.subtitle}>
            {t(
              'Nhắc lịch và cập nhật từ phòng khám.',
              'Reminders and updates from the clinic.',
            )}
          </p>
        </div>
        <Button
          variant="outline"
          size="sm"
          onClick={() => markAll.mutate()}
          disabled={markAll.isPending}
        >
          {t('Đánh dấu đã đọc tất cả', 'Mark all as read')}
        </Button>
      </header>

      {isLoading && <PageLoader />}
      {isError && (
        <p className={styles.error} role="alert">
          {error.message}
        </p>
      )}

      <div className={styles.list}>
        {data?.map((n) => (
          <Card key={n.id}>
            <div className={styles.itemHeader}>
              <strong className={n.read ? styles.read : styles.unread}>{n.title}</strong>
              <time dateTime={n.createdAt}>
                {new Date(n.createdAt).toLocaleString(locale === 'en' ? 'en-US' : 'vi-VN')}
              </time>
            </div>
            <p className={styles.content}>{n.content}</p>
            {!n.read && (
              <Button
                size="sm"
                variant="ghost"
                onClick={() => markRead.mutate(n.id)}
                disabled={markRead.isPending}
              >
                {t('Đánh dấu đã đọc', 'Mark as read')}
              </Button>
            )}
          </Card>
        ))}
        {data?.length === 0 && (
          <p className={styles.empty}>{t('Chưa có thông báo.', 'No notifications yet.')}</p>
        )}
      </div>

      <p className={styles.settingsHint}>
        {t('Muốn đổi Email/Push theo từng loại tin?', 'Want to change Email/Push per type?')}{' '}
        <Link to="/patient/settings">{t('Mở Cài đặt', 'Open Settings')}</Link>
      </p>
    </div>
  );
}
