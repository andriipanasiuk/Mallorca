import { createStore } from 'zustand/vanilla';
import type { StoreApi } from 'zustand/vanilla';
import { mulberry32 } from '../cards/index.ts';
import type { Decision } from '../brains/index.ts';
import { toObservations } from '../brains/index.ts';
import {
  apply,
  createTable,
  TOURNAMENT,
  handHistory,
  legalActionsOf,
  startHand,
  toAct,
} from '../engine/index.ts';
import type {
  Action,
  EngineEvent,
  LegalActions,
  LogEntry,
  SeatIndex,
  Table,
} from '../engine/index.ts';
import type { HandCategory } from '../equity/index.ts';
import { withFallback } from './botClient.ts';
import type { BotClient } from './botClient.ts';

export type Phase =
  'idle' | 'humanToAct' | 'botThinking' | 'animating' | 'handOver' | 'tournamentOver';

export type Intent = { kind: 'fold' | 'passive' } | { kind: 'raise'; to: number };

export interface HandResult {
  winner: SeatIndex | null;
  category: HandCategory | null;
  showdown: boolean;
  amount: number;
}

export interface SessionState {
  table: Table;
  phase: Phase;
  log: LogEntry[];
  lastActions: [LogEntry | null, LogEntry | null];
  lastDecision: Decision | null;
  seed: number;
  result: HandResult | null;
}

export interface SessionActions {
  start(): Promise<void>;
  nextHand(): Promise<void>;
  act(intent: Intent): Promise<void>;
  dispose(): void;
}

export type Session = StoreApi<SessionState & SessionActions>;

export interface SessionOptions {
  bot: BotClient;
  seed: number;
  botDelayMs: number;
  humanSeat: SeatIndex;
  names: [string, string];
  startingStacks?: [number, number];
}

function label(event: Extract<EngineEvent, { type: 'acted' }>): LogEntry {
  if (event.allIn && event.action.kind !== 'fold') {
    return { key: 'allIn', params: {} };
  }
  switch (event.action.kind) {
    case 'fold':
    case 'check':
      return { key: event.action.kind, params: {} };
    case 'call':
      return { key: 'call', params: { amount: event.amount } };
    case 'raise':
      return { key: 'raiseTo', params: { amount: event.action.to } };
  }
}

export function resultOf(events: EngineEvent[]): Omit<HandResult, 'amount'> {
  const awards: [number, number] = [0, 0];
  let hands: Extract<EngineEvent, { type: 'showdown' }>['hands'] | null = null;
  for (const event of events) {
    if (event.type === 'potAwarded') {
      awards[event.seat] += event.amount;
    } else if (event.type === 'showdown') {
      hands = event.hands;
    }
  }
  let winner: SeatIndex | null;
  if (hands !== null) {
    winner = hands[0].rank === hands[1].rank ? null : hands[0].rank > hands[1].rank ? 0 : 1;
  } else {
    winner = awards[0] > 0 ? 0 : 1;
  }
  const shown = hands === null ? null : hands[winner ?? 0];
  return {
    winner,
    category: shown === null ? null : shown.category,
    showdown: hands !== null,
  };
}

export function potCollected(paid: [number, number], winner: SeatIndex | null): number {
  const matched = Math.min(paid[0], paid[1]);
  return winner === null ? matched : matched * 2;
}

function awardedTo(events: EngineEvent[], seat: SeatIndex): number {
  let total = 0;
  for (const event of events) {
    if (event.type === 'potAwarded' && event.seat === seat) {
      total += event.amount;
    }
  }
  return total;
}

export function toIntent(action: Action): Intent {
  switch (action.kind) {
    case 'fold':
      return { kind: 'fold' };
    case 'raise':
      return { kind: 'raise', to: action.to };
    default:
      return { kind: 'passive' };
  }
}

export function resolveIntent(intent: Intent, legal: LegalActions): Action {
  if (intent.kind === 'raise' && legal.raise !== null) {
    const { min, max } = legal.raise;
    const to = Math.min(Math.max(Math.round(intent.to), min), max);
    return { kind: 'raise', to };
  }
  if (intent.kind === 'fold' && legal.fold) {
    return { kind: 'fold' };
  }
  if (legal.check) {
    return { kind: 'check' };
  }
  if (legal.call !== null) {
    return { kind: 'call' };
  }
  return { kind: 'fold' };
}

export function createSession(options: SessionOptions): Session {
  const { seed, botDelayMs, humanSeat, names } = options;
  const botSeat: SeatIndex = humanSeat === 0 ? 1 : 0;
  const bot = withFallback(options.bot);
  const rng = mulberry32(seed);
  let epoch = 0;
  let startStacks: [number, number] = [0, 0];
  const timers = new Map<ReturnType<typeof setTimeout>, () => void>();

  const sleep = (ms: number): Promise<void> =>
    new Promise((resolve) => {
      const timer = setTimeout(() => {
        timers.delete(timer);
        resolve();
      }, ms);
      timers.set(timer, resolve);
    });

  const cancelTimers = () => {
    for (const [timer, resolve] of timers) {
      clearTimeout(timer);
      resolve();
    }
    timers.clear();
  };

  const fresh = (): Table => {
    const table = createTable(names, TOURNAMENT, 0);
    const stacks = options.startingStacks;
    if (stacks === undefined) {
      return table;
    }
    return {
      ...table,
      seats: [
        { name: names[0], stack: stacks[0] },
        { name: names[1], stack: stacks[1] },
      ],
    };
  };

  return createStore<SessionState & SessionActions>((set, get) => {
    const record = (events: EngineEvent[]) => {
      bot.observe(toObservations(events, botSeat));
      const lastActions: [LogEntry | null, LogEntry | null] = [...get().lastActions];
      for (const event of events) {
        if (event.type === 'acted') {
          lastActions[event.seat] = label(event);
        }
      }
      set({ log: [...get().log, ...handHistory(events)], lastActions });
    };

    const conclude = (step: { state: Table; events: EngineEvent[] }) => {
      if (step.state.hand?.street === 'complete') {
        const { seats } = step.state;
        const paid = (seat: SeatIndex) =>
          startStacks[seat] - seats[seat].stack + awardedTo(step.events, seat);
        set({
          result: {
            ...resultOf(step.events),
            amount: potCollected([paid(0), paid(1)], resultOf(step.events).winner),
          },
          phase: step.state.winner === null ? 'handOver' : 'tournamentOver',
        });
      }
    };

    const play = (action: Action) => {
      const step = apply(get().table, action);
      set({ table: step.state });
      record(step.events);
      conclude(step);
    };

    const drive = async (myEpoch: number): Promise<void> => {
      for (;;) {
        const { table } = get();
        if (table.hand === null || table.hand.street === 'complete') {
          return;
        }
        if (toAct(table) === humanSeat) {
          set({ phase: 'humanToAct' });
          return;
        }
        set({ phase: 'botThinking' });
        const legal = legalActionsOf(table);
        let decision: Decision | null;
        try {
          decision = await bot.decide(legal);
        } catch {
          decision = null;
        }
        if (myEpoch !== epoch) {
          return;
        }
        if (decision !== null) {
          set({ lastDecision: decision });
        }
        if (botDelayMs > 0) {
          set({ phase: 'animating' });
          await sleep(botDelayMs);
          if (myEpoch !== epoch) {
            return;
          }
        }
        const intent: Intent = decision === null ? { kind: 'passive' } : toIntent(decision.action);
        play(resolveIntent(intent, legal));
      }
    };

    const beginHand = (): Promise<void> => {
      const { seats } = get().table;
      startStacks = [seats[0].stack, seats[1].stack];
      const step = startHand(get().table, rng);
      bot.reset();
      set({ table: step.state, lastActions: [null, null], result: null });
      record(step.events);
      conclude(step);
      return drive(epoch);
    };

    return {
      table: fresh(),
      phase: 'idle',
      log: [],
      lastActions: [null, null],
      lastDecision: null,
      seed,
      result: null,
      start() {
        epoch += 1;
        cancelTimers();
        set({ table: fresh(), log: [], lastDecision: null, result: null });
        return beginHand();
      },
      dispose() {
        epoch += 1;
        cancelTimers();
        bot.dispose?.();
      },
      nextHand() {
        if (get().phase !== 'handOver') {
          return Promise.resolve();
        }
        return beginHand();
      },
      act(intent) {
        const { phase, table } = get();
        if (phase !== 'humanToAct' || toAct(table) !== humanSeat) {
          return Promise.resolve();
        }
        play(resolveIntent(intent, legalActionsOf(table)));
        return drive(epoch);
      },
    };
  });
}
