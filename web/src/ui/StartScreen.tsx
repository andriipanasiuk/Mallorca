import { CASH, TOURNAMENT } from '../game/index.ts';
import type { TableMode } from '../game/index.ts';
import { t } from './i18n.ts';

export function StartScreen({
  onStart,
  seed,
}: {
  onStart: (mode: TableMode) => void;
  seed: number;
}) {
  return (
    <main class="start-screen">
      <h1 class="start-title">{t('start.heading')}</h1>
      <p class="start-tagline">{t('start.tagline')}</p>
      <button
        type="button"
        class="button button-primary"
        data-testid="start-cash"
        onClick={() => onStart(CASH)}
      >
        {t('start.cash')}
      </button>
      <button
        type="button"
        class="button"
        data-testid="start-tournament"
        onClick={() => onStart(TOURNAMENT)}
      >
        {t('start.tournament')}
      </button>
      <p class="start-seed">{t('status.seed', { seed })}</p>
    </main>
  );
}
