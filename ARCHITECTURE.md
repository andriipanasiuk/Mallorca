# Architecture

One repository, two parts. The root holds the Java heads-up poker bot as an archive: it is not modified and it is
the reference the TypeScript port is checked against. `web/` is the browser game: a heads-up No-Limit Hold'em
tournament against a TypeScript port of the Mallorca math bot, built as a static site.

## Repository layout

| Path                                                                | Owns                                                                  |
| ------------------------------------------------------------------- | --------------------------------------------------------------------- |
| `MallorcaBrains/`, `MallorcaCore/`, `MallorcaEquilator/`, `Tools/`  | Java bot, game model and equilator; the source of the golden fixtures |
| Other Gradle modules (`MallorcaUI/`, `PokerRobot/`, `HHParser/`, …) | Java archive, not used by `web/`                                      |
| `web/src/`                                                          | The game, in layers (see "Layers")                                    |
| `web/src/locales/en.json`                                           | Every string the UI shows                                             |
| `web/e2e/`                                                          | Playwright tests and the contact sheet script                         |
| `web/scripts/preflop-table.ts`                                      | Regenerates `src/brains/data/preflop-strength.json`                   |
| `web/tools/golden/`                                                 | JVM harness that writes the golden fixtures, see its `README.md`      |
| `.github/workflows/`                                                | `web-ci.yml` on pull requests, `web-deploy.yml` on `master`           |

Generated and never committed: `web/dist`, `web/test-results`, `web/playwright-report`, `web/e2e/contact/` and
`web/tools/golden/build`.

## Stack

Node 24, Vite, TypeScript strict, Preact, zustand, i18next, Vitest with `fast-check`, Playwright, ESLint
(typescript-eslint, `eslint-plugin-import-x`) and Prettier. Hand evaluation is `phe`, behind the `HandEvaluator`
interface.

## Layers

```
web/src/
  cards/    Card, parse and format ("As Kd"), seeded RNG, shuffle
  equity/   hand evaluation and equity
  engine/   heads-up NLHE rules as a pure reducer, tournament and blind schedule, hand history
  brains/   port of the Mallorca math bot
  game/     session store, turn loop, bot worker client, view-model
  ui/       Preact components, layout, tokens, i18n
```

| Layer    | May import from                       |
| -------- | ------------------------------------- |
| `cards`  | nothing                               |
| `equity` | `cards`                               |
| `engine` | `cards`, `equity`                     |
| `brains` | `cards`, `equity`, `engine`           |
| `game`   | `cards`, `equity`, `engine`, `brains` |
| `ui`     | `game`                                |

`brains` takes only types from `engine`: `Action`, `LegalActions` and the events it turns into observations.
`ui` imports only from `game`; for the table, `game` re-exports `Card`, `formatCard`, `rankOf`, `suitOf`,
`SeatIndex`, `LogEntry` and `HandCategory`.

ESLint enforces the table with a `@typescript-eslint/no-restricted-imports` rule per directory. For `brains` the
rule sets `allowTypeImports` on `engine`; the tests in `brains` (`*.test.ts`) may also import `engine` code to
build events. `import-x/no-cycle` forbids import cycles inside `src`.

### Purity

`cards`, `equity`, `engine` and `brains` use no DOM, no `Math.random`, no `Date` and no `performance`. ESLint
enforces it with `no-restricted-globals` and `no-restricted-properties`. Randomness enters as an `Rng` argument.

## cards

- `Card` is a number 0..51, `rank * 4 + suit`. `rankOf` returns 2..14, `suitOf` returns 0..3 for `c d h s`.
- `parseCard("As")`, `parseCards("As Kd")`, `formatCard`, `fullDeck()`.
- `Rng` has `next(): number` in [0, 1). `mulberry32(seed)` implements it. `shuffle(items, rng)` is Fisher-Yates
  and returns a copy.

## equity

- `HandEvaluator` has `rank(cards)` for 5 to 7 cards, higher is better, and `category(rank)`, a `HandCategory`:
  `high-card`, `pair`, `two-pair`, `trips`, `straight`, `flush`, `full-house`, `quads`, `straight-flush`.
  `pheEvaluator` implements it over `phe`.
- `equityVsHand(hero, villain, board)` enumerates the remaining board exactly.
- `equityVsRange(hero, range, board, { rng, samples })` weighs a `WeightedHands` range: exact on the turn and river,
  seeded Monte Carlo on earlier streets.
- `streetEquity(hero, range, board)` returns `StreetEquity`: `strength`, `positivePotential` and
  `negativePotential` on a board of 3 to 5 cards. `realEquity` combines them.
- `src/equity/golden.test.ts`, part of `npm test`, checks `streetEquity` against 100 equities of the Java
  `PokerEquilatorBrecher`, within 0.02 on the flop and 0.01 on the turn and river.

## engine

- `createTournament(names, startingStack = 2000, firstButton = 0)` returns a `Tournament`:
  `{ seats, button, handNumber, hand, winner }`. A `Seat` is `{ name, stack }`.
- `startHand(t, rng)` moves the button (the first hand uses `firstButton`, then it alternates), sets the blind
  level, shuffles and deals. `apply(t, action)` applies an `Action`: `fold`, `check`, `call` or `raise` with `to`.
  Both return `{ state, events }`. `legalActionsOf(t)` and `toAct(t)` read the current hand.
- `startHandWith(seats, { button, blinds, handNumber }, deck)`, `applyAction(hand, seats, action)` and
  `legalActions(hand, seats)` are the hand-level reducer; `applyAction` throws `IllegalActionError` for anything
  `legalActions` does not offer.
- `startHandWith` takes an already shuffled deck and deals from its front. Hole cards alternate starting with the
  seat after the button: `deck[0]` and `deck[2]` go to the big blind seat, `deck[1]` and `deck[3]` to the button.
  The board follows in order with no burn cards: flop `deck[4..6]`, turn `deck[7]`, river `deck[8]`. `Hand.deck`
  holds the undealt cards.
- `Hand.bets` is the street bet per seat, `committed` the total per seat for the hand, `pot` is the sum of
  `committed`. `canRaise` is false for a seat that faces a short all-in raise.
- `seats` stacks are the chips behind. `LegalActions` is `{ fold, check, call, raise }`: `call` is the amount the
  call adds, `raise` amounts are "raise to" the street total and `max` is all-in. `legalActions` returns no action
  when nobody is to act.
- Events: `handStarted`, `blindPosted`, `holeCards`, `acted`, `streetDealt`, `showdown`, `betReturned`,
  `potAwarded`, `handEnded`, `tournamentEnded`. `acted.amount` is the chips added to the pot by that action.
  `showdown.hands` is indexed by seat; `cards` are the hole cards.
- When a hand ends, `street` is `complete`, `toAct` is null, `pot` is 0 and the stacks include the awarded pot.
- `startHand` and `apply` both end the tournament when a hand completes with an empty stack: they set `winner` and
  append `tournamentEnded`. A hand can complete inside `startHand` when a short stack is all-in from the blinds.
- `handHistory(events)` returns `LogEntry` values, `{ key, params }`, with seat indices in `params.seat` and no
  names or English text. `key` is a locale key under `log` (with `you` or `other` for entries that have a seat);
  `params.category` is a `HandCategory`.

### Rules

- The button posts the small blind and acts first preflop, last on later streets.
- The minimum raise is the last raise increment, at least the big blind. An all-in for less than a full raise does
  not reopen betting for a player who already acted; a full raise does.
- A bet larger than the opponent's stack is capped: the uncalled excess returns to its owner as `betReturned`,
  before `potAwarded`. `potAwarded` holds only the matched pot, twice the smaller contribution.
- A player who cannot cover a blind posts all-in for what they have. When both players are all-in, or one is
  all-in and the other has called, the board runs out to showdown with no betting.
- A tie splits the pot; the odd chip goes to the big blind.
- Starting stack 2000. `BIG_BLINDS` holds the big blind of each level:
  `20, 30, 40, 50, 60, 80, 100, 120, 160, 200, 240, 300, 400, 500, 600, 800, 1000, 1200, 1600, 2000, 2400`.
  The small blind is half, rounded down. `blindsFor(handNumber)` raises the level every 10 hands and holds the
  last level.
- The tournament ends when a stack reaches zero.

Tests: `scenarios.test.ts` is the rule table, `properties.test.ts` holds the `fast-check` properties (chips are
conserved, every legal action is accepted, every random legal sequence ends the hand, awarded pots match what was
committed).

## brains

A port of the MallorcaBrains math bot and the parts of MallorcaCore and MallorcaEquilator it uses. Preflop charts,
push/fold, neural opponent models, hand history parsers, the robot and the Swing UI are not ported.

| Module                 | Holds                                                                            |
| ---------------------- | -------------------------------------------------------------------------------- |
| `observation.ts`       | `Observation`, `toObservations(events, heroSeat)`, `parseObservation`            |
| `handState.ts`         | `HandState`, `GameContext`, `HandStateObserver`: the bot's view of the hand      |
| `spectrum.ts`          | `Spectrum`: a weight per villain hole-card pair                                  |
| `villainModel.ts`      | `VillainModel`, `randomVillainModel`, `VillainSpectrumTracker`                   |
| `strength.ts`          | `StrengthManager`: hero equity per street against the spectrum and at random     |
| `spectrumEquilator.ts` | `SpectrumEquilator`: hero strength against a spectrum on a board, with its cache |
| `preflopEquilator.ts`  | Preflop strength from `data/preflop-strength.json`                               |
| `solver.ts`            | `NLGameSolver`, `ShowdownGameSolver`: the game tree, as `SolverNode`s            |
| `profit.ts`            | `profitMap`, `SolverRequest`, `HeroKnowledge`, `RandomVariable`, raise sizing    |
| `adviceFromMap.ts`     | `LessVarianceActionFromMap`: picks the action class from the action distribution |
| `preprocess.ts`        | `preprocessAction`, `toLegalAction`: resizes and clamps the chosen action        |
| `brain.ts`             | `createMathBrain`, `Brain`, `Decision`                                           |
| `javaCard.ts`          | The Java card numbering, for hashes that must match the original                 |

- `Observation` is the bot's view of engine events: `handStarted` (hand number, `heroOnButton`, both stacks,
  blinds), `holeCards` (its own), `acted` (`who` is `hero` or `villain`, the `Action` and the chips added) and
  `street` with the board. `toObservations` drops the villain's hole cards and the showdown.
- `createMathBrain({ villainModel })` returns a `Brain` with `observe(o)` and `decide(legal)`. It enumerates
  equities exactly and takes no `Rng`.
- `randomVillainModel` returns the probabilities the Java `RandomBot` samples from: facing a bet fold, passive and
  aggressive 1/3 each, otherwise passive and aggressive 1/2 each. It is never sampled.
- `profitMap(solver, game, situation, knowledge)` builds the `ActionDistribution`; `knowledge` is the
  `HeroKnowledge` (`hero`, `spectrum`, `strength`). The solver gets one `SolverRequest` per call (game, hand
  state, effective stack, pot, spectrum, hero, strength, button).
- `decide` returns `{ action, solverChoice, evByAction }`. `action` is always legal under `legal`. `solverChoice`
  is the class (`fold`, `passive`, `aggressive`) the advice creator picked, before preprocessing and clamping.
- `evByAction` holds the expected profit of each class in chips, relative to folding now: `fold` is 0 at the top
  of the tree.
- `passive` counts as won only the chips in the pot from the current street, after the call: on the flop, turn
  and river the pot at the end of the previous street is left out; preflop the whole pot counts. The loss is the
  chips the call adds.
- `aggressive` is the profit of the solver's own raise size (`createRaiseAction` sizing), before the preprocessor
  resizes it and before it is clamped into `legal.raise`. When the action ends up as a call or check because no
  raise is legal, `aggressive` still refers to that unclamped raise.
- A class the solver did not consider (no raise when the effective stack is 0) is `Number.NEGATIVE_INFINITY`.
- `data/preflop-strength.json` is the 169 by 169 preflop table, regenerated with our equity code by
  `node scripts/preflop-table.ts`; `preflopTable.test.ts` checks every cell against the Java table within 0.01.
- `npm run test:golden` (`src/brains/decisions.golden.test.ts`) replays 674 decisions of the Java bot and expects
  the same action and each class's EV within 0.5 % of the pot plus half a chip. It is not part of `npm test`. A
  mismatch is a porting bug.

## game

- `createSession({ bot, seed, botDelayMs, humanSeat, names, startingStacks? })` returns a zustand store with
  `SessionState` and the actions `start`, `nextHand`, `act(intent)` and `dispose`. `seed` drives `mulberry32` for
  every shuffle of the tournament. The actions are module functions over one shared `Runtime` per session: the
  store accessors, the bot, the RNG, the timers and an epoch that `start` and `dispose` advance.
- `phase` is one of `idle` (before `start`), `humanToAct`, `botThinking` (waiting for `decide`), `animating`
  (the `botDelayMs` pause before the bot's action is applied), `handOver` and `tournamentOver`. `act` is ignored
  unless the phase is `humanToAct`, and `nextHand` unless it is `handOver`.
- Turn loop: after each step the session forwards the events to the bot as observations, then either waits for
  the human or asks the bot to decide and applies its action after `botDelayMs`.
- `Intent` is `fold`, `passive` (check or call) or `raise` with a target. `resolveIntent` turns it into a legal
  action: a raise is rounded and clamped into `legal.raise`, a fold becomes a check when checking is free.
- `log` holds the `LogEntry` values of the tournament. `lastActions` holds one tag per seat for the current hand,
  `{ key, params }` with `key` under `actions` (`fold`, `check`, `call`, `raiseTo`, `allIn`).
- `result` is set when a hand completes: `winner` (null for a split), `category` of the shown hand or null,
  `showdown` and `amount`. `amount` is the pot collected, twice the smaller contribution, or each share for a
  split. It equals the `win` entry of the log; an uncalled bet is its own `returned` entry.
- `BotClient` has `observe(observations)`, `decide(legal)`, `reset()` and an optional `dispose()`. `workerBot()`
  runs the brain in `bot.worker.ts` over the `WorkerRequest` and `WorkerReply` messages in `workerProtocol.ts`;
  `createWorkerHandler(newBrain, post)` in `workerHandler.ts` is the worker's message handling as a plain
  function. `inProcessBot()` runs the brain on the main thread.
- `withFallback` wraps a client: when `decide` rejects or takes longer than 5 s, it disposes the primary, replays
  the observations of the hand into an in-process bot and uses it from then on. It falls back once, and a failure
  from before `reset` or `dispose` does not trigger it. The session drops a decision that arrives after `start`
  or `dispose`.
- `toViewModel(state, humanSeat)` returns the `ViewModel`: per seat the name, stack, street bet, button, whether
  it is to act, the cards (`'back'` for the bot's cards until a showdown) and the last action tag; the board, pot,
  blinds and hand number; the controls (`fold`, `passive` with its amount, `raise` with `min`, `max` and the
  presets `halfPot`, `pot` and `allIn`, and `enabled`); the result and the tournament winner.
- `simulation.test.ts` plays full tournaments between the bot and a seeded random human through the session and
  checks that chips are conserved, the session never stalls and the winner holds every chip.

## ui

- Preact components render only the `ViewModel`, the session phase and the log; they call the session actions
  and hold no game rules.
- Components: `App`, `StartScreen`, `Table`, `Seat`, `Card` (SVG), `Board`, `Pot`, `ActionBar`, `BetSlider`,
  `HandLog`, `TournamentOver`.
- Layout: portrait for phones (bot at the top, the human's cards and controls at the bottom), landscape for wide
  screens with the hand log at the side, switched by a `min-aspect-ratio: 1/1` media query. The `coach` panel is
  the slot for a later training mode and is empty.
- Every colour, font, radius, spacing and shadow is a CSS token on `:root` in `ui/styles/tokens.css`, with a dark
  theme under `prefers-color-scheme: dark`. The visual design brief is GitHub issue #2.
- Every string comes from `src/locales/en.json` through i18next (`ui/i18n.ts`). `ui/phrases.ts` words log
  entries, action tags and hand categories; an entry with the human seat uses the `log.you` keys, any other seat
  the `log.other` keys with `{{name}}`.
- `main.tsx` picks a random seed per tournament and reads two query parameters: `?seed=<n>` fixes the tournament
  seed and `?botDelay=<ms>` sets the bot pause (700 ms by default). The human sits in seat 0.
- Keyboard: `f` folds, `c` checks or calls, `r` raises, the arrow keys move the raise amount.
- `data-testid` contract for e2e and contact sheets: `start`, `blinds`, `hand-number`, `new-tournament`,
  `seat-<i>`, `stack-<i>` (with `data-stack`), `bet-<i>`, `action-<i>`, `thinking-<i>`, `card` (with
  `data-card`), `card-back`, `board`, `pot` (with `data-amount`), `result`, `next-hand`, `fold`, `passive`,
  `raise`, `raise-amount`, `preset-half`, `preset-pot`, `preset-allin`, `hand-log`, `coach`, `live-region`,
  `tournament-over` and `play-again`.

## e2e

- Playwright runs against the production build (`vite preview`) on three projects: `phone` 390 × 844,
  `small-phone` 360 × 740 and `desktop` 1440 × 900.
- `game.spec.ts`: a passive hand keeps the chips, all-in every hand ends the tournament and "Play again" returns
  to a fresh table, a reload mid-hand shows the start screen. `layout.spec.ts`: the start screen and the table fit
  the viewport without horizontal scroll.
- `e2e/contact.ts` (`npm run contact`) captures every view state of a seeded game on `phone` and `desktop` and
  composes one sheet per device into `e2e/contact/`.

## Conventions

- Chips are integers.
- Cards in fixtures and text are two-character strings: rank `23456789TJQKA`, suit `cdhs`.
- Tests sit next to the code as `*.test.ts` or `*.test.tsx`; golden tests are `*.golden.test.ts` and run only
  under `npm run test:golden`.
- Fixtures generated from the Java original live in `src/equity/fixtures/` and `src/brains/fixtures/` and are
  written only by `web/tools/golden/run.sh`.
