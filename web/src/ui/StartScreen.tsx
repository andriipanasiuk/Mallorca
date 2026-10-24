import { useState } from 'preact/hooks';
import { CASH, OPPONENTS, TOURNAMENT } from '../game/index.ts';
import type { Opponent, TableMode } from '../game/index.ts';
import { t } from './i18n.ts';
import { Scene } from './Scene.tsx';

export interface StartScreenProps {
  onStart: (mode: TableMode, opponent: Opponent) => void;
  seed: number;
  farewell?: boolean;
  simple?: boolean;
}

function OpponentChoice({
  value,
  onChange,
}: {
  value: Opponent;
  onChange: (opponent: Opponent) => void;
}) {
  return (
    <div class="start-opponent" role="group" aria-label={t('start.opponent')}>
      <span class="start-opponent-label">{t('start.opponent')}</span>
      {OPPONENTS.map((opponent) => (
        <button
          key={opponent}
          type="button"
          class="button button-small"
          aria-pressed={opponent === value}
          data-testid={`opponent-${opponent}`}
          onClick={() => onChange(opponent)}
        >
          {t(`start.opponents.${opponent}`)}
        </button>
      ))}
    </div>
  );
}

export function StartScreen({ onStart, seed, farewell = false, simple = false }: StartScreenProps) {
  const [opponent, setOpponent] = useState<Opponent>('math');
  const [starting, setStarting] = useState(false);
  const start = (mode: TableMode) => {
    setStarting(true);
    onStart(mode, opponent);
  };
  return (
    <Scene dimmed simple={simple}>
      <main class="start-screen">
        <h1 class="start-title">{t('start.heading')}</h1>
        {farewell ? (
          <p class="start-tagline" data-testid="farewell">
            {t('start.farewell')}
          </p>
        ) : (
          <p class="start-tagline">{t('start.tagline')}</p>
        )}
        <OpponentChoice value={opponent} onChange={setOpponent} />
        <div class="start-actions">
          <button
            type="button"
            class="button button-primary"
            data-testid="start-cash"
            onClick={() => start(CASH)}
          >
            {t('start.cash')}
          </button>
          <button
            type="button"
            class="button"
            data-testid="start-tournament"
            onClick={() => start(TOURNAMENT)}
          >
            {t('start.tournament')}
          </button>
        </div>
        {starting && opponent === 'cfr' ? (
          <p class="start-seed" role="status" data-testid="start-loading">
            {t('start.loading')}
          </p>
        ) : (
          <p class="start-seed">{t('status.seed', { seed })}</p>
        )}
      </main>
    </Scene>
  );
}
