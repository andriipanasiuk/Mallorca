import { otherSeat } from './legal.ts';
import type { TableMode } from './table.ts';
import type { EngineEvent, Seat, SeatIndex } from './types.ts';

export interface Rules {
  blinds(handNumber: number): { small: number; big: number };
  beforeHand(seats: [Seat, Seat]): { seats: [Seat, Seat]; events: EngineEvent[] };
  isOver(seats: [Seat, Seat]): SeatIndex | null;
}

export const BIG_BLINDS = [
  20, 30, 40, 50, 60, 80, 100, 120, 160, 200, 240, 300, 400, 500, 600, 800, 1000, 1200, 1600, 2000,
  2400,
];

const HANDS_PER_LEVEL = 10;

export function blindsFor(handNumber: number): { small: number; big: number } {
  const level = Math.min(
    Math.max(Math.floor((handNumber - 1) / HANDS_PER_LEVEL), 0),
    BIG_BLINDS.length - 1,
  );
  const big = BIG_BLINDS[level] as number;
  return { small: Math.floor(big / 2), big };
}

function brokeSeat(seats: [Seat, Seat]): SeatIndex | null {
  return seats[0].stack === 0 ? 0 : seats[1].stack === 0 ? 1 : null;
}

const tournamentRules: Rules = {
  blinds: blindsFor,
  beforeHand: (seats) => ({ seats, events: [] }),
  isOver: (seats) => {
    const broke = brokeSeat(seats);
    return broke === null ? null : otherSeat(broke);
  },
};

function cashRules(bigBlind: number, buyIn: number): Rules {
  return {
    blinds: () => ({ small: Math.floor(bigBlind / 2), big: bigBlind }),
    beforeHand: (seats) => {
      const events: EngineEvent[] = [];
      const topped = seats.map((seat, index) => {
        if (seat.stack >= buyIn) return seat;
        events.push({ type: 'toppedUp', seat: index as SeatIndex, amount: buyIn - seat.stack });
        return { ...seat, stack: buyIn };
      }) as [Seat, Seat];
      return { seats: topped, events };
    },
    isOver: () => null,
  };
}

export function rulesOf(mode: TableMode): Rules {
  return mode.kind === 'cash' ? cashRules(mode.bigBlind, mode.buyIn) : tournamentRules;
}
