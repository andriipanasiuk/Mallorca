import type { Card } from '../cards/index.ts';
import { blindsFor, legalActionsOf, toAct } from '../engine/index.ts';
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
  blinds: { small: number; big: number };
  handNumber: number;
  result: { winner: SeatIndex | null; category: HandCategory | null; amount: number } | null;
  tournamentWinner: SeatIndex | null;
}

function clamp(value: number, min: number, max: number): number {
  return Math.min(Math.max(Math.round(value), min), max);
}

function seatView(state: SessionState, seat: SeatIndex, humanSeat: SeatIndex): SeatView {
  const { tournament, result } = state;
  const { hand } = tournament;
  const hole = hand?.holeCards[seat];
  const revealed = seat === humanSeat || result?.showdown === true;
  const cards: (Card | 'back')[] =
    hole === undefined ? [] : revealed ? [hole[0], hole[1]] : ['back', 'back'];
  return {
    name: tournament.seats[seat].name,
    stack: tournament.seats[seat].stack,
    bet: hand?.bets[seat] ?? 0,
    isButton: hand !== null && tournament.button === seat,
    isToAct: hand?.toAct === seat,
    cards,
    lastAction: state.lastActions[seat],
  };
}

export function toViewModel(state: SessionState, humanSeat: SeatIndex): ViewModel {
  const { tournament, phase, result } = state;
  const { hand } = tournament;
  const enabled = phase === 'humanToAct' && toAct(tournament) === humanSeat;
  const legal = legalActionsOf(tournament);
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
    blinds: hand === null ? blindsFor(1) : blindsFor(tournament.handNumber),
    handNumber: tournament.handNumber,
    result:
      result === null
        ? null
        : { winner: result.winner, category: result.category, amount: result.amount },
    tournamentWinner: tournament.winner,
  };
}
