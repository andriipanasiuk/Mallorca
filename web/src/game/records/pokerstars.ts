import type { SeatIndex } from '../../engine/index.ts';
import type { HandCategory } from '../../equity/index.ts';
import { formatCard } from '../cards.ts';
import type { HandRecord } from './record.ts';

const CATEGORY_TEXT: Record<HandCategory, string> = {
  'high-card': 'high card',
  pair: 'a pair',
  'two-pair': 'two pair',
  trips: 'three of a kind',
  straight: 'a straight',
  flush: 'a flush',
  'full-house': 'a full house',
  quads: 'four of a kind',
  'straight-flush': 'a straight flush',
};

const STREET_TITLE = { flop: 'Flop', turn: 'Turn', river: 'River' } as const;

type Street = 'preflop' | keyof typeof STREET_TITLE;
type Pair = [number, number];

function other(seat: SeatIndex): SeatIndex {
  return seat === 0 ? 1 : 0;
}

function cardsText(cards: readonly number[]): string {
  return cards.map(formatCard).join(' ');
}

function dateText(playedAt: string): string {
  return `${playedAt.slice(0, 10).replaceAll('-', '/')} ${playedAt.slice(11, 19)} UTC`;
}

function handNumberText(record: HandRecord): string {
  return String(Date.parse(record.playedAt) * 1000 + (record.handNumber % 1000));
}

function allInSuffix(allIn: boolean): string {
  return allIn ? ' and is all-in' : '';
}

function formatHand(record: HandRecord): string {
  const { events, names } = record;
  const started = events.find((event) => event.type === 'handStarted');
  if (started === undefined) {
    return '';
  }
  const { button, smallBlind, bigBlind, stacks } = started;
  const hero = record.humanSeat ?? 0;
  const lines: string[] = [
    `PokerStars Hand #${handNumberText(record)}: Hold'em No Limit (${smallBlind}/${bigBlind}) - ${dateText(record.playedAt)}`,
    `Table 'Mallorca' 2-max Seat #${button + 1} is the button`,
    `Seat 1: ${names[0]} (${stacks[0]} in chips)`,
    `Seat 2: ${names[1]} (${stacks[1]} in chips)`,
  ];
  const bets: Pair = [0, 0];
  const awards: Pair = [0, 0];
  const foldedOn: (Street | undefined)[] = [undefined, undefined];
  const showdownHands: { cards: string; category: HandCategory }[] = [];
  let street: Street = 'preflop';
  let lastActedIndex = -1;
  events.forEach((event, index) => {
    if (event.type === 'acted') {
      lastActedIndex = index;
    }
  });
  let returnedFlushed = false;
  const flushReturned = (): void => {
    if (returnedFlushed) {
      return;
    }
    returnedFlushed = true;
    for (const event of events) {
      if (event.type === 'betReturned') {
        lines.push(`Uncalled bet (${event.amount}) returned to ${names[event.seat]}`);
      }
    }
  };
  events.forEach((event, index) => {
    switch (event.type) {
      case 'blindPosted': {
        bets[event.seat] = event.amount;
        const kind = event.seat === button ? 'small' : 'big';
        const allIn = event.amount >= stacks[event.seat];
        lines.push(
          `${names[event.seat]}: posts ${kind} blind ${event.amount}${allInSuffix(allIn)}`,
        );
        if (event.seat !== button) {
          lines.push(
            '*** HOLE CARDS ***',
            `Dealt to ${names[hero]} [${record.holeCards[hero].join(' ')}]`,
          );
        }
        break;
      }
      case 'acted': {
        const name = names[event.seat];
        const { action } = event;
        if (action.kind === 'fold') {
          foldedOn[event.seat] = street;
          lines.push(`${name}: folds`);
        } else if (action.kind === 'check') {
          lines.push(`${name}: checks`);
        } else if (action.kind === 'call') {
          bets[event.seat] += event.amount;
          lines.push(`${name}: calls ${event.amount}${allInSuffix(event.allIn)}`);
        } else {
          const largest = Math.max(bets[0], bets[1]);
          const verb =
            largest > 0 ? `raises ${action.to - largest} to ${action.to}` : `bets ${action.to}`;
          bets[event.seat] = action.to;
          lines.push(`${name}: ${verb}${allInSuffix(event.allIn)}`);
        }
        break;
      }
      case 'streetDealt': {
        if (index > lastActedIndex) {
          flushReturned();
        }
        street = event.street;
        bets[0] = 0;
        bets[1] = 0;
        const dealt = cardsText(event.cards);
        const before = event.board.slice(0, event.board.length - event.cards.length);
        lines.push(
          event.street === 'flop'
            ? `*** FLOP *** [${dealt}]`
            : `*** ${event.street.toUpperCase()} *** [${cardsText(before)}] [${dealt}]`,
        );
        break;
      }
      case 'showdown': {
        flushReturned();
        lines.push('*** SHOW DOWN ***');
        for (const seat of [other(button), button]) {
          const hand = event.hands[seat];
          const cards = hand.cards.map(formatCard).join(' ');
          showdownHands[seat] = { cards: `[${cards}]`, category: hand.category };
          lines.push(`${names[seat]}: shows [${cards}] (${CATEGORY_TEXT[hand.category]})`);
        }
        break;
      }
      case 'potAwarded': {
        flushReturned();
        awards[event.seat] += event.amount;
        lines.push(`${names[event.seat]} collected ${event.amount} from pot`);
        break;
      }
      default:
        break;
    }
  });
  const totalPot = awards[0] + awards[1];
  lines.push('*** SUMMARY ***', `Total pot ${totalPot} | Rake 0`);
  if (record.board.length > 0) {
    lines.push(`Board [${record.board.join(' ')}]`);
  }
  for (const seat of [0, 1] as const) {
    const position = seat === button ? ' (button) (small blind)' : ' (big blind)';
    const shown = showdownHands[seat];
    const folded = foldedOn[seat];
    let result: string;
    if (shown !== undefined) {
      result =
        awards[seat] > 0
          ? ` showed ${shown.cards} and won (${awards[seat]}) with ${CATEGORY_TEXT[shown.category]}`
          : ` showed ${shown.cards} and lost with ${CATEGORY_TEXT[shown.category]}`;
    } else if (folded !== undefined) {
      result =
        folded === 'preflop' ? ' folded before Flop' : ` folded on the ${STREET_TITLE[folded]}`;
    } else {
      result = ` collected (${awards[seat]})`;
    }
    lines.push(`Seat ${seat + 1}: ${names[seat]}${position}${result}`);
  }
  return lines.join('\n');
}

export function toPokerStars(records: HandRecord[]): string {
  return records.map(formatHand).join('\n\n\n');
}
