import type { Intent, Phase, SeatIndex, ViewModel } from '../game/index.ts';
import { ActionBar } from './ActionBar.tsx';
import { Board } from './Board.tsx';
import { HandLog } from './HandLog.tsx';
import { t } from './i18n.ts';
import { Pot } from './Pot.tsx';
import { Seat } from './Seat.tsx';

export interface TableProps {
  vm: ViewModel;
  phase: Phase;
  humanSeat: SeatIndex;
  log: string[];
  onAct: (intent: Intent) => void;
  onNextHand: () => void;
}

function resultText(vm: ViewModel, humanSeat: SeatIndex): string | null {
  const { result } = vm;
  if (result === null) {
    return null;
  }
  if (result.winner === null) {
    return t('result.split', { amount: result.amount });
  }
  if (result.winner === humanSeat) {
    return result.handName === null
      ? t('result.youWin', { amount: result.amount })
      : t('result.youWinWith', { amount: result.amount, hand: result.handName });
  }
  const name = vm.seats[result.winner].name;
  return result.handName === null
    ? t('result.wins', { name, amount: result.amount })
    : t('result.winsWith', { name, amount: result.amount, hand: result.handName });
}

export function Table({ vm, phase, humanSeat, log, onAct, onNextHand }: TableProps) {
  const botSeat: SeatIndex = humanSeat === 0 ? 1 : 0;
  const botWaiting = phase === 'botThinking' || phase === 'animating';
  const text = resultText(vm, humanSeat);
  const raise = vm.controls.raise;
  const barKey = raise === null ? 'none' : `${raise.min}:${raise.max}`;
  const thinking = botWaiting && vm.seats[botSeat].isToAct;
  const announcement = text ?? (thinking ? t('seat.thinking') : '');
  const winner = vm.result?.winner ?? null;
  return (
    <div class="table-layout">
      <div class="visually-hidden" role="status" aria-live="polite" data-testid="live-region">
        {announcement}
      </div>
      <div class="table-main">
        <Seat
          index={botSeat}
          view={vm.seats[botSeat]}
          thinking={thinking}
          winner={winner === botSeat}
          position="top"
        />
        <div class="table-center">
          <Board cards={vm.board} />
          {text === null && <Pot amount={vm.pot} />}
          {text !== null && (
            <div class="result" data-testid="result">
              <span class="result-text">{text}</span>
              {phase === 'handOver' && (
                <button
                  type="button"
                  class="button button-primary"
                  data-testid="next-hand"
                  onClick={onNextHand}
                >
                  {t('result.next')}
                </button>
              )}
            </div>
          )}
        </div>
        <Seat
          index={humanSeat}
          view={vm.seats[humanSeat]}
          thinking={false}
          winner={winner === humanSeat}
          position="bottom"
        />
        <ActionBar key={barKey} controls={vm.controls} onAct={onAct} />
      </div>
      <div class="table-side">
        <HandLog lines={log} />
        <aside class="coach" data-testid="coach" aria-label={t('coach.label')} />
      </div>
    </div>
  );
}
