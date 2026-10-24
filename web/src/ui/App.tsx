import { useEffect, useState } from 'preact/hooks';
import { toViewModel } from '../game/index.ts';
import type { HandStore, Intent, Session, SeatIndex } from '../game/index.ts';
import { HistoryMenu } from './HistoryMenu.tsx';
import { StartScreen } from './StartScreen.tsx';
import { Table } from './Table.tsx';
import { SessionOver } from './SessionOver.tsx';
import { StatusBar } from './StatusBar.tsx';
import { TournamentOver } from './TournamentOver.tsx';

export interface AppProps {
  session: Session;
  store: HandStore;
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

export function App({ session, store, humanSeat, onNewGame }: AppProps) {
  const state = useSessionState(session);
  if (state.phase === 'idle') {
    return (
      <StartScreen seed={state.seed} onStart={(mode) => void session.getState().start(mode)} />
    );
  }
  const vm = toViewModel(state, humanSeat);
  const act = (intent: Intent) => void session.getState().act(intent);
  return (
    <div class="app">
      <StatusBar
        vm={vm}
        canLeave={state.phase === 'handOver'}
        onLeave={() => session.getState().leave()}
      >
        <HistoryMenu seed={state.seed} store={store} onNewGame={onNewGame} />
      </StatusBar>
      <Table
        vm={vm}
        phase={state.phase}
        humanSeat={humanSeat}
        log={state.log}
        onAct={act}
        onNextHand={() => void session.getState().nextHand()}
      />
      {state.phase === 'sessionOver' && (
        <SessionOver
          session={vm.session}
          store={store}
          sessionId={state.sessionId}
          onPlayAgain={onNewGame}
        />
      )}
      {state.phase === 'tournamentOver' && (
        <TournamentOver won={vm.tournamentWinner === humanSeat} onPlayAgain={onNewGame} />
      )}
    </div>
  );
}
