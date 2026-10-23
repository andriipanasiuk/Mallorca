import { useEffect, useState } from 'preact/hooks';
import { toViewModel } from '../game/index.ts';
import type { Intent, Session, SeatIndex } from '../game/index.ts';
import { HandLog } from './HandLog.tsx';
import { nextHandDelay } from './autoNext.ts';
import { readAutoNext, readTableStyle, writeAutoNext, writeTableStyle } from './preferences.ts';
import type { TableStyle } from './preferences.ts';
import { t } from './i18n.ts';
import { Scene } from './Scene.tsx';
import { StartScreen } from './StartScreen.tsx';
import { Table } from './Table.tsx';
import { TournamentOver } from './TournamentOver.tsx';

export interface AppProps {
  session: Session;
  humanSeat: SeatIndex;
  onNewTournament: () => void;
  onLeave: () => void;
  farewell?: boolean;
  autoNext?: boolean;
}

function useSessionState(session: Session) {
  const [state, setState] = useState(session.getState());
  useEffect(() => {
    setState(session.getState());
    return session.subscribe(setState);
  }, [session]);
  return state;
}

export function App({
  session,
  humanSeat,
  onNewTournament,
  onLeave,
  farewell = false,
  autoNext = true,
}: AppProps) {
  const state = useSessionState(session);
  const [tableStyle, setTableStyle] = useState<TableStyle>(readTableStyle);
  const simple = tableStyle === 'simple';
  const toggleSimple = () => {
    const next: TableStyle = simple ? 'room' : 'simple';
    writeTableStyle(next);
    setTableStyle(next);
  };
  const [autoNextOn, setAutoNextOn] = useState(readAutoNext);
  const toggleAutoNext = () => {
    writeAutoNext(!autoNextOn);
    setAutoNextOn(!autoNextOn);
  };
  const handOver = state.phase === 'handOver';
  const autoNextMs =
    autoNext && autoNextOn && handOver ? nextHandDelay(toViewModel(state, humanSeat)) : null;
  const handNumber = handOver ? toViewModel(state, humanSeat).handNumber : null;
  useEffect(() => {
    if (autoNextMs === null) {
      return;
    }
    const timer = setTimeout(() => {
      if (session.getState().phase === 'handOver') {
        void session.getState().nextHand();
      }
    }, autoNextMs);
    return () => clearTimeout(timer);
  }, [session, autoNextMs, handNumber]);
  if (state.phase === 'idle') {
    return (
      <StartScreen
        seed={state.seed}
        simple={simple}
        farewell={farewell}
        onStart={() => void session.getState().start()}
      />
    );
  }
  const vm = toViewModel(state, humanSeat);
  const act = (intent: Intent) => void session.getState().act(intent);
  return (
    <Scene simple={simple}>
      <div class="app">
        <header class="scene-top">
          <span class="status-pill glass pill" data-testid="blinds">
            {t('status.blinds', { small: vm.blinds.small, big: vm.blinds.big })}
          </span>
          <span class="status-pill glass pill" data-testid="hand-number">
            {t('status.hand', { number: vm.handNumber })}
          </span>
          <details class="menu">
            <summary class="menu-summary glass pill">{t('status.menu')}</summary>
            <div class="menu-body glass">
              <label class="menu-toggle">
                <input
                  type="checkbox"
                  data-testid="simple-table"
                  checked={simple}
                  onChange={toggleSimple}
                />
                <span>{t('status.simpleTable')}</span>
              </label>
              <label class="menu-toggle">
                <input
                  type="checkbox"
                  data-testid="auto-next"
                  checked={autoNextOn}
                  onChange={toggleAutoNext}
                />
                <span>{t('status.autoNext')}</span>
              </label>
              <span>{t('status.seed', { seed: state.seed })}</span>
              <button
                type="button"
                class="button button-small glass pill"
                data-testid="new-tournament"
                onClick={onNewTournament}
              >
                {t('status.newTournament')}
              </button>
              <HandLog
                entries={state.log}
                names={[vm.seats[0].name, vm.seats[1].name]}
                humanSeat={humanSeat}
                testId="hand-log-menu"
              />
            </div>
          </details>
        </header>
        <Table
          vm={vm}
          phase={state.phase}
          humanSeat={humanSeat}
          log={state.log}
          onAct={act}
          autoNextMs={autoNextMs}
          onNextHand={() => void session.getState().nextHand()}
        />
        {state.phase === 'tournamentOver' && (
          <TournamentOver
            won={vm.tournamentWinner === humanSeat}
            onPlayAgain={onNewTournament}
            onLeave={onLeave}
          />
        )}
      </div>
    </Scene>
  );
}
