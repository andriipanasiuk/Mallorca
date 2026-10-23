import { formatCard, rankOf, suitOf } from '../game/index.ts';
import type { Card as CardValue } from '../game/index.ts';
import { t } from './i18n.ts';

const RANKS: Record<number, string> = { 10: '10', 11: 'J', 12: 'Q', 13: 'K', 14: 'A' };
const SUITS = ['♣', '♦', '♥', '♠'];

export interface CardProps {
  card: CardValue | 'back';
  highlight?: boolean;
}

export function Card({ card, highlight = false }: CardProps) {
  const className = `card${highlight ? ' card-highlight' : ''}`;
  if (card === 'back') {
    return (
      <svg
        class={`${className} card-back`}
        viewBox="0 0 70 100"
        role="img"
        aria-label={t('card.back')}
        data-testid="card-back"
      >
        <rect class="card-body" x="1" y="1" width="68" height="98" rx="7" />
        <rect class="card-pattern-frame" x="8" y="8" width="54" height="84" rx="4" />
        <path class="card-pattern" d="M8 8 L62 92 M62 8 L8 92 M35 8 L35 92 M8 50 L62 50" />
      </svg>
    );
  }
  const rank = rankOf(card);
  const suit = SUITS[suitOf(card)] ?? '';
  const red = suitOf(card) === 1 || suitOf(card) === 2;
  return (
    <svg
      class={`${className}${red ? ' card-red' : ' card-black'}`}
      viewBox="0 0 70 100"
      role="img"
      aria-label={formatCard(card)}
      data-testid="card"
      data-card={formatCard(card)}
    >
      <rect class="card-body" x="1" y="1" width="68" height="98" rx="7" />
      <text class="card-rank" x="8" y="30">
        {RANKS[rank] ?? String(rank)}
      </text>
      <text class="card-suit" x="35" y="78" text-anchor="middle">
        {suit}
      </text>
    </svg>
  );
}
