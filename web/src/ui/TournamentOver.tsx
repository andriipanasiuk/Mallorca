import { t } from './i18n.ts';

export interface TournamentOverProps {
  won: boolean;
  onPlayAgain: () => void;
}

export function TournamentOver({ won, onPlayAgain }: TournamentOverProps) {
  return (
    <div class="overlay" data-testid="tournament-over" role="dialog" aria-modal="true">
      <div class="dialog">
        <h2 class="dialog-title">{won ? t('over.won') : t('over.lost')}</h2>
        <button
          type="button"
          class="button button-primary"
          data-testid="play-again"
          onClick={onPlayAgain}
        >
          {t('over.again')}
        </button>
      </div>
    </div>
  );
}
