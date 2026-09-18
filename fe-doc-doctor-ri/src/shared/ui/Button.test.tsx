import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Button } from '@shared/ui/Button';

describe('Button', () => {
  it('renders children and applies variant class', () => {
    render(<Button variant="secondary">Đặt lịch</Button>);
    const btn = screen.getByRole('button', { name: 'Đặt lịch' });
    expect(btn).toBeInTheDocument();
    expect(btn.className).toContain('secondary');
  });

  it('can be disabled', () => {
    render(<Button disabled>Không khả dụng</Button>);
    expect(screen.getByRole('button')).toBeDisabled();
  });
});
