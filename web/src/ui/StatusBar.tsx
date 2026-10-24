import type { ComponentChildren } from 'preact';
import type { ViewModel } from '../game/index.ts';
import { t } from './i18n.ts';
import { signed } from './signed.ts';

export interface StatusBarProps {
  vm: ViewModel;
  canLeave: boolean;
  onLeave: () => void;
  children: ComponentChildren;
}

export function StatusBar({ vm, canLeave, onLeave, children }: StatusBarProps) {
  const cash = vm.mode === 'cash';
  const net = signed(vm.session.netBb);
  return (
    <header class="status-bar">
      <div class="status-info">
        <span data-testid="blinds">
          {t('status.blinds', { small: vm.blinds.small, big: vm.blinds.big })}
        </span>
        {cash && (
          <span data-testid="session-result">
            {t('status.result', { sign: net.sign, bb: net.value })}
          </span>
        )}
        <span data-testid="hand-number">{t('status.hand', { number: vm.handNumber })}</span>
      </div>
      {cash && (
        <button
          type="button"
          class="button button-small leave"
          data-testid="leave"
          disabled={!canLeave}
          onClick={onLeave}
        >
          {t('status.leave')}
        </button>
      )}
      {children}
    </header>
  );
}
