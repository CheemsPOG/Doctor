import { AppShell } from '@widgets/app-shell/AppShell';

const NAV = [
  { to: '/doctor', label: 'Lịch hôm nay', end: true },
  { to: '/doctor/queue', label: 'Hàng đợi' },
];

export function DoctorLayout() {
  return (
    <AppShell
      brandTo="/doctor"
      navItems={NAV}
      footerNote="Doctor Ri — Cổng bác sĩ"
    />
  );
}
