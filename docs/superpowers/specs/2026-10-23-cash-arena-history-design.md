# Cash mode, arena and hand history

Issue: #3. Groundwork for the CFR brain (#4) and the coach mode (#5).

## Goal

- A heads-up cash game with 100 bb stacks, next to the existing tournament.
- A headless arena that measures one brain against another in bb/100 with a confidence interval.
- A history of every hand played in the browser, stored on the client and exportable.

Success:

- A cash session is playable on phone and desktop, from the start screen to leaving the table.
- `npm run arena` plays duplicate pairs between two brains and prints bb/100 with a 95% confidence interval.
- Every finished hand of a browser session is stored and can be exported as JSON and as PokerStars text.
- The golden tests of the bot pass unchanged.

Out of scope: the CFR brain, difficulty levels, server storage, the coach panel.

## Decisions

| Topic            | Decision                                                                                        |
| ---------------- | ----------------------------------------------------------------------------------------------- |
| Stacks in cash   | Before each hand a seat with less than 100 bb is topped up to 100 bb; the other keeps its chips |
| Cash session     | Endless; the running result is always visible; the player leaves the table to end it            |
| Blinds in cash   | 10/20, buy-in 2000 chips, the tournament starting stack                                         |
| History format   | Own JSON record; export as JSON or PokerStars text                                              |
| History storage  | IndexedDB in the browser, an in-memory store as fallback and for tests                          |
| Arena            | Node CLI, duplicate pairs, worker threads, JSON report, optional saving of hands                |
| Engine structure | `Tournament` generalises to `Table` with mode rules                                             |

With the top-up rule the effective stack before every cash hand is exactly 100 bb, the depth the CFR strategy
is trained for.

## Engine

`Tournament` becomes `Table`:

```ts
type TableMode =
  { kind: "tournament" } | { kind: "cash"; bigBlind: number; buyIn: number };

interface Table {
  mode: TableMode;
  seats: [Seat, Seat];
  button: SeatIndex;
  handNumber: number;
  hand: Hand | null;
  winner: SeatIndex | null;
}
```

The table stays plain data. `rulesOf(mode)` returns the rules:

```ts
interface Rules {
  blinds(handNumber: number): { small: number; big: number };
  beforeHand(seats: [Seat, Seat]): {
    seats: [Seat, Seat];
    events: EngineEvent[];
  };
  isOver(seats: [Seat, Seat]): SeatIndex | null;
}
```

- Tournament rules: `blindsFor`, no top-up, over when a stack is 0, the other seat wins. Behaviour is exactly
  today's.
- Cash rules: blinds `bigBlind / 2` and `bigBlind`, every seat below `buyIn` is topped up to `buyIn`, never
  over.
- `createTable(names, mode, firstButton)`, `startHand(table, rng)`, `apply(table, action)`, `legalActionsOf`,
  `toAct` keep their current behaviour for the tournament. `createTable` seats both players with the
  tournament starting stack or the cash buy-in.
- New event `{ type: 'toppedUp'; seat: SeatIndex; amount: number }`, emitted by `startHand` before
  `handStarted`, so `handStarted.stacks` already include it. `handHistory` turns it into a log entry.
- `tournamentEnded` stays and occurs only in the tournament.
- `startHandWith` is unchanged; the arena calls it with its own deck.

The brain reads blinds and stacks from `handStarted`, so `brains` needs no change. `toObservations` ignores
`toppedUp`.

Tests: the tournament tests move to `Table` with unchanged expectations. New tests cover the cash blinds, the
top-up of one or both seats, a cash table never having a winner, and `toppedUp` in the log. A property test
plays random cash hands and checks that both stacks before every hand are at least `buyIn` and the smaller
one equals it.

## Session and UI

- `SessionOptions` gains `mode: TableMode`. `SessionState.tournament` becomes `table`.
- `SessionState.session` is `{ hands: number; net: number }` for the human seat: `net` is the current stack
  minus the buy-in minus the sum of the human's top-ups, in chips.
- `leave()` is accepted in a cash session in the phase `handOver` and moves to the new phase `sessionOver`.
- The view-model shows the result in bb with one decimal, the hand count and, in `sessionOver`, bb/100.
- Start screen: two buttons, cash 100 bb and tournament, and the seed as today.
- Status bar in cash: the blinds and the running result instead of the blind level.
- The session-over screen shows the result in bb, bb/100 and hands, with play again and export history.
- Copy follows the UI rules: no all-caps words, no em or en dashes as punctuation, English through i18next.

Tests: unit tests of `net` with top-ups on either side; a session test that plays several cash hands and
leaves; the view-model in both modes; e2e for a cash session on phone and desktop including leaving the table.

## Hand history

The module is `game/records/`, named apart from the engine's `handHistory` log.

```ts
interface HandRecord {
  version: 1;
  id: string;
  sessionId: string;
  mode: TableMode;
  handNumber: number;
  playedAt: string;
  names: [string, string];
  humanSeat: SeatIndex | null;
  bot: { id: string; version: string };
  holeCards: [[string, string], [string, string]];
  board: string[];
  events: EngineEvent[];
  decisions: {
    eventIndex: number;
    solverChoice: string;
    evByAction: Record<string, number>;
  }[];
}
```

- `handRecord(events, meta)` is a pure function. `meta` carries the ids, names, seats, bot and `playedAt`,
  which the caller takes from the clock. `humanSeat` is null for arena hands, where both seats are bots.
- `toPokerStars(records)` returns the text format. Chip amounts without currency, `10/20` blinds, the button
  seat, `Dealt to` the human's cards, the bot's cards only when the hand reaches a showdown, the summary with
  the pot and the board.
- `HandStore` has `add(record)`, `list(sessionId?)`, `clear()`. `indexedDbStore()` uses the database
  `mallorca`, object store `hands`, index `sessionId`. `memoryStore()` keeps an array.
- The session writes a record when a hand completes. When IndexedDB cannot open, the app uses `memoryStore`;
  a failed write is ignored and does not stop the game.
- Export, from the session-over screen and the menu, for the current session or all hands: a `.json` file
  with an array of records, or a `.txt` file in PokerStars format. The menu also offers clearing the history.
- Infinite values in `evByAction` are stored as null, since JSON has no infinity.

Tests: `handRecord` on scripted hands (fold preflop, showdown, all-in run-out, top-up); `toPokerStars`
against reference texts; one contract test suite run against `memoryStore`; IndexedDB covered by e2e in a
real browser. No new npm dependencies.

## Arena

`web/tools/arena/`, run as `npm run arena -- --a math --b always-call --pairs 5000 --seed 1`, with Node
running the TypeScript directly like `npm run contact`.

- `match.ts`: `playPair(a, b, seed, index)` builds the deck of the pair from `mulberry32` seeded by
  `(seed, index)` and plays it twice, brain `a` on seat 0 then on seat 1, same button seat. Cash rules with
  both stacks reset to 2000 before each hand, so the two hands of a pair start alike. Returns the net chips of
  `a` over the pair and the events of both hands.
- `bots.ts`: a registry of `Brain` factories by name: `math` (`createMathBrain` with `randomVillainModel`),
  `always-call`, `always-raise` (minimum raise, all-in when only that is legal), `random` (uniform over legal
  action kinds, seeded). #4 adds its brains here.
- `stats.ts`: the sample is the net of each pair. bb/100 is the mean over pairs divided by 2, by the big
  blind, times 100. The 95% interval is 1.96 times the standard deviation of that per-pair value over the
  square root of the number of pairs.
- `cli.ts`: flags `--a`, `--b`, `--pairs`, `--seed`, `--workers` (default `os.availableParallelism()`),
  `--out report.json`, `--save-hands hands.jsonl`. Splits pair indices across `worker_threads` and gathers the
  results by index, so the result depends only on the seed.
- Output: a table with bb/100, the interval, hands and time; the report adds the parameters and the git
  commit. `--save-hands` writes one `HandRecord` per line.
- `tools/` may use the clock and Node APIs; the purity rule applies only to `src` layers.

Speed: the golden suite runs 674 decisions of the math bot in about 42 s on one core, roughly 60 ms per
decision. With about three bot decisions per hand that is near 0.2 s per hand per core; 5000 pairs against
a baseline take a few minutes on 11 workers.

Tests, part of `npm test`: the math bot against itself over duplicate pairs nets exactly 0, since it is
deterministic and the cards mirror; the same seed gives the same report with 1 and 4 workers; `stats` on
known samples; a smoke run of 20 pairs of `math` against `always-call`.

## Risks

- Renaming `Tournament` touches `game`, `ui` and many tests. It is mechanical and done first, in its own
  commit, with all tests green before cash rules are added.
- The arena's throughput is bound by the math bot. If it is too slow for useful intervals, the plan measures
  and records it; the CFR brain of #4 decides by lookup and will be much faster.
