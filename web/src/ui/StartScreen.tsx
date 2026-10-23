import { t } from './i18n.ts';

export function StartScreen({ onStart, seed }: { onStart: () => void; seed: number }) {
  return (
    <main class="start-screen">
      <h1 class="start-title">{t('start.heading')}</h1>
      <p class="start-tagline">{t('start.tagline')}</p>
      <button type="button" class="button button-primary" data-testid="start" onClick={onStart}>
        {t('start.cta')}
      </button>
      <p class="start-seed">{t('status.seed', { seed })}</p>
    </main>
  );
}
