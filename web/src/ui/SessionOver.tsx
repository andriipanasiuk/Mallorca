import type { ViewModel } from '../game/index.ts';
import { t } from './i18n.ts';
import { signed } from './signed.ts';

export interface SessionOverProps {
  session: ViewModel['session'];
  onPlayAgain: () => void;
}

export function SessionOver({ session, onPlayAgain }: SessionOverProps) {
  const net = signed(session.netBb);
  const rate = session.bbPer100 === null ? null : signed(session.bbPer100);
  return (
    <div class="overlay" data-testid="session-over" role="dialog" aria-modal="true">
      <div class="dialog">
        <h2 class="dialog-title">{t('session.title')}</h2>
        <p data-testid="session-summary">
          {t('session.summary', { sign: net.sign, bb: net.value, hands: session.hands })}
        </p>
        {rate !== null && (
          <p data-testid="session-rate">
            {t('session.rate', { rate: `${rate.sign}${rate.value}` })}
          </p>
        )}
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
