export type AppLocale = 'vi' | 'en';

const STORAGE_KEY = 'doctorri.locale';

export function getStoredLocale(): AppLocale {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw === 'en' || raw === 'vi') return raw;
  } catch {
    /* ignore */
  }
  return 'vi';
}

export function setStoredLocale(locale: AppLocale): void {
  localStorage.setItem(STORAGE_KEY, locale);
  document.documentElement.lang = locale;
}

export function applyDocumentLocale(locale: AppLocale): void {
  document.documentElement.lang = locale;
}
