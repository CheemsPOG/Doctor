import { describe, expect, it } from 'vitest';
import { formatSlotTime, formatSlotDate } from '@shared/lib/formatSlotTime';

describe('formatSlotTime', () => {
  it('formats morning slot with Sáng', () => {
    const result = formatSlotTime('2026-07-20T09:30:00+07:00');
    expect(result).toMatch(/09:30/);
    expect(result).toContain('Sáng');
  });

  it('formats afternoon slot with Chiều', () => {
    const result = formatSlotTime('2026-07-20T14:00:00+07:00');
    expect(result).toMatch(/14:00/);
    expect(result).toContain('Chiều');
  });

  it('returns em dash for invalid datetime', () => {
    expect(formatSlotTime('not-a-date')).toBe('—');
  });
});

describe('formatSlotDate', () => {
  it('formats ISO datetime to Vietnamese date string', () => {
    const result = formatSlotDate('2026-07-20T09:30:00+07:00');
    expect(result).toContain('2026');
    expect(result).toContain('20');
  });
});
