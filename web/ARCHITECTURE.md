# Architecture

## Package layout

```
web/
  src/cards/     Card, parse and format ("As Kd"), seeded RNG, shuffle
  src/equity/    hand evaluation and equity
  src/engine/    heads-up NLHE rules as a pure reducer, table with a mode and blind schedule
  src/brains/    port of the Mallorca math bot
  src/game/      session store, turn loop, bot worker client, view-model
  src/ui/        Preact components
  e2e/           Playwright tests
  tools/golden/  JVM harness that writes golden fixtures from the Java original
```

## Layers

| Layer    | May import from                       |
| -------- | ------------------------------------- |
| `cards`  | nothing                               |
| `equity` | `cards`                               |
| `engine` | `cards`, `equity`                     |
| `brains` | `cards`, `equity`, `engine`           |
| `game`   | `cards`, `equity`, `engine`, `brains` |
| `ui`     | `game`                                |

`brains` takes only types from `engine`: `Action` and observations.

`ui` imports only from `game`. For the table, `game` re-exports `Card`, `formatCard`, `rankOf` and `suitOf`
from `cards`.

ESLint enforces the table with a `@typescript-eslint/no-restricted-imports` rule per directory. For `brains` the
rule sets `allowTypeImports` on `engine`; the tests in `brains` (`*.test.ts`) may also import `engine` code to build
events.

## Purity

`cards`, `equity`, `engine` and `brains` use no DOM, no `Math.random`, no `Date` and no `performance`.
ESLint enforces it with `no-restricted-globals` and `no-restricted-properties`. Randomness enters as an `Rng`
argument.

## cards

- `Card` is a number 0..51, `rank * 4 + suit`. `rankOf` returns 2..14, `suitOf` returns 0..3 for `c d h s`.
- `parseCard("As")`, `parseCards("As Kd")`, `formatCard`, `fullDeck()`.
- `Rng` has `next(): number` in [0, 1). `mulberry32(seed)` implements it. `shuffle(items, rng)` is Fisher-Yates
  and returns a copy.

## engine

- `startHandWith(seats, button, blinds, deck, handNumber)` takes an already shuffled deck and deals from its front.
  Hole cards alternate starting with the seat after the button: `deck[0]` and `deck[2]` go to the big blind seat,
  `deck[1]` and `deck[3]` to the button. The board follows in order with no burn cards: flop `deck[4..6]`, turn
  `deck[7]`, river `deck[8]`. `Hand.deck` holds the undealt cards.
- `Hand.bets` is the street bet per seat, `committed` the total per seat for the hand, `pot` is the sum of
  `committed`. `canRaise` is false for a seat that faces a short all-in raise.
- `seats` stacks are the chips behind. `legalActions` raise amounts are "raise to" the street total and `max` is
  all-in. An uncalled excess returns to its owner as `betReturned`, before `potAwarded`; `potAwarded` holds only
  the matched pot, twice the smaller contribution.
- `acted.amount` is the chips added to the pot by that action. When a hand ends, `street` is `complete`, `toAct`
  is null, `pot` is 0 and the stacks include the awarded pot.
- `showdown.hands` is indexed by seat; `cards` are the hole cards.
- `legalActions` returns no action when nobody is to act.
- `Table` (`engine/table.ts`) holds `mode`, `seats`, `button`, `handNumber`, `hand` and `winner`. `TableMode` is
  `{ kind: 'tournament' }` or `{ kind: 'cash', bigBlind, buyIn }`; `createTable(names, mode, firstButton)` seats
  2000 chips for `TOURNAMENT` and `buyIn` for a cash mode such as `CASH`.
- `startHand(t, rng)` and `apply(t, action)` both end the tournament when a hand completes with an empty stack:
  they set `winner` and append `tournamentEnded`. A hand can complete inside `startHand` when a short stack is
  all-in from the blinds.
- `rulesOf(mode)` returns `Rules`: `blinds(handNumber)`, `beforeHand(seats)` and `isOver(seats)`. Tournament rules use
  `blindsFor`, no top-up, and end when a seat is empty. Cash rules use fixed blinds of half and full `bigBlind`,
  top every seat below `buyIn` up to it before the hand and emit `toppedUp { seat, amount }` ahead of `handStarted`,
  and never end the table. `startHand` and `apply` take the blinds and the winner from the rules.
- `blindsFor(handNumber)` raises the level every 10 hands and holds the last level.
- `handHistory(events)` returns `LogEntry` values, `{ key, params }`, with seat indices in `params.seat` and no
  names or English text. `key` is a locale key under `log` (with `you` or `other` for entries that have a seat);
  `params.category` is a `HandCategory`.

## brains

- `createMathBrain({ villainModel })` returns a `Brain` with `observe(o)` and `decide(legal)`. It enumerates
  equities exactly and takes no `Rng`.
- `decide` returns `{ action, solverChoice, evByAction }`. `action` is always legal under `legal`. `solverChoice`
  is the class (`fold`, `passive`, `aggressive`) the advice creator picked, before preprocessing and clamping.
- `evByAction` holds the expected profit of each class in chips, relative to folding now: `fold` is 0 at the
  top of the tree.
- `passive` counts as won only the chips in the pot from the current street, after the call: on the flop, turn and
  river the pot at the end of the previous street is left out; preflop the whole pot counts. The loss is the
  chips the call adds.
- `aggressive` is the profit of the solver's own raise size (`Action.createRaiseAction` sizing), before the
  preprocessor resizes it and before it is clamped into `legal.raise`. When the action ends up as a call or check
  because no raise is legal, `aggressive` still refers to that unclamped raise.
- A class the solver did not consider (no raise when the effective stack is 0) is `Number.NEGATIVE_INFINITY`.
- `npm run test:golden` checks 674 decisions of the Java bot (`src/brains/decisions.golden.test.ts`); it is
  not part of `npm test`.

## game

- `createSession({ bot, seed, botDelayMs, humanSeat, names, mode, startingStacks? })` returns a zustand store with
  `SessionState` (its engine state is `table`) and the actions `start(mode?)`, `nextHand`, `act(intent)`, `leave` and
  `dispose`. `mode` is a `TableMode`; `start(mode)` replaces the session's mode for this and later starts, and
  `start()` keeps the current one. `seed` drives `mulberry32` for every shuffle.
- `phase` is one of `idle` (before `start`), `humanToAct`, `botThinking` (waiting for `decide`), `animating`
  (the `botDelayMs` pause before the bot's action is applied), `handOver`, `tournamentOver` and `sessionOver`. `act`
  is ignored unless the phase is `humanToAct`, and `nextHand` unless it is `handOver`. `leave` moves a cash session
  from `handOver` to `sessionOver`, where `act` and `nextHand` do nothing; in any other mode or phase it is ignored.
- `session` is `{ hands, net }` for the human seat: `hands` counts completed hands and
  `net = stack - buyIn - top-ups`, where `buyIn` is the mode's in cash and 2000 in a tournament. Mid-hand `net`
  includes the blinds already posted; after a hand it is the result.
- `Intent` is `fold`, `passive` (check or call) or `raise` with a target. `resolveIntent` turns it into a legal
  action: a raise is rounded and clamped into `legal.raise`, a fold becomes a check when checking is free.
- `log` holds the `LogEntry` values of the tournament. `lastActions` holds one tag per seat for the current hand,
  `{ key, params }` with `key` under `actions` (`fold`, `check`, `call`, `raiseTo`, `allIn`).
- `result` is set when a hand completes: `winner` (null for a split), `category` of the shown hand or null,
  `showdown` and `amount`. `amount` is the pot collected, twice the smaller contribution, or each share for a
  split. It equals the `win` entry of the log; an uncalled bet is its own `returned` entry.
- `BotClient` has `observe(observations)`, `decide(legal)`, `reset()` and an optional `dispose()`. `workerBot()`
  runs the brain in `bot.worker.ts` over the `WorkerRequest` and `WorkerReply` messages in `workerProtocol.ts`.
  `inProcessBot()` runs it on the main thread.
- `withFallback` wraps a client: when `decide` rejects or takes longer than 5 s, it disposes the primary, replays
  the observations of the hand into an in-process bot and uses it from then on. It falls back once, and a failure
  from before `reset` or `dispose` does not trigger it. The session drops a decision that arrives after `start`
  or `dispose`.
- `toViewModel(state, humanSeat)` returns the `ViewModel`: per seat the name, stack, street bet, button, whether
  it is to act, the cards (`'back'` for the bot's cards until a showdown) and the last action tag; the board, pot,
  blinds (from `rulesOf(mode).blinds`) and hand number; the `mode` (`cash` or `tournament`); `session` with
  `hands`, `netBb` and `bbPer100` (one decimal, null with no hands); the controls (`fold`, `passive` with its amount, `raise` with `min`, `max` and the
  presets `halfPot`, `pot` and `allIn`, and `enabled`); the result and the tournament winner.
- `SessionOptions.records` is `{ store, sessionId, now }`; without it nothing is recorded and `sessionId` in the state is
  null. The session collects the events of each hand from `beginHand` on and, when the hand completes, calls
  `store.add(handRecord(...))` without awaiting it and ignores a rejection. A bot `Decision` is recorded with
  `recordDecision`; its `eventIndex` is the index of the resulting `acted` event in the hand's events. The timeout
  fallback plays without a decision and records none. `bot` is `MATH_BOT`, `playedAt` is `now()` at completion.
- `game/records` holds `HandRecord` (version 1: id `<sessionId>-<handNumber>`, mode, names, human seat, bot, hole
  cards, board, every engine event of the hand and the bot decisions with `evByAction` where a non-finite EV is
  null) and `toPokerStars(records)`, which formats records as PokerStars hand history text.
- `HandStore` has `add`, `list(sessionId?)` and `clear`. `list` returns records by `handNumber` ascending within one
  session; across sessions it returns every record. `memoryStore()` is a map. `indexedDbStore()` uses database
  `mallorca` version 1, object store `hands` keyed by `id` with index `sessionId`. `openHandStore()` returns the
  IndexedDB store, or `memoryStore()` when `indexedDB` is undefined or fails to open.
- For the ui, `game` re-exports `Card`, `formatCard`, `rankOf`, `suitOf`, `SeatIndex`, `LogEntry`, `HandCategory`,
  `TableMode`, `CASH` and `TOURNAMENT`, plus `HandRecord`, `HandStore`, `openHandStore`, `memoryStore` and
  `toPokerStars`.

## ui

- Preact components render only the `ViewModel`, the session phase and the log; they call the session actions
  and hold no game rules.
- Every string comes from `src/locales/en.json` through i18next (`ui/i18n.ts`). `ui/phrases.ts` words log
  entries, action tags and hand categories; an entry with the human seat uses the `log.you` keys, any other seat
  the `log.other` keys with `{{name}}`.
- `signed` in `ui` formats a bb value as sign and one-decimal absolute value. `main.tsx` reads two query parameters: `?seed=<n>` fixes the tournament seed and `?botDelay=<ms>` sets the bot
  pause (700 ms by default). The human sits in seat 0.
- Keyboard: `f` folds, `c` checks or calls, `r` raises, the arrow keys move the raise amount.
- `data-testid` contract for e2e and contact sheets: `start-cash`, `start-tournament`, `blinds`, `session-result`, `hand-number`, `leave`, `new-game`,
  `seat-<i>`, `stack-<i>` (with `data-stack`), `bet-<i>`, `action-<i>`, `thinking-<i>`, `card` (with
  `data-card`), `card-back`, `board`, `pot` (with `data-amount`), `result`, `next-hand`, `fold`, `passive`,
  `raise`, `raise-amount`, `preset-half`, `preset-pot`, `preset-allin`, `hand-log`, `coach`, `live-region`,
  `tournament-over`, `session-over`, `session-summary`, `session-rate` and `play-again`.

## Conventions

- Chips are integers.
- Cards in fixtures and text are two-character strings.
- Tests sit next to the code as `*.test.ts`.
