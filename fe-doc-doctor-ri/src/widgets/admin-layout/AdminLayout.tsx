import { AppShell } from '@widgets/app-shell/AppShell';

const NAV = [
  { to: '/admin', label: 'Tổng quan', end: true },
  { to: '/admin/services', label: 'Dịch vụ' },
  { to: '/admin/doctors', label: 'Bác sĩ' },
  { to: '/admin/audit', label: 'Audit' },
];

export function AdminLayout() {
  return (
    <AppShell
      brandTo="/admin"
      navItems={NAV}
      footerNote="Doctor Ri — Quản trị phòng khám"
    />
  );
}
