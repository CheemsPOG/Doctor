export type AuthUser = {
  accessToken: string;
  refreshToken?: string;
  userId: string;
  patientId: string;
  doctorId: string;
  fullName: string;
  email: string;
  role: string;
};

const STORAGE_KEY = 'doctorri.auth';

export function getAuthSession(): AuthUser | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw) as Partial<AuthUser>;
    if (!parsed.accessToken || !parsed.userId) return null;
    return {
      accessToken: parsed.accessToken,
      refreshToken: parsed.refreshToken,
      userId: String(parsed.userId),
      patientId: parsed.patientId != null ? String(parsed.patientId) : '',
      doctorId: parsed.doctorId != null ? String(parsed.doctorId) : '',
      fullName: parsed.fullName ?? '',
      email: parsed.email ?? '',
      role: parsed.role ?? 'PATIENT',
    };
  } catch {
    return null;
  }
}

export function setAuthSession(user: AuthUser): void {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(user));
}

export function clearAuthSession(): void {
  localStorage.removeItem(STORAGE_KEY);
}

export function isAuthenticated(): boolean {
  return getAuthSession() !== null;
}
