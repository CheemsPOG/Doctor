export type DayPeriod = 'morning' | 'afternoon' | 'evening';

/**
 * Morning before 12:00, afternoon 12:00–17:59, evening from 18:00.
 */
export function getDayPeriod(isoString: string): DayPeriod | null {
  const date = new Date(isoString);
  if (Number.isNaN(date.getTime())) {
    return null;
  }
  const hour = date.getHours();
  if (hour < 12) return 'morning';
  if (hour < 18) return 'afternoon';
  return 'evening';
}

export function formatDayPeriod(period: DayPeriod): string {
  switch (period) {
    case 'morning':
      return 'Sáng';
    case 'afternoon':
      return 'Chiều';
    case 'evening':
      return 'Tối';
  }
}

/**
 * Formats an ISO datetime to HH:mm with clear morning/afternoon label.
 * Example: "09:30 · Sáng", "14:00 · Chiều"
 */
export function formatSlotTime(isoString: string): string {
  const date = new Date(isoString);
  if (Number.isNaN(date.getTime())) {
    return '—';
  }
  const time = date.toLocaleTimeString('vi-VN', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  });
  const period = getDayPeriod(isoString);
  if (!period) {
    return time;
  }
  return `${time} · ${formatDayPeriod(period)}`;
}

/**
 * Formats an ISO datetime string to Vietnamese locale date.
 */
export function formatSlotDate(isoString: string): string {
  const date = new Date(isoString);
  if (Number.isNaN(date.getTime())) {
    return '—';
  }
  return date.toLocaleDateString('vi-VN', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  });
}
