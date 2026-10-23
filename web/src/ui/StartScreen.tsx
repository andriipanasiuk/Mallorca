import { t } from './i18n.ts';
import { Scene } from './Scene.tsx';

export interface StartScreenProps {
  onStart: () => void;
  seed: number;
  farewell?: boolean;
  simple?: boolean;
}

export function StartScreen({ onStart, seed, farewell = false, simple = false }: StartScreenProps) {
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
        <button type="button" class="button button-primary" data-testid="start" onClick={onStart}>
          {t('start.cta')}
        </button>
        <p class="start-seed">{t('status.seed', { seed })}</p>
      </main>
    </Scene>
  );
}
