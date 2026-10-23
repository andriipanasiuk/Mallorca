import { useEffect, useState } from 'preact/hooks';
import { toViewModel } from '../game/index.ts';
import type { Intent, Session, SeatIndex } from '../game/index.ts';
import { t } from './i18n.ts';
import { StartScreen } from './StartScreen.tsx';
import { Table } from './Table.tsx';
import { TournamentOver } from './TournamentOver.tsx';

export interface AppProps {
  session: Session;
  humanSeat: SeatIndex;
  onNewTournament: () => void;
}

function useSessionState(session: Session) {
  const [state, setState] = useState(session.getState());
  useEffect(() => {
    setState(session.getState());
    return session.subscribe(setState);
  }, [session]);
  return state;
}

export function App({ session, humanSeat, onNewTournament }: AppProps) {
  const state = useSessionState(session);
  if (state.phase === 'idle') {
    return <StartScreen seed={state.seed} onStart={() => void session.getState().start()} />;
  }
  const vm = toViewModel(state, humanSeat);
  const act = (intent: Intent) => void session.getState().act(intent);
  return (
    <div class="app">
      <header class="status-bar">
        <span data-testid="blinds">
          {t('status.blinds', { small: vm.blinds.small, big: vm.blinds.big })}
        </span>
        <span data-testid="hand-number">{t('status.hand', { number: vm.handNumber })}</span>
        <details class="menu">
          <summary class="menu-summary">{t('status.menu')}</summary>
          <div class="menu-body">
            <span>{t('status.seed', { seed: state.seed })}</span>
            <button
              type="button"
              class="button button-small"
              data-testid="new-tournament"
              onClick={onNewTournament}
            >
              {t('status.newTournament')}
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
      {state.phase === 'tournamentOver' && (
        <TournamentOver won={vm.tournamentWinner === humanSeat} onPlayAgain={onNewTournament} />
      )}
    </div>
  );
}
