import { createStore } from 'zustand/vanilla';
import type { StoreApi } from 'zustand/vanilla';
import { mulberry32 } from '../cards/index.ts';
import type { Decision } from '../brains/index.ts';
import { toObservations } from '../brains/index.ts';
import {
  apply,
  createTable,
  TOURNAMENT_STACK,
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
  TableMode,
} from '../engine/index.ts';
import type { HandCategory } from '../equity/index.ts';
import { withFallback } from './botClient.ts';
import type { BotClient } from './botClient.ts';
import { handRecord, MATH_BOT, recordDecision } from './records/record.ts';
import type { RecordedDecision } from './records/record.ts';
import type { HandStore } from './records/store.ts';

export type Phase =
  | 'idle'
  | 'humanToAct'
  | 'botThinking'
  | 'animating'
  | 'handOver'
  | 'tournamentOver'
  | 'sessionOver';

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
  session: { hands: number; net: number };
  sessionId: string | null;
}

export interface SessionActions {
  start(mode?: TableMode): Promise<void>;
  leave(): void;
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
  mode: TableMode;
  records?: { store: HandStore; sessionId: string; now: () => string };
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
  let mode = options.mode;
  let startStacks: [number, number] = [0, 0];
  let hands = 0;
  let topUps = 0;
  let handEvents: EngineEvent[] = [];
  let handDecisions: RecordedDecision[] = [];
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
    const table = createTable(names, mode, 0);
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

  const tally = (table: Table): SessionState['session'] => {
    const buyIn = table.mode.kind === 'cash' ? table.mode.buyIn : TOURNAMENT_STACK;
    const live = table.hand !== null && table.hand.street !== 'complete';
    const stack = live ? startStacks[humanSeat] : table.seats[humanSeat].stack;
    return { hands, net: stack - buyIn - topUps };
  };

  return createStore<SessionState & SessionActions>((set, get) => {
    const record = (events: EngineEvent[]) => {
      handEvents = [...handEvents, ...events];
      bot.observe(toObservations(events, botSeat));
      const lastActions: [LogEntry | null, LogEntry | null] = [...get().lastActions];
      for (const event of events) {
        if (event.type === 'acted') {
          lastActions[event.seat] = label(event);
        }
      }
      for (const event of events) {
        if (event.type === 'toppedUp' && event.seat === humanSeat) {
          topUps += event.amount;
        }
      }
      set({
        log: [...get().log, ...handHistory(events)],
        lastActions,
        session: tally(get().table),
      });
    };

    const saveRecord = (tableMode: TableMode) => {
      if (options.records === undefined) {
        return;
      }
      const { store, sessionId, now } = options.records;
      try {
        const saved = handRecord(handEvents, handDecisions, {
          sessionId,
          mode: tableMode,
          names,
          humanSeat,
          bot: MATH_BOT,
          playedAt: now(),
        });
        store.add(saved).catch(() => {});
      } catch {
        return;
      }
    };

    const conclude = (step: { state: Table; events: EngineEvent[] }) => {
      if (step.state.hand?.street === 'complete') {
        const { seats } = step.state;
        hands += 1;
        saveRecord(step.state.mode);
        const paid = (seat: SeatIndex) =>
          startStacks[seat] - seats[seat].stack + awardedTo(step.events, seat);
        set({
          result: {
            ...resultOf(step.events),
            amount: potCollected([paid(0), paid(1)], resultOf(step.events).winner),
          },
          phase: step.state.winner === null ? 'handOver' : 'tournamentOver',
          session: tally(step.state),
        });
      }
    };

    const play = (action: Action, decision: Decision | null = null) => {
      const step = apply(get().table, action);
      if (decision !== null) {
        const index = step.events.findIndex((event) => event.type === 'acted');
        if (index >= 0) {
          handDecisions = [...handDecisions, recordDecision(decision, handEvents.length + index)];
        }
      }
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
        play(resolveIntent(intent, legal), decision);
      }
    };

    const beginHand = (): Promise<void> => {
      const step = startHand(get().table, rng);
      const { seats, hand } = step.state;
      startStacks = [
        seats[0].stack + (hand?.committed[0] ?? 0),
        seats[1].stack + (hand?.committed[1] ?? 0),
      ];
      bot.reset();
      handEvents = [];
      handDecisions = [];
      set({ table: step.state, lastActions: [null, null], result: null });
      record(step.events);
      conclude(step);
      return drive(epoch);
    };

    const initial = fresh();
    return {
      table: initial,
      phase: 'idle',
      log: [],
      lastActions: [null, null],
      lastDecision: null,
      seed,
      result: null,
      session: tally(initial),
      sessionId: options.records?.sessionId ?? null,
      start(next) {
        epoch += 1;
        cancelTimers();
        if (next !== undefined) {
          mode = next;
        }
        hands = 0;
        topUps = 0;
        const table = fresh();
        set({
          table,
          log: [],
          lastDecision: null,
          result: null,
          session: tally(table),
        });
        return beginHand();
      },
      leave() {
        const { phase, table } = get();
        if (table.mode.kind === 'cash' && phase === 'handOver') {
          set({ phase: 'sessionOver' });
        }
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
