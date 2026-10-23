import { cleanup, render, screen } from '@testing-library/preact';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { fixtureResult, viewModel } from './fixtures.ts';
import { Table } from './Table.tsx';

afterEach(cleanup);

function table(vm = viewModel(), phase: 'humanToAct' | 'botThinking' | 'handOver' = 'humanToAct') {
  return render(
    <Table vm={vm} phase={phase} humanSeat={0} log={[]} onAct={vi.fn()} onNextHand={vi.fn()} />,
  );
}

describe('Table result and announcements', () => {
  it('shows the won amount and hides the pot while a result is shown', () => {
    table(fixtureResult(), 'handOver');
    expect(screen.getByTestId('result').textContent).toContain('Mallorca wins 120 with Two pair');
    expect(screen.queryByTestId('pot')).toBeNull();
  });

  it('shows the pot while a hand is in progress', () => {
    table();
    expect(screen.getByTestId('pot')).not.toBeNull();
  });

  it('words a split with each share', () => {
    const vm = fixtureResult();
    vm.result = { winner: null, category: null, amount: 60 };
    table(vm, 'handOver');
    expect(screen.getByTestId('result').textContent).toContain('Split pot 60 each');
  });

  it('announces the result in a polite live region', () => {
    table(fixtureResult(), 'handOver');
    const region = screen.getByTestId('live-region');
    expect(region.getAttribute('role')).toBe('status');
    expect(region.getAttribute('aria-live')).toBe('polite');
    expect(region.textContent).toBe('Mallorca wins 120 with Two pair');
  });

  it('announces the bot thinking state', () => {
    const vm = viewModel({ enabled: false });
    vm.seats[1].isToAct = true;
    table(vm, 'botThinking');
    expect(screen.getByTestId('live-region').textContent).toBe('Thinking…');
  });
});
