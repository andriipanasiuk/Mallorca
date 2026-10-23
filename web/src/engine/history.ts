import { formatCard } from '../cards/index.ts';
import type { Card } from '../cards/index.ts';
import type { Action, EngineEvent, SeatIndex } from './types.ts';

export interface LogEntry {
  key: string;
  params: Record<string, string | number>;
}

function cardsText(cards: readonly Card[]): string {
  return cards.map(formatCard).join(' ');
}

function describeAction(seat: SeatIndex, action: Action, amount: number): LogEntry {
  switch (action.kind) {
    case 'fold':
    case 'check':
      return { key: action.kind, params: { seat } };
    case 'call':
      return { key: 'call', params: { seat, amount } };
    case 'raise':
      return { key: 'raise', params: { seat, amount: action.to } };
  }
}

function describeEvent(event: EngineEvent): LogEntry[] {
  switch (event.type) {
    case 'handStarted':
      return [
        {
          key: 'handStarted',
          params: { number: event.handNumber, small: event.smallBlind, big: event.bigBlind },
        },
      ];
    case 'blindPosted':
      return [{ key: 'post', params: { seat: event.seat, amount: event.amount } }];
    case 'acted': {
      const line = describeAction(event.seat, event.action, event.amount);
      return event.allIn ? [line, { key: 'allIn', params: { seat: event.seat } }] : [line];
    }
    case 'streetDealt':
      return [{ key: event.street, params: { cards: cardsText(event.cards) } }];
    case 'showdown':
      return event.hands.map((hand, seat) => ({
        key: 'show',
        params: { seat, cards: cardsText(hand.cards), category: hand.category },
      }));
    case 'betReturned':
      return [{ key: 'returned', params: { seat: event.seat, amount: event.amount } }];
    case 'potAwarded':
      return [{ key: 'win', params: { seat: event.seat, amount: event.amount } }];
    case 'toppedUp':
      return [{ key: 'toppedUp', params: { seat: event.seat, amount: event.amount } }];
    case 'tournamentEnded':
      return [{ key: 'winTournament', params: { seat: event.winner } }];
    case 'holeCards':
    case 'handEnded':
      return [];
  }
}

export function handHistory(events: EngineEvent[]): LogEntry[] {
  return events.flatMap(describeEvent);
}
