import { t } from './i18n.ts';

export interface TournamentOverProps {
  won: boolean;
  onPlayAgain: () => void;
  onLeave: () => void;
}

export function TournamentOver({ won, onPlayAgain, onLeave }: TournamentOverProps) {
  return (
    <div class="overlay" data-testid="tournament-over" role="dialog" aria-modal="true">
      <div class="dialog glass">
        <h2 class="dialog-title">{t('over.title')}</h2>
        <p class="dialog-line">{won ? t('over.won') : t('over.lost')}</p>
        <div class="dialog-actions">
          <button
            type="button"
            class="button button-primary"
            data-testid="play-again"
            onClick={onPlayAgain}
          >
            {t('over.again')}
          </button>
          <button type="button" class="button" data-testid="leave-tonight" onClick={onLeave}>
            {t('over.leave')}
          </button>
        </div>
      </div>
    </div>
  );
}
