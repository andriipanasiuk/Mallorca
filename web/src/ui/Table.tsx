import type { Intent, LogEntry, Phase, SeatIndex, ViewModel } from '../game/index.ts';
import { ActionBar } from './ActionBar.tsx';
import { Board } from './Board.tsx';
import { HandLog } from './HandLog.tsx';
import { t } from './i18n.ts';
import { handName } from './phrases.ts';
import { Pot } from './Pot.tsx';
import { Seat } from './Seat.tsx';

export interface TableProps {
  vm: ViewModel;
  phase: Phase;
  humanSeat: SeatIndex;
  log: LogEntry[];
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
    return result.category === null
      ? t('result.youWin', { amount: result.amount })
      : t('result.youWinWith', { amount: result.amount, hand: handName(result.category) });
  }
  const name = vm.seats[result.winner].name;
  return result.category === null
    ? t('result.wins', { name, amount: result.amount })
    : t('result.winsWith', { name, amount: result.amount, hand: handName(result.category) });
}

function chipsDirection(winner: SeatIndex | null, humanSeat: SeatIndex): string {
  if (winner === null) {
    return 'split';
  }
  return winner === humanSeat ? 'to-bottom' : 'to-top';
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
      <div class="table-felt">
        <div class="felt-plane">
          {text === null && <Pot amount={vm.pot} />}
          <Board cards={vm.board} />
        </div>
        {vm.result !== null && (
          <span
            class={`result-chips result-chips-${chipsDirection(winner, humanSeat)}`}
            aria-hidden="true"
          >
            <span class="chip chip-terracotta" />
            <span class="chip chip-honey" />
          </span>
        )}
      </div>
      {text !== null && (
        <div class="result glass pill" data-testid="result">
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
      <Seat
        index={botSeat}
        view={vm.seats[botSeat]}
        thinking={thinking}
        winner={winner === botSeat}
        position="top"
      />
      <Seat
        index={humanSeat}
        view={vm.seats[humanSeat]}
        thinking={false}
        winner={winner === humanSeat}
        position="bottom"
      />
      <ActionBar key={barKey} controls={vm.controls} onAct={onAct} />
      <div class="table-side">
        <HandLog entries={log} names={[vm.seats[0].name, vm.seats[1].name]} humanSeat={humanSeat} />
        <aside class="coach" data-testid="coach" aria-label={t('coach.label')} />
      </div>
    </div>
  );
}
