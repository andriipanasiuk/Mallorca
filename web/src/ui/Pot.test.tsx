import { cleanup, render, screen } from '@testing-library/preact';
import { afterEach, describe, expect, it } from 'vitest';
import { Pot } from './Pot.tsx';

afterEach(cleanup);

describe('Pot', () => {
  it('shows the amount with decorative chips hidden from assistive tech', () => {
    render(<Pot amount={140} />);
    const pot = screen.getByTestId('pot');
    expect(pot.textContent).toBe('Pot 140');
    expect(pot.getAttribute('data-amount')).toBe('140');
    const stack = pot.querySelector('.chip-stack');
    expect(stack?.getAttribute('aria-hidden')).toBe('true');
    expect(stack?.querySelectorAll('.chip').length).toBe(3);
  });
});
