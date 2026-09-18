import { Link } from 'react-router-dom';
import { NotificationChannelPrefs } from '@features/notifications/ui/NotificationChannelPrefs';
import { useLocale } from '@shared/i18n/LocaleProvider';
import type { AppLocale } from '@shared/lib/locale';
import styles from './SettingsPage.module.css';

const LOCALES: { value: AppLocale; vi: string; en: string }[] = [
  { value: 'vi', vi: 'Tiếng Việt', en: 'Vietnamese' },
  { value: 'en', vi: 'English', en: 'English' },
];

export function SettingsPage() {
  const { locale, setLocale, t } = useLocale();

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <h1 className={styles.title}>{t('Cài đặt', 'Settings')}</h1>
        <p className={styles.subtitle}>
          {t(
            'Ngôn ngữ giao diện và tuỳ chọn nhận thông báo.',
            'Interface language and notification delivery preferences.',
          )}
        </p>
      </header>

      <section className={styles.card} aria-labelledby="lang-heading">
        <h2 id="lang-heading" className={styles.sectionTitle}>
          {t('Ngôn ngữ', 'Language')}
        </h2>
        <p className={styles.sectionHelp}>
          {t(
            'Lựa chọn được lưu trên thiết bị này. Một số màn hình sẽ được dịch dần.',
            'Saved on this device. More screens will be translated over time.',
          )}
        </p>
        <div className={styles.langGroup} role="radiogroup" aria-label={t('Ngôn ngữ', 'Language')}>
          {LOCALES.map((item) => {
            const selected = locale === item.value;
            return (
              <button
                key={item.value}
                type="button"
                role="radio"
                aria-checked={selected}
                className={`${styles.langOption} ${selected ? styles.langSelected : ''}`}
                onClick={() => setLocale(item.value)}
              >
                {t(item.vi, item.en)}
              </button>
            );
          })}
        </div>
      </section>

      <section className={styles.card} aria-labelledby="channel-heading">
        <h2 id="channel-heading" className={styles.sectionTitle}>
          {t('Kênh thông báo', 'Notification channels')}
        </h2>
        <NotificationChannelPrefs />
      </section>

      <section className={styles.card} aria-labelledby="account-heading">
        <h2 id="account-heading" className={styles.sectionTitle}>
          {t('Tài khoản', 'Account')}
        </h2>
        <ul className={styles.links}>
          <li>
            <Link to="/patient/profile">{t('Hồ sơ cá nhân', 'Personal profile')}</Link>
          </li>
          <li>
            <Link to="/patient/notifications">{t('Hộp thư thông báo', 'Notification inbox')}</Link>
          </li>
        </ul>
      </section>
    </div>
  );
}
