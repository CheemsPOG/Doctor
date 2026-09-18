const DEFAULT_FREE_CANCEL_HOURS = 24;

/**
 * Returns true if the appointment can be cancelled without penalty
 * based on how many hours remain before startAt.
 */
export function canFreeCancel(
  startAt: string,
  now: Date = new Date(),
  freeCancelHours: number = DEFAULT_FREE_CANCEL_HOURS,
): boolean {
  const appointmentStart = new Date(startAt);
  if (Number.isNaN(appointmentStart.getTime())) {
    return false;
  }
  const hoursUntil = (appointmentStart.getTime() - now.getTime()) / (1000 * 60 * 60);
  return hoursUntil >= freeCancelHours;
}

export function getFreeCancelHours(): number {
  return DEFAULT_FREE_CANCEL_HOURS;
}
