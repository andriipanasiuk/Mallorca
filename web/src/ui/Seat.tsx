import type { SeatView } from '../game/index.ts';
import { Card } from './Card.tsx';
import { t } from './i18n.ts';
import { actionTag } from './phrases.ts';

export interface SeatProps {
  index: 0 | 1;
  view: SeatView;
  thinking: boolean;
  winner: boolean;
  position: 'top' | 'bottom';
}

export function Seat({ index, view, thinking, winner, position }: SeatProps) {
  const className = [
    'seat',
    `seat-${position}`,
    view.isToAct ? 'seat-active' : '',
    winner ? 'seat-winner' : '',
  ]
    .filter(Boolean)
    .join(' ');
  return (
    <section class={className} data-testid={`seat-${index}`} data-winner={winner}>
      <header class="seat-info">
        <span class="seat-name">{view.name}</span>
        {view.isButton && (
          <span class="dealer-button" role="img" aria-label={t('seat.dealerLabel')}>
            {t('seat.dealer')}
          </span>
        )}
        <span class="seat-stack" data-testid={`stack-${index}`} data-stack={view.stack}>
          {view.stack}
        </span>
      </header>
      <div class="seat-cards">
        {view.cards.map((card, slot) => (
          <Card key={slot} card={card} highlight={winner && card !== 'back'} />
        ))}
      </div>
      <div class="seat-tags">
        {thinking && (
          <span class="tag tag-thinking" data-testid={`thinking-${index}`}>
            {t('seat.thinking')}
          </span>
        )}
        {!thinking && view.lastAction !== null && (
          <span class="tag tag-action" data-testid={`action-${index}`}>
            {actionTag(view.lastAction)}
          </span>
        )}
        {view.bet > 0 && (
          <span class="tag tag-bet" data-testid={`bet-${index}`}>
            {t('seat.bet', { amount: view.bet })}
          </span>
        )}
      </div>
    </section>
  );
}
