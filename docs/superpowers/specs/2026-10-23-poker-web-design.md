# Mallorca Web: heads-up poker against the Mallorca bot

## Goal

A browser game where a person plays a heads-up No-Limit Hold'em sit-and-go against the Mallorca math bot.
Later it grows into a training mode where the bot becomes an agent that plays and explains why a move is
better. Version one is the game.

Success for version one:

- A full tournament can be played in the browser on a phone (portrait) and on a desktop (landscape).
- The rules engine is covered by unit and property tests.
- The ported bot makes the same decisions as the Java original on recorded situations (golden tests).
- The site is a static build deployed to GitHub Pages.

## Constraints

- Lives in this repository as a new package `web/`. The Java modules stay untouched as an archive and as the
  reference for golden tests.
- Only the brains are taken from the Java project (the math bot chain). Rules, equity and UI are new code or
  existing npm libraries.
- Process, stack and rules follow `whatmakesittick/whatmakesittick.github.io`.
- Visual design comes from a designer. Version one ships a functional table built on CSS tokens; the brief is
  `docs/design-brief.md`.
- UI language: English only, through i18next.

## Stack

Node 24 (`.nvmrc`), Vite, TypeScript strict, Preact, zustand, i18next, Vitest with `fast-check`,
Playwright, ESLint (typescript-eslint) and Prettier. Hand evaluation through `phe` behind an interface.

## Package layout

```
web/
  src/cards/     Card, Rank, Suit, Deck, parse/format ("As Kd"), seeded RNG
  src/equity/    HandEvaluator (phe adapter), hand-vs-hand and hand-vs-spectrum equity
  src/engine/    heads-up NLHE rules as a pure reducer, tournament and blind schedule, hand history text
  src/brains/    port of MallorcaBrains math bot: Spectrum, StreetEquity, VillainModel, solver, Brain
  src/brains/data/preflop-strength.json
  src/game/      session: zustand store, turn loop, bot Web Worker client, view-model
  src/ui/        Preact components, two layouts, tokens, i18n
  src/locales/en.json
  e2e/           Playwright smoke and contact sheet
  tools/golden/  JVM harness that writes golden fixtures from the Java original
```

Dependencies point down only: `ui → game → engine, brains → equity → cards`. `brains` depends on `engine` types
only for `Action` and observations. An ESLint `no-restricted-imports` rule per layer enforces it.
`cards`, `equity`, `engine` and `brains` use no DOM, no `Math.random` and no clock; randomness comes in as an
`Rng` argument.

## cards

- `Card` is a number 0..51 (`rank * 4 + suit`), with `Rank` 2..14 and `Suit` c d h s.
- `parseCard("As")`, `parseCards("As Kd")`, `formatCard`.
- `Rng` interface with `next(): number` in [0, 1); `mulberry32(seed)` implementation; `shuffle(cards, rng)`.

## equity

- `HandEvaluator.rank7(cards: Card[]): number`, higher is better, plus `describe(rank)` for the hand category
  ("Two Pair"). The adapter wraps `phe`.
- `equityVsHand(hero, villain, board)`: exact enumeration of the remaining board.
- `equityVsSpectrum(hero, spectrum, board, opts)`: weighted equity against a range. Exact on the turn and river,
  Monte Carlo with a seeded RNG on earlier streets, sample count in `opts`.
- `StreetEquity` as in the original: strength now plus positive and negative potential.
- Checked against golden fixtures from `PokerEquilatorBrecher`.

## engine

State:

```ts
interface Tournament {
  seats: [Seat, Seat]
  button: 0 | 1
  handNumber: number
  hand: Hand | null
  winner: 0 | 1 | null
}
interface Hand {
  street: 'preflop' | 'flop' | 'turn' | 'river' | 'showdown' | 'complete'
  deck: Card[]
  holeCards: [[Card, Card], [Card, Card]]
  board: Card[]
  bets: [number, number]
  committed: [number, number]
  pot: number
  toAct: 0 | 1 | null
  lastRaise: number
  actedThisStreet: [boolean, boolean]
  bigBlind: number
}
```

API:

- `createTournament({ names, startingStack: 2000, schedule })`.
- `startHand(t, rng): { state, events }`: moves the button (alternating), sets the blind level, shuffles,
  posts blinds, deals.
- `legalActions(t): { fold: boolean, check: boolean, call: number | null, raise: { min: number, max: number } | null }`.
  `raise` amounts are the total bet for the street ("raise to").
- `apply(t, action): { state, events }` where `action` is `fold`, `check`, `call`, `raise(to)`. Illegal actions throw.
- Events: `HandStarted`, `BlindPosted`, `HoleCardsDealt`, `Acted`, `StreetDealt`, `Showdown`, `PotAwarded`,
  `HandEnded`, `TournamentEnded`.

Rules:

- The button posts the small blind and acts first preflop, last on later streets.
- Minimum raise: the last raise increment, at least the big blind. An all-in for less than a full raise does not
  reopen betting for a player who already acted.
- A bet larger than the opponent's stack is capped; the excess returns.
- When both players are all-in, or one is all-in and the other has called, the board runs out to showdown.
- Split pot; the odd chip goes to the big blind.
- Chips are integers. Starting stack 2000. Blinds start at 10/20 and follow the original schedule
  `20, 30, 40, 50, 60, 80, 100, 120, 160, 200, 240, 300, 400, 500, 600, 800, 1000, 1200, 1600, 2000, 2400`
  (big blind; small blind is half, rounded down), changing every 10 hands.
- The tournament ends when a stack reaches zero.
- `handHistory(events)` writes a readable text log.

Tests: a scenario table (min-raise, short all-in, overbet cap, split, run-out, blind schedule, button rotation)
and `fast-check` properties: chips are conserved, `legalActions` never offers an action `apply` rejects, every
random legal sequence ends the hand.

## brains

Ported from MallorcaBrains and its Core dependencies: `Bot`, `ObservingPlayer`, `SpectrumPlayerObserver`,
`StrengthManager`, `EVAdvisor`, `NLProfitCalculator`, `NLGameSolver`, `ShowdownGameSolver`, `ProfitCorrections`,
`MostProfitActionFromMap`, `ActionDistribution`, `NLActionPreprocessor`, plus `Spectrum`, `StreetEquity`,
`HandState`, `Advisor`, `Advice`, `IAggressionInfo` from Core. `RandomBot` becomes `RandomVillainModel`, the
first `VillainModel`.

Out: preflop charts, push/fold, neural opponent models, hand history parsers, robot and Swing UI.

Interface:

```ts
interface Brain {
  observe(observation: Observation): void
  decide(legal: LegalActions): Decision
}
interface Decision {
  action: Action
  evByAction: { fold: number; passive: number; aggressive: number }
}
```

Observations are engine events filtered to what the bot may see: its own hole cards, the board, actions,
stacks, blinds. The preflop strength table is regenerated with our equity code into
`brains/data/preflop-strength.json` and checked against the Java table within Monte Carlo tolerance.

Golden tests: `tools/golden` compiles the needed Java sources with `javac` (JDK 17+), runs about 200 seeded
situations through the original `EVAdvisor` with `RandomBot` and `PokerEquilatorBrecher`, and writes JSON
(input, EV per action, chosen action, equities). TS tests assert EV within a fixed tolerance and the same
action. A mismatch is a porting bug.

Performance: `decide` runs in a Web Worker. Target under one second per decision on a mid-range phone; if the
solver is slower on the flop, equity switches to seeded Monte Carlo with a sample count that keeps the
golden tests green.

## game

- zustand store holding `Tournament`, the event queue being animated, the hand log and the session phase
  (`idle`, `humanToAct`, `botThinking`, `animating`, `handOver`, `tournamentOver`).
- Turn loop: after each `apply`, the store forwards events to the bot as observations, then either enables the
  human controls or asks the worker for a decision and applies it after a short pause (`botDelayMs`, 700 by
  default, 0 in tests).
- `BotClient` interface with a worker implementation and an in-process implementation for tests.
- `toViewModel(state)` produces everything the UI shows: seats, visible cards, board, pot, legal buttons with
  amounts, slider range and presets (half pot, pot, all-in), last action labels, winner info.
- Seed: random per tournament, shown in the menu, settable via `?seed=` for reproducible e2e runs.

## ui

- Components: `Table`, `Seat`, `Card` (SVG), `Board`, `Pot`, `ActionBar`, `BetSlider`, `HandLog`, `StartScreen`,
  `TournamentOver`.
- Portrait layout for phones, landscape layout with a side hand log for wide screens, switched by a media query.
- Every colour, font, radius and spacing is a CSS token on `:root`, with a dark theme.
- Components render the view-model and compute nothing.
- All strings in `src/locales/en.json` through i18next.
- Keyboard: F fold, C check or call, R raise, arrows move the slider.

## e2e

Playwright against the production build, on a phone viewport and a desktop viewport:

- Start a tournament with a fixed seed, play actions through the buttons until a hand ends, assert the stacks
  still sum to 4000.
- Play a full tournament by going all-in every hand until it ends, assert the end screen.
- `e2e/contact.ts` captures every view state per device into one contact sheet for review and for the designer.

## Repository and process

- Root `CLAUDE.md` and `README.md` describe the Java modules as archive and point to `web/`.
- `web/CLAUDE.md` and `web/ARCHITECTURE.md` hold the rules and the layer contract.
- Before each commit: `prettier`, `eslint`, `tsc --noEmit`, `vitest` on touched files. Before a PR:
  `npm run lint`, `npm test`, `npm run build`, `npm run test:e2e`.
- TDD; fix the code, not the rule; no comments in code; feature branches; conventional commits; squash merge.
- `.github/workflows/web-ci.yml` on pull requests touching `web/**`: lint, test, build, e2e.
- `.github/workflows/web-deploy.yml` on push to `master` touching `web/**`: build with base `/mallorca/` and
  deploy to GitHub Pages.

## Out of scope for version one

Training mode and the explaining agent (the `evByAction` field prepares for it), neural opponent models and
bot choice, languages other than English, multiplayer, accounts, persistent statistics.
