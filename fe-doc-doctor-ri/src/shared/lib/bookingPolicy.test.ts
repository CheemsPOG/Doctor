import { describe, expect, it } from 'vitest';
import { canFreeCancel, getFreeCancelHours } from '@shared/lib/bookingPolicy';

describe('bookingPolicy', () => {
  it('returns default free cancel hours of 24', () => {
    expect(getFreeCancelHours()).toBe(24);
  });

  it('allows free cancel when appointment is more than 24 hours away', () => {
    const now = new Date('2026-07-19T10:00:00+07:00');
    const startAt = '2026-07-21T10:00:00+07:00';
    expect(canFreeCancel(startAt, now)).toBe(true);
  });

  it('denies free cancel when appointment is within 24 hours', () => {
    const now = new Date('2026-07-20T09:00:00+07:00');
    const startAt = '2026-07-20T15:00:00+07:00';
    expect(canFreeCancel(startAt, now)).toBe(false);
  });

  it('returns false for invalid startAt', () => {
    expect(canFreeCancel('invalid')).toBe(false);
  });
});
