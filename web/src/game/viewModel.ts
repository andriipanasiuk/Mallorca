import type { Card } from '../cards/index.ts';
import { legalActionsOf, rulesOf, toAct } from '../engine/index.ts';
import type { LogEntry, SeatIndex } from '../engine/index.ts';
import type { HandCategory } from '../equity/index.ts';
import type { SessionState } from './session.ts';

export interface SeatView {
  name: string;
  stack: number;
  bet: number;
  isButton: boolean;
  isToAct: boolean;
  cards: (Card | 'back')[];
  lastAction: LogEntry | null;
}

export interface RaiseControl {
  min: number;
  max: number;
  presets: { halfPot: number; pot: number; allIn: number };
}

export interface ViewModel {
  seats: [SeatView, SeatView];
  board: Card[];
  pot: number;
  controls: {
    fold: boolean;
    passive: { kind: 'check' | 'call'; amount: number };
    raise: RaiseControl | null;
    enabled: boolean;
  };
  mode: 'cash' | 'tournament';
  session: { hands: number; netBb: number; bbPer100: number | null };
  blinds: { small: number; big: number };
  handNumber: number;
  result: { winner: SeatIndex | null; category: HandCategory | null; amount: number } | null;
  tournamentWinner: SeatIndex | null;
}

function roundTenth(value: number): number {
  return Math.round(value * 10) / 10;
}

function clamp(value: number, min: number, max: number): number {
  return Math.min(Math.max(Math.round(value), min), max);
}

function seatView(state: SessionState, seat: SeatIndex, humanSeat: SeatIndex): SeatView {
  const { table, result } = state;
  const { hand } = table;
  const hole = hand?.holeCards[seat];
  const revealed = seat === humanSeat || result?.showdown === true;
  const cards: (Card | 'back')[] =
    hole === undefined ? [] : revealed ? [hole[0], hole[1]] : ['back', 'back'];
  return {
    name: table.seats[seat].name,
    stack: table.seats[seat].stack,
    bet: hand?.bets[seat] ?? 0,
    isButton: hand !== null && table.button === seat,
    isToAct: hand?.toAct === seat,
    cards,
    lastAction: state.lastActions[seat],
  };
}

export function toViewModel(state: SessionState, humanSeat: SeatIndex): ViewModel {
  const { table, phase, result } = state;
  const { hand } = table;
  const enabled = phase === 'humanToAct' && toAct(table) === humanSeat;
  const legal = legalActionsOf(table);
  const call = enabled ? legal.call : null;
  let raise: RaiseControl | null = null;
  if (enabled && legal.raise !== null && hand !== null) {
    const { min, max } = legal.raise;
    const currentBet = Math.max(hand.bets[0], hand.bets[1]);
    const potAfterCall = hand.pot + (call ?? 0);
    raise = {
      min,
      max,
      presets: {
        halfPot: clamp(currentBet + potAfterCall / 2, min, max),
        pot: clamp(currentBet + potAfterCall, min, max),
        allIn: max,
      },
    };
  }
  const blinds = rulesOf(table.mode).blinds(Math.max(table.handNumber, 1));
  const { hands, net } = state.session;
  const netBb = net / blinds.big;
  return {
    seats: [seatView(state, 0, humanSeat), seatView(state, 1, humanSeat)],
    board: hand === null ? [] : [...hand.board],
    pot: hand?.pot ?? 0,
    controls: {
      fold: enabled && legal.fold,
      passive: call === null ? { kind: 'check', amount: 0 } : { kind: 'call', amount: call },
      raise,
      enabled,
    },
    mode: table.mode.kind,
    session: {
      hands,
      netBb: roundTenth(netBb),
      bbPer100: hands === 0 ? null : roundTenth((netBb / hands) * 100),
    },
    blinds,
    handNumber: table.handNumber,
    result:
      result === null
        ? null
        : { winner: result.winner, category: result.category, amount: result.amount },
    tournamentWinner: table.winner,
  };
}
