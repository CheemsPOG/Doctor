import {
  useNotificationPreferences,
  useUpdateNotificationPreference,
} from '@features/notifications/application/useNotifications';
import { useLocale } from '@shared/i18n/LocaleProvider';
import { PageLoader } from '@shared/ui/Spinner';
import styles from './NotificationChannelPrefs.module.css';

const TYPE_LABELS: Record<string, { vi: string; en: string }> = {
  APPOINTMENT_CREATED: { vi: 'Đặt lịch thành công', en: 'Appointment created' },
  APPOINTMENT_CANCELLED: { vi: 'Hủy lịch hẹn', en: 'Appointment cancelled' },
  APPOINTMENT_RESCHEDULED: { vi: 'Đổi lịch hẹn', en: 'Appointment rescheduled' },
  ATTENDANCE_CONFIRMED: { vi: 'Xác nhận sẽ đến', en: 'Attendance confirmed' },
  APPOINTMENT_REMINDER: { vi: 'Nhắc lịch khám', en: 'Appointment reminder' },
  QUEUE: { vi: 'Hàng đợi / gọi số', en: 'Queue updates' },
  SYSTEM: { vi: 'Thông báo hệ thống', en: 'System notices' },
};

export function NotificationChannelPrefs() {
  const { t } = useLocale();
  const prefs = useNotificationPreferences();
  const updatePref = useUpdateNotificationPreference();

  if (prefs.isLoading) return <PageLoader />;
  if (prefs.isError) {
    return (
      <p className={styles.error} role="alert">
        {prefs.error.message}
      </p>
    );
  }

  return (
    <div className={styles.wrap}>
      <p className={styles.help}>
        {t(
          'Chọn kênh nhận từng loại thông báo. Tắt Email/Push sẽ không gửi ra ngoài; hộp thư trong app vẫn có thể nhận tin in-app.',
          'Choose channels for each notification type. Turning off Email/Push stops outbound delivery; in-app inbox may still show messages.',
        )}
      </p>
      <div className={styles.list}>
        {prefs.data?.map((p) => {
          const label = TYPE_LABELS[p.notificationType];
          const title = label
            ? t(label.vi, label.en)
            : p.notificationType;
          return (
            <div key={p.notificationType} className={styles.row}>
              <div className={styles.type}>
                <strong>{title}</strong>
                <span className={styles.code}>{p.notificationType}</span>
              </div>
              <label className={styles.check}>
                <input
                  type="checkbox"
                  checked={p.emailEnabled}
                  onChange={(e) =>
                    updatePref.mutate({ ...p, emailEnabled: e.target.checked })
                  }
                />
                Email
              </label>
              <label className={styles.check}>
                <input
                  type="checkbox"
                  checked={p.pushEnabled}
                  onChange={(e) =>
                    updatePref.mutate({ ...p, pushEnabled: e.target.checked })
                  }
                />
                Push
              </label>
            </div>
          );
        })}
      </div>
      {updatePref.isError && (
        <p className={styles.error} role="alert">
          {updatePref.error.message}
        </p>
      )}
    </div>
  );
}
