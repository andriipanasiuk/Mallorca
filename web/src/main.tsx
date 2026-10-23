import { render } from 'preact';
import { createSession, workerBot } from './game/index.ts';
import type { Session } from './game/index.ts';
import { App } from './ui/App.tsx';
import { t } from './ui/i18n.ts';
import '@fontsource/cormorant-garamond/latin-500.css';
import '@fontsource/cormorant-garamond/latin-600.css';
import '@fontsource/inter/latin-400.css';
import '@fontsource/inter/latin-600.css';
import '@fontsource/ibm-plex-mono/latin-500.css';
import './ui/styles/tokens.css';
import './ui/styles/layout.css';

const HUMAN_SEAT = 0;
const DEFAULT_BOT_DELAY = 700;

const params = new URLSearchParams(window.location.search);

function numberParam(name: string): number | null {
  const raw = params.get(name);
  if (raw === null || raw.trim() === '') {
    return null;
  }
  const value = Number(raw);
  return Number.isFinite(value) && value >= 0 ? Math.floor(value) : null;
}

function randomSeed(): number {
  return crypto.getRandomValues(new Uint32Array(1))[0] ?? 1;
}

const botDelay = numberParam('botDelay') ?? DEFAULT_BOT_DELAY;
let nextSeed: number | null = numberParam('seed');
let session: Session | null = null;

const root = document.getElementById('app');
if (root === null) {
  throw new Error('Missing #app element');
}

function mount(farewell = false): void {
  session?.getState().dispose();
  const seed = nextSeed ?? randomSeed();
  nextSeed = null;
  session = createSession({
    bot: workerBot(),
    seed,
    botDelayMs: botDelay,
    humanSeat: HUMAN_SEAT,
    names: [t('seat.you'), t('seat.bot')],
  });
  render(
    <App
      session={session}
      humanSeat={HUMAN_SEAT}
      farewell={farewell}
      onNewTournament={() => mount()}
      onLeave={() => mount(true)}
    />,
    root as HTMLElement,
  );
}

mount();
