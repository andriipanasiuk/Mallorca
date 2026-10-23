import { cleanup, render } from '@testing-library/preact';
import { afterEach, describe, expect, it, vi } from 'vitest';
import en from '../locales/en.json';
import { ActionBar } from './ActionBar.tsx';
import { fixtureResult } from './fixtures.ts';
import { StartScreen } from './StartScreen.tsx';
import { Table } from './Table.tsx';
import { TournamentOver } from './TournamentOver.tsx';
import { viewModel } from './fixtures.ts';

afterEach(cleanup);

function templates(node: unknown): string[] {
  if (typeof node === 'string') {
    return [node];
  }
  return Object.values(node as Record<string, unknown>).flatMap(templates);
}

const patterns = templates(en).map(
  (value) =>
    new RegExp(
      `^${value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&').replace(/\\\{\\\{\w+\\\}\\\}/g, '.+')}$`,
    ),
);

const glyph = /^(\d+|10|[JQKA]|[♣♦♥♠])$/;

function visibleStrings(container: Element): string[] {
  const walker = document.createTreeWalker(container, NodeFilter.SHOW_TEXT);
  const found: string[] = [];
  for (let node = walker.nextNode(); node !== null; node = walker.nextNode()) {
    const text = (node.textContent ?? '').trim();
    const inLog = node.parentElement?.closest('.hand-log-lines') !== null;
    if (text !== '' && !inLog) {
      found.push(text);
    }
  }
  return found;
}

function expectFromLocale(container: Element) {
  const strings = visibleStrings(container);
  expect(strings.length).toBeGreaterThan(0);
  for (const text of strings) {
    const ok = glyph.test(text) || patterns.some((pattern) => pattern.test(text));
    expect(ok, `"${text}" is not in en.json`).toBe(true);
  }
}

describe('copy', () => {
  it('start screen strings come from en.json', () => {
    const { container } = render(<StartScreen seed={42} onStart={vi.fn()} />);
    expectFromLocale(container);
  });

  it('table strings come from en.json', () => {
    const vm = viewModel();
    vm.board = [48, 41, 25];
    const { container } = render(
      <Table
        vm={vm}
        phase="humanToAct"
        humanSeat={0}
        log={['Bot raises to 60']}
        onAct={vi.fn()}
        onNextHand={vi.fn()}
      />,
    );
    expectFromLocale(container);
  });

  it('result and thinking strings come from en.json', () => {
    const vm = fixtureResult();
    const { container } = render(
      <Table
        vm={vm}
        phase="handOver"
        humanSeat={0}
        log={[]}
        onAct={vi.fn()}
        onNextHand={vi.fn()}
      />,
    );
    expectFromLocale(container);
    expect(container.querySelector('[data-testid="result"]')?.textContent).toContain(
      'Mallorca wins 120 with Two pair',
    );
    cleanup();
    const youWin = fixtureResult();
    youWin.result = { winner: 0, handName: 'Flush', amount: 120 };
    const won = render(
      <Table
        vm={youWin}
        phase="handOver"
        humanSeat={0}
        log={[]}
        onAct={vi.fn()}
        onNextHand={vi.fn()}
      />,
    );
    expectFromLocale(won.container);
    expect(won.container.querySelector('[data-testid="result"]')?.textContent).toContain(
      'You win 120 with Flush',
    );
    cleanup();
    const thinking = viewModel({ enabled: false });
    thinking.seats[1].isToAct = true;
    thinking.seats[1].lastAction = null;
    const view = render(
      <Table
        vm={thinking}
        phase="botThinking"
        humanSeat={0}
        log={[]}
        onAct={vi.fn()}
        onNextHand={vi.fn()}
      />,
    );
    expectFromLocale(view.container);
    expect(view.container.textContent).toContain('Thinking…');
  });

  it('action bar and end screen strings come from en.json', () => {
    expectFromLocale(
      render(<ActionBar controls={viewModel().controls} onAct={vi.fn()} />).container,
    );
    cleanup();
    expectFromLocale(render(<TournamentOver won onPlayAgain={vi.fn()} />).container);
    cleanup();
    expectFromLocale(render(<TournamentOver won={false} onPlayAgain={vi.fn()} />).container);
  });

  it('copy has no all-caps words and no em or en dashes', () => {
    for (const value of templates(en)) {
      expect(value).not.toMatch(/[—–]/);
      const words = value.replace(/\{\{\w+\}\}/g, '').match(/[A-Za-z]+/g) ?? [];
      for (const word of words) {
        expect(word.length > 1 && word === word.toUpperCase(), word).toBe(false);
      }
    }
  });
});
