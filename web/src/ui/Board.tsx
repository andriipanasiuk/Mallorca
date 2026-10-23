import type { Card as CardValue } from '../game/index.ts';
import { Card } from './Card.tsx';
import { t } from './i18n.ts';

export function Board({ cards }: { cards: CardValue[] }) {
  return (
    <div class="board" data-testid="board" role="group" aria-label={t('board.label')}>
      {cards.map((card) => (
        <Card key={card} card={card} />
      ))}
    </div>
  );
}
