export type AppRole =
  | 'PATIENT'
  | 'RECEPTIONIST'
  | 'NURSE'
  | 'DOCTOR'
  | 'CLINIC_ADMIN'
  | 'SYSTEM_ADMIN';

export function normalizeRole(role: string | undefined | null): AppRole {
  const r = (role ?? 'PATIENT').toUpperCase();
  if (
    r === 'PATIENT' ||
    r === 'RECEPTIONIST' ||
    r === 'NURSE' ||
    r === 'DOCTOR' ||
    r === 'CLINIC_ADMIN' ||
    r === 'SYSTEM_ADMIN'
  ) {
    return r;
  }
  return 'PATIENT';
}

export function homePathForRole(role: string | undefined | null): string {
  switch (normalizeRole(role)) {
    case 'RECEPTIONIST':
    case 'NURSE':
      return '/clinic';
    case 'DOCTOR':
      return '/doctor';
    case 'CLINIC_ADMIN':
    case 'SYSTEM_ADMIN':
      return '/admin';
    default:
      return '/patient';
  }
}

export function roleAllowed(role: string | undefined | null, allowed: AppRole[]): boolean {
  return allowed.includes(normalizeRole(role));
}
