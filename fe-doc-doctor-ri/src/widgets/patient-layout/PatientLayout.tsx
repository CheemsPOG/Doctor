import { AppShell } from '@widgets/app-shell/AppShell';
import { useLocale } from '@shared/i18n/LocaleProvider';

export function PatientLayout() {
  const { t } = useLocale();

  const nav = [
    { to: '/patient', label: t('Trang chủ', 'Home'), end: true },
    { to: '/patient/services', label: t('Dịch vụ', 'Services') },
    { to: '/patient/book', label: t('Đặt lịch', 'Book') },
    { to: '/patient/appointments', label: t('Lịch hẹn', 'Appointments') },
    { to: '/patient/results', label: t('Kết quả', 'Results') },
  ];

  const account = [
    { to: '/patient/profile', label: t('Hồ sơ cá nhân', 'Profile') },
    { to: '/patient/settings', label: t('Cài đặt', 'Settings') },
  ];

  return (
    <AppShell
      brandTo="/patient"
      navItems={nav}
      accountItems={account}
      notificationsPath="/patient/notifications"
    />
  );
}
