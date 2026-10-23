import { formatCard } from '../cards/index.ts';
import type { Action, EngineEvent } from './types.ts';

const YOU = 'You';

function say(name: string, verb: string): string {
  if (name === YOU) {
    return `${name} ${verb === 'be' ? 'are' : verb}`;
  }
  return `${name} ${verb === 'be' ? 'is' : `${verb}s`}`;
}

function describeAction(name: string, action: Action, amount: number): string {
  switch (action.kind) {
    case 'fold':
      return say(name, 'fold');
    case 'check':
      return say(name, 'check');
    case 'call':
      return `${say(name, 'call')} ${amount}`;
    case 'raise':
      return `${say(name, 'raise')} to ${action.to}`;
  }
}

function describeEvent(event: EngineEvent, names: [string, string]): string[] {
  switch (event.type) {
    case 'handStarted':
      return [`Hand ${event.handNumber}, blinds ${event.smallBlind}/${event.bigBlind}`];
    case 'blindPosted':
      return [`${say(names[event.seat], 'post')} ${event.amount}`];
    case 'acted': {
      const name = names[event.seat];
      const line = describeAction(name, event.action, event.amount);
      return event.allIn ? [line, `${say(name, 'be')} all-in`] : [line];
    }
    case 'streetDealt': {
      const label = event.street.charAt(0).toUpperCase() + event.street.slice(1);
      return [`${label} ${event.cards.map(formatCard).join(' ')}`];
    }
    case 'showdown':
      return event.hands.map((hand, seat) => {
        const cards = hand.cards.map(formatCard).join(' ');
        const category = hand.category.replace('-', ' ');
        return `Showdown: ${say(names[seat as 0 | 1], 'show')} ${cards}, ${category}`;
      });
    case 'potAwarded':
      return [`${say(names[event.seat], 'win')} ${event.amount}`];
    case 'tournamentEnded':
      return [`${say(names[event.winner], 'win')} the tournament`];
    case 'holeCards':
    case 'handEnded':
      return [];
  }
}

export function handHistory(events: EngineEvent[], names: [string, string]): string[] {
  return events.flatMap((event) => describeEvent(event, names));
}
