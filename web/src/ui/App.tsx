import { useEffect, useState } from 'preact/hooks';
import { toViewModel } from '../game/index.ts';
import type { Intent, Session, SeatIndex } from '../game/index.ts';
import { t } from './i18n.ts';
import { StartScreen } from './StartScreen.tsx';
import { Table } from './Table.tsx';
import { SessionOver } from './SessionOver.tsx';
import { signed } from './signed.ts';
import { TournamentOver } from './TournamentOver.tsx';

export interface AppProps {
  session: Session;
  humanSeat: SeatIndex;
  onNewGame: () => void;
}

function useSessionState(session: Session) {
  const [state, setState] = useState(session.getState());
  useEffect(() => {
    setState(session.getState());
    return session.subscribe(setState);
  }, [session]);
  return state;
}

export function App({ session, humanSeat, onNewGame }: AppProps) {
  const state = useSessionState(session);
  if (state.phase === 'idle') {
    return (
      <StartScreen seed={state.seed} onStart={(mode) => void session.getState().start(mode)} />
    );
  }
  const vm = toViewModel(state, humanSeat);
  const cash = vm.mode === 'cash';
  const net = signed(vm.session.netBb);
  const act = (intent: Intent) => void session.getState().act(intent);
  return (
    <div class="app">
      <header class="status-bar">
        <span data-testid="blinds">
          {t('status.blinds', { small: vm.blinds.small, big: vm.blinds.big })}
        </span>
        {cash && (
          <span data-testid="session-result">
            {t('status.result', { sign: net.sign, bb: net.value })}
          </span>
        )}
        <span data-testid="hand-number">{t('status.hand', { number: vm.handNumber })}</span>
        {cash && (
          <button
            type="button"
            class="button button-small leave"
            data-testid="leave"
            disabled={state.phase !== 'handOver'}
            onClick={() => session.getState().leave()}
          >
            {t('status.leave')}
          </button>
        )}
        <details class="menu">
          <summary class="menu-summary">{t('status.menu')}</summary>
          <div class="menu-body">
            <span>{t('status.seed', { seed: state.seed })}</span>
            <button
              type="button"
              class="button button-small"
              data-testid="new-game"
              onClick={onNewGame}
            >
              {t('status.newGame')}
            </button>
          </div>
        </details>
      </header>
      <Table
        vm={vm}
        phase={state.phase}
        humanSeat={humanSeat}
        log={state.log}
        onAct={act}
        onNextHand={() => void session.getState().nextHand()}
      />
      {state.phase === 'sessionOver' && (
        <SessionOver session={vm.session} onPlayAgain={onNewGame} />
      )}
      {state.phase === 'tournamentOver' && (
        <TournamentOver won={vm.tournamentWinner === humanSeat} onPlayAgain={onNewGame} />
      )}
    </div>
  );
}
