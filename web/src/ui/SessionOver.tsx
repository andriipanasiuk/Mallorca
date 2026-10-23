import type { HandStore, ViewModel } from '../game/index.ts';
import { exportHistory } from './history.ts';
import { t } from './i18n.ts';
import { signed } from './signed.ts';

export interface SessionOverProps {
  session: ViewModel['session'];
  store: HandStore;
  sessionId: string | null;
  onPlayAgain: () => void;
}

export function SessionOver({ session, store, sessionId, onPlayAgain }: SessionOverProps) {
  const net = signed(session.netBb);
  const rate = session.bbPer100 === null ? null : signed(session.bbPer100);
  return (
    <div class="overlay" data-testid="session-over" role="dialog" aria-modal="true">
      <div class="dialog">
        <h2 class="dialog-title">{t('session.title')}</h2>
        <p data-testid="session-summary">
          {t('session.summary', { sign: net.sign, bb: net.value, count: session.hands })}
        </p>
        {rate !== null && (
          <p data-testid="session-rate">
            {t('session.rate', { rate: `${rate.sign}${rate.value}` })}
          </p>
        )}
        <div class="dialog-actions">
          <button
            type="button"
            class="button button-small"
            data-testid="export-json"
            onClick={() => void exportHistory(store, sessionId, 'json')}
          >
            {t('history.exportJson')}
          </button>
          <button
            type="button"
            class="button button-small"
            data-testid="export-text"
            onClick={() => void exportHistory(store, sessionId, 'text')}
          >
            {t('history.exportText')}
          </button>
        </div>
        <button
          type="button"
          class="button button-primary"
          data-testid="play-again"
          onClick={onPlayAgain}
        >
          {t('session.again')}
        </button>
      </div>
    </div>
  );
}
