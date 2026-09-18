import { AppShell } from '@widgets/app-shell/AppShell';

const NAV = [
  { to: '/clinic', label: 'Hàng đợi', end: true },
  { to: '/clinic/walk-in', label: 'Walk-in' },
];

export function ClinicLayout() {
  return (
    <AppShell
      brandTo="/clinic"
      navItems={NAV}
      footerNote="Doctor Ri — Lễ tân / Điều phối phòng khám"
    />
  );
}
