# Cash Mode, Arena and Hand History Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** A 100 bb heads-up cash mode next to the tournament, a client-side hand history with JSON and PokerStars export, and a headless duplicate arena that reports bb/100 between two brains.

**Architecture:** `engine` generalises `Tournament` into a plain-data `Table` whose `mode` selects `Rules` (blinds, top-up, end). `game` gains session results, a `sessionOver` phase and `game/records/` (record builder, PokerStars formatter, stores). `web/tools/arena/` plays duplicate pairs over `engine` and `brains` in Node worker threads.

**Tech Stack:** Node 24 (TypeScript run directly), Vite, TypeScript strict, Preact, zustand, i18next, Vitest + fast-check, Playwright, IndexedDB, `node:worker_threads`.

**Spec:** `docs/superpowers/specs/2026-10-23-cash-arena-history-design.md`

## Global Constraints

- Work in `web/`; run `source ~/.nvm/nvm.sh && nvm use` before npm commands. Java modules are not touched.
- Before every commit: `npx prettier --write <paths>`, `npx eslint <paths>`, `npx tsc --noEmit`, `npx vitest run <paths>`.
- TDD: the failing test comes first. Never loosen a tolerance, add an ignore or a suppression, or delete an assertion.
- No comments in code or config. UI copy: no all-caps words, no em or en dashes as sentence punctuation, English via i18next keys in `src/locales/en.json`.
- Layers import only downward: `ui → game → engine | brains → equity → cards`; `brains` takes only types from `engine`. `cards`, `equity`, `engine`, `brains` use no DOM, `Math.random`, `Date`, `performance`.
- Chips are integers. Cash: blinds 10/20, buy-in 2000. A seat below 2000 before a hand is topped up to 2000.
- Commits: conventional, layer as scope (`feat(engine): ...`), on branch `feat/cash-arena-history`. Run `git branch --show-current` before committing.
- No new npm dependencies.
- `npm run test:golden` stays green with no change to `src/brains`.

## Review Focus

- A cash player who loses an all-in to 0 chips: the next hand tops them up to 2000 and the session goes on, never `tournamentOver` (Task 2, Task 3).
- The bot busts while the human holds 3400: only the bot is topped up and the human's `net` stays +1400 (Task 3).
- IndexedDB missing or a write that rejects: the hand completes, the next hand deals, no error reaches the UI (Task 7).
- A bot class the solver skipped (`evByAction` of `-Infinity`) survives `JSON.stringify` as null and a reload of the export (Task 5).
- `leave()` mid-hand or `act()` after leaving is ignored; a split pot and an all-in run-out format correctly in PokerStars text (Task 3, Task 6).

---

### Task 1: Rename `Tournament` to `Table` with a mode

Mechanical rename, behaviour unchanged.

**Files:**

- Rename: `web/src/engine/tournament.ts` → `web/src/engine/table.ts`, `tournament.test.ts` → `table.test.ts`
- Modify: `web/src/engine/index.ts`, `web/src/game/session.ts`, `web/src/game/viewModel.ts`, all `*.test.ts(x)` in `game` and `ui`, `web/e2e/*` where they read state, `web/ARCHITECTURE.md`

**Interfaces:**

- Produces:
  - `type TableMode = { kind: 'tournament' } | { kind: 'cash'; bigBlind: number; buyIn: number }`
  - `interface Table { mode: TableMode; seats: [Seat, Seat]; button: SeatIndex; handNumber: number; hand: Hand | null; winner: SeatIndex | null }`
  - `createTable(names: [string, string], mode: TableMode, firstButton?: SeatIndex): Table`; seats get 2000 for the tournament, `buyIn` for cash
  - `startHand(t: Table, rng: Rng)`, `apply(t: Table, action)`, `legalActionsOf(t: Table)`, `toAct(t: Table)` with today's signatures otherwise
  - `SessionState.table` replaces `SessionState.tournament`
  - Exports `TOURNAMENT: TableMode = { kind: 'tournament' }` and `CASH: TableMode = { kind: 'cash', bigBlind: 20, buyIn: 2000 }` from `engine`

- [ ] **Step 1:** `git mv` the two files, rename the type, `createTournament` → `createTable(names, TOURNAMENT, firstButton)`, `state.tournament` → `state.table` everywhere. `createTable` with `TOURNAMENT` must equal today's `createTournament(names, 2000, firstButton)` plus `mode`.
- [ ] **Step 2:** Add to `table.test.ts`:

```ts
it("creates a cash table with the buy-in on both seats", () => {
  const t = createTable(["A", "B"], CASH);
  expect(t.mode).toEqual({ kind: "cash", bigBlind: 20, buyIn: 2000 });
  expect(t.seats.map((s) => s.stack)).toEqual([2000, 2000]);
});
```

- [ ] **Step 3:** Run `npx tsc --noEmit && npm test && npm run test:golden`. Expected: all green, test count is today's plus one.
- [ ] **Step 4:** Update `ARCHITECTURE.md` (engine and game sections) to the new names. Commit `refactor(engine): generalise the tournament into a table with a mode`.

### Task 2: Mode rules, cash top-up and the `toppedUp` event

**Files:**

- Create: `web/src/engine/rules.ts`, `web/src/engine/rules.test.ts`
- Modify: `web/src/engine/table.ts`, `web/src/engine/types.ts`, `web/src/engine/history.ts`, `web/src/engine/history.test.ts`, `web/src/engine/properties.test.ts`, `web/src/engine/index.ts`, `web/src/locales/en.json`

**Interfaces:**

- Consumes: Task 1 `Table`, `TableMode`
- Produces:
  - `interface Rules { blinds(handNumber: number): { small: number; big: number }; beforeHand(seats: [Seat, Seat]): { seats: [Seat, Seat]; events: EngineEvent[] }; isOver(seats: [Seat, Seat]): SeatIndex | null }`
  - `rulesOf(mode: TableMode): Rules`, exported from `engine`
  - `EngineEvent` gains `{ type: 'toppedUp'; seat: SeatIndex; amount: number }`
  - Log entry `{ key: 'toppedUp', params: { seat, amount } }`

- [ ] **Step 1: Failing tests in `rules.test.ts`**

```ts
it("cash blinds are half and full big blind for every hand", () => {
  expect(rulesOf(CASH).blinds(1)).toEqual({ small: 10, big: 20 });
  expect(rulesOf(CASH).blinds(500)).toEqual({ small: 10, big: 20 });
});
it("tops up only the seat below the buy-in", () => {
  const r = rulesOf(CASH).beforeHand(seatsOf(3400, 600));
  expect(r.seats.map((s) => s.stack)).toEqual([3400, 2000]);
  expect(r.events).toEqual([{ type: "toppedUp", seat: 1, amount: 1400 }]);
});
it("tops up a busted seat from zero", () => {
  expect(rulesOf(CASH).beforeHand(seatsOf(0, 4000)).events).toEqual([
    { type: "toppedUp", seat: 0, amount: 2000 },
  ]);
});
it("a cash table is never over", () => {
  expect(rulesOf(CASH).isOver(seatsOf(0, 4000))).toBeNull();
});
it("tournament rules keep blindsFor, no top-up, over at zero", () => {
  expect(rulesOf(TOURNAMENT).blinds(11)).toEqual(blindsFor(11));
  expect(rulesOf(TOURNAMENT).beforeHand(seatsOf(0, 4000)).events).toEqual([]);
  expect(rulesOf(TOURNAMENT).isOver(seatsOf(0, 4000))).toBe(1);
});
```

Add to `table.test.ts`: a cash table where seat 0 shoves and loses to 0 has `winner === null`, no `tournamentEnded` event, and the next `startHand` emits `toppedUp` for seat 0 before `handStarted` with `handStarted.stacks[0] === 2000`.

- [ ] **Step 2:** Run `npx vitest run src/engine`. Expected: FAIL, `rulesOf` not defined.
- [ ] **Step 3:** Implement `rules.ts`. `startHand` calls `rulesOf(t.mode).beforeHand(t.seats)` first, uses `blinds(handNumber)`, and puts the top-up events before the `startHandWith` events. `conclude` sets `winner` from `isOver` and pushes `tournamentEnded` only when it is not null. `handHistory` maps `toppedUp` to `{ key: 'toppedUp', params: { seat, amount } }`. Add `"toppedUp": "You top up {{amount}}"` under `log.you` and `"toppedUp": "{{name}} tops up {{amount}}"` under `log.other`.
- [ ] **Step 4:** Add a property test in `properties.test.ts`: for random seeds, play 30 cash hands with random legal actions; before each hand both `handStarted.stacks` are at least 2000 and their minimum equals 2000; chips are conserved once you subtract the sum of `toppedUp` amounts.
- [ ] **Step 5:** Run `npm test && npm run test:golden`. Expected: PASS. Commit `feat(engine): cash mode rules with top-up to the buy-in`.

### Task 3: Session results, `leave()` and `sessionOver`

**Files:**

- Modify: `web/src/game/session.ts`, `web/src/game/session.test.ts`, `web/src/game/viewModel.ts`, `web/src/game/viewModel.test.ts`, `web/src/game/index.ts`

**Interfaces:**

- Consumes: Task 2 `rulesOf`, `toppedUp`
- Produces:
  - `SessionOptions.mode: TableMode` (required)
  - `start(mode?: TableMode): Promise<void>`; a given mode replaces the session's mode for this and later starts
  - `SessionState.session: { hands: number; net: number }` for the human seat, `net = stack − buyIn − sum of human toppedUp`, where `buyIn` is 2000 in a tournament
  - `Phase` gains `'sessionOver'`; `leave(): void` is accepted only when `table.mode.kind === 'cash'` and `phase === 'handOver'`
  - `ViewModel` gains `mode: 'cash' | 'tournament'` and `session: { hands: number; netBb: number; bbPer100: number | null }`. `netBb` is rounded to one decimal; `bbPer100` is null when `hands` is 0. `blinds` comes from `rulesOf(mode).blinds(max(handNumber, 1))`

- [ ] **Step 1: Failing tests in `session.test.ts`** with `mode: CASH`:
  - after a hand the human wins by 1400 then a hand where the bot busts: `session` is `{ hands: 2, net: 1400 + won }` and the human got no top-up;
  - the human shoves and loses everything: phase is `handOver`, not `tournamentOver`; after `nextHand` the human stack is 2000 and `net` is −2000;
  - `leave()` in `humanToAct` leaves the phase unchanged; in `handOver` it gives `sessionOver`; `act()` and `nextHand()` in `sessionOver` change nothing;
  - `leave()` in a tournament session does nothing.
  - In `viewModel.test.ts`: 40 hands and `net` 250 at big blind 20 give `netBb` 12.5 and `bbPer100` 31.3 (one decimal); zero hands give `bbPer100` null.
- [ ] **Step 2:** Run `npx vitest run src/game`. Expected: FAIL.
- [ ] **Step 3:** Implement. Count `hands` when a hand completes. Sum the human `toppedUp` amounts from the recorded events.
- [ ] **Step 4:** Run `npm test`. Expected: PASS. Commit `feat(game): cash session result and leaving the table`.

### Task 4: Cash mode in the UI

**Files:**

- Create: `web/src/ui/SessionOver.tsx`, `web/src/ui/SessionOver.test.tsx`
- Modify: `web/src/ui/StartScreen.tsx`, `web/src/ui/App.tsx`, `web/src/main.tsx`, `web/src/ui/styles/layout.css`, `web/src/locales/en.json`, `web/e2e/game.spec.ts`, `web/e2e/layout.spec.ts`, `web/e2e/contact.ts`, the UI tests that click `start`

**Interfaces:**

- Consumes: Task 3 `start(mode)`, `leave()`, `ViewModel.mode`, `ViewModel.session`, phase `sessionOver`
- Produces: test ids `start-cash`, `start-tournament`, `session-result`, `leave`, `session-over`

- [ ] **Step 1:** Failing tests:
  - `StartScreen` renders two buttons, and clicking `start-cash` calls `onStart(CASH)`;
  - `App` in a cash session shows `session-result` with the text `+12.5 bb` and a `leave` button in `handOver`;
  - `SessionOver` shows the result, the bb/100 and the hand count.
- [ ] **Step 2:** Run `npx vitest run src/ui`. Expected: FAIL.
- [ ] **Step 3:** Implement. Copy in `en.json`:
  - `start.tagline` "Heads-up no-limit hold'em. A cash game with 100 big blinds, or a tournament with rising blinds."
  - `start.cash` "Cash game, 100 big blinds"
  - `start.tournament` "Tournament"
  - `status.result` "{{sign}}{{bb}} bb"
  - `status.leave` "Leave table"
  - `status.newTournament` becomes `status.newGame` "New game"
  - `session.title` "Session over"
  - `session.summary` "{{sign}}{{bb}} bb over {{hands}} hands"
  - `session.rate` "{{rate}} bb/100"
  - `session.again` "Play again"

  In a cash game the status bar shows the blinds and `session-result` in place of the blind level. `main.tsx` passes `mode: TOURNAMENT` at mount; the start screen picks the mode through `start(mode)`.

- [ ] **Step 4:** e2e in `game.spec.ts`: start a cash game with `?seed=3&botDelay=0`, play a hand with check or call, leave the table, and expect `session-over` to show `hands` 1. Run it on the phone and desktop projects. Update the existing tests to `start-tournament`.
- [ ] **Step 5:** Run `npm test && npm run build && npm run test:e2e`. Expected: PASS. Run `npm run contact` and look at the two contact sheets. Commit `feat(ui): cash game start, running result and session over`.

### Task 5: Hand record

**Files:**

- Create: `web/src/game/records/record.ts`, `web/src/game/records/record.test.ts`

**Interfaces:**

- Consumes: `EngineEvent`, `TableMode`, `SeatIndex` from `engine`; `Decision` from `brains`
- Produces:
  - `interface RecordedDecision { eventIndex: number; solverChoice: string; evByAction: Record<string, number | null> }`
  - `interface RecordMeta { sessionId: string; mode: TableMode; names: [string, string]; humanSeat: SeatIndex | null; bot: { id: string; version: string }; playedAt: string }`
  - `HandRecord` exactly as in the spec, with `decisions: RecordedDecision[]` and `id = \`${sessionId}-${handNumber}\``
  - `handRecord(events: EngineEvent[], decisions: RecordedDecision[], meta: RecordMeta): HandRecord`. `events` are the events of one hand, from `toppedUp`/`handStarted` to `handEnded`; `handNumber` comes from `handStarted`
  - `recordDecision(decision: Decision, eventIndex: number): RecordedDecision` maps non-finite EVs to null
  - `MATH_BOT = { id: 'math', version: '1' }`

- [ ] **Step 1:** Failing tests that build hands with `engine/testDeck.ts`:
  - a fold preflop gives the hole cards as strings (`['Ah', 'Kd']`), an empty board and every event;
  - a showdown gives a board of 5 strings;
  - an all-in preflop run-out gives a board of 5;
  - a hand preceded by `toppedUp` keeps that event first;
  - `recordDecision` with `aggressive: Number.NEGATIVE_INFINITY` gives `null`, and `JSON.parse(JSON.stringify(record))` deep-equals the record.
- [ ] **Step 2:** Run `npx vitest run src/game/records`. Expected: FAIL.
- [ ] **Step 3:** Implement with `formatCard` for cards.
- [ ] **Step 4:** Run the tests. Expected: PASS. Commit `feat(game): hand record builder`.

### Task 6: PokerStars text export

**Files:**

- Create: `web/src/game/records/pokerstars.ts`, `web/src/game/records/pokerstars.test.ts`

**Interfaces:**

- Consumes: Task 5 `HandRecord`
- Produces: `toPokerStars(records: HandRecord[]): string`, where hands are separated by two blank lines

**Format rules:**

- Hand number: `Date.parse(playedAt) * 1000 + handNumber % 1000`.
- Seat `n + 1` is seat index `n`. The hero is `humanSeat`, or seat 0 when `humanSeat` is null.
- A raise when there is a bet on the street, including the preflop blinds, is `raises X to Y`, where X is the increment over the largest bet. Otherwise it is `bets Y`.
- A call is `calls <acted.amount>`. All-ins append ` and is all-in`.
- Top-ups are not printed; the seat line shows the stack after the top-up.
- The bot's cards appear only under `*** SHOW DOWN ***`.
- The category text is the `hands` name in lower case, `pair` → `a pair`.

Reference for a flop fold:

```
PokerStars Hand #1792778400000001: Hold'em No Limit (10/20) - 2026/10/23 18:00:00 UTC
Table 'Mallorca' 2-max Seat #1 is the button
Seat 1: You (2000 in chips)
Seat 2: Mallorca (2000 in chips)
You: posts small blind 10
Mallorca: posts big blind 20
*** HOLE CARDS ***
Dealt to You [Ah Kd]
You: raises 40 to 60
Mallorca: calls 40
*** FLOP *** [2c 7d 9h]
Mallorca: checks
You: bets 60
Mallorca: folds
Uncalled bet (60) returned to You
You collected 120 from pot
*** SUMMARY ***
Total pot 120 | Rake 0
Board [2c 7d 9h]
Seat 1: You (button) (small blind) collected (120)
Seat 2: Mallorca (big blind) folded on the Flop
```

Showdown additions:

- `*** TURN *** [2c 7d 9h] [Ts]` and `*** RIVER *** [2c 7d 9h Ts] [3s]`.
- `*** SHOW DOWN ***`, then `Mallorca: shows [Qs Qh] (a pair)` per seat and `Mallorca collected 400 from pot`.
- Summary lines `showed [Qs Qh] and won (400) with a pair` and `showed [Ah Kd] and lost with high card`.
- A split pot prints `collected` for each seat with its share.

- [ ] **Step 1:** Failing tests:
  - the flop fold above, matched exactly against the reference text;
  - a showdown on the river;
  - a split pot;
  - a preflop all-in with a run-out;
  - two records joined by two blank lines.
- [ ] **Step 2:** Run. Expected: FAIL.
- [ ] **Step 3:** Implement.
- [ ] **Step 4:** Run. Expected: PASS. Commit `feat(game): export hand records as PokerStars text`.

### Task 7: Hand store and session recording

**Files:**

- Create: `web/src/game/records/store.ts`, `web/src/game/records/store.test.ts`, `web/src/game/records/index.ts`
- Modify: `web/src/game/session.ts`, `web/src/game/session.test.ts`, `web/src/game/index.ts`

**Interfaces:**

- Consumes: Task 5 `handRecord`, `recordDecision`, `MATH_BOT`
- Produces:
  - `interface HandStore { add(record: HandRecord): Promise<void>; list(sessionId?: string): Promise<HandRecord[]>; clear(): Promise<void> }`
  - `memoryStore(): HandStore`
  - `indexedDbStore(): Promise<HandStore>`: database `mallorca`, version 1, object store `hands` keyed by `id`, index `sessionId`
  - `openHandStore(): Promise<HandStore>` returns `memoryStore()` when IndexedDB is missing or fails to open
  - `SessionOptions.records?: { store: HandStore; sessionId: string; now: () => string }`; `SessionState.sessionId: string | null`
  - `game/index.ts` re-exports `HandStore`, `HandRecord`, `openHandStore`, `memoryStore`, `toPokerStars`

- [ ] **Step 1:** Failing tests:
  - a contract suite `describeStore(name, make)` run on `memoryStore`: add two records of two sessions, `list()` returns both in `handNumber` order, `list('s1')` returns one, `clear()` empties the store;
  - a session with a memory store and `now: () => '2026-10-23T18:00:00.000Z'` that plays two hands stores two records with `id`s `s1-1` and `s1-2` and the bot's decisions whose `eventIndex` points at an `acted` event of the bot seat;
  - a store whose `add` rejects leaves the session dealing hand 2 normally.
- [ ] **Step 2:** Run. Expected: FAIL.
- [ ] **Step 3:** Implement. The session collects the events of the current hand from `beginHand` on and records the bot's `Decision` with the index its `acted` event gets. When the hand completes it calls `store.add(handRecord(...))` without awaiting in the game flow and swallows a rejection.
- [ ] **Step 4:** Run `npm test`. Expected: PASS. Commit `feat(game): store hand records of every session`.

### Task 8: Export and clear in the UI

**Files:**

- Create: `web/src/ui/download.ts`
- Modify: `web/src/main.tsx`, `web/src/ui/App.tsx`, `web/src/ui/SessionOver.tsx`, `web/src/locales/en.json`, `web/e2e/game.spec.ts`

**Interfaces:**

- Consumes: Task 7 `openHandStore`, `toPokerStars`, `SessionState.sessionId`
- Produces:
  - `download(filename: string, text: string, type: string): void`, built on Blob and a temporary anchor
  - test ids `export-json`, `export-text` in the session-over dialog (current session) and `export-all-json`, `export-all-text`, `clear-history` in the menu
  - file names `mallorca-<sessionId>.json` or `.txt` and `mallorca-history.json` or `.txt`

- [ ] **Step 1:** `main.tsx` awaits `openHandStore()` once and passes `records: { store, sessionId: crypto.randomUUID(), now: () => new Date().toISOString() }` to every new session, and `store` to `App`. Copy:
  - `history.exportJson` "Export history as JSON"
  - `history.exportText` "Export history as text"
  - `history.exportAllJson` "Export all hands as JSON"
  - `history.exportAllText` "Export all hands as text"
  - `history.clear` "Clear history"
- [ ] **Step 2:** e2e test: play two cash hands, leave, click `export-json`, read the download through Playwright `page.waitForEvent('download')`, and check that it parses to two records. Reload, start a new game, open the menu, and check that `export-all-json` gives at least two records, which proves IndexedDB persisted them. Click `clear-history`; `export-all-json` then gives `[]`.
- [ ] **Step 3:** Run `npm run test:e2e`. Expected: FAIL.
- [ ] **Step 4:** Implement.
- [ ] **Step 5:** Run `npm test && npm run build && npm run test:e2e`. Expected: PASS. Commit `feat(ui): export and clear the hand history`.

### Task 9: Arena match and bots

**Files:**

- Create: `web/tools/arena/bots.ts`, `web/tools/arena/match.ts`, `web/tools/arena/match.test.ts`
- Modify: `web/tsconfig.json` (`include` gains `tools/arena`)

**Interfaces:**

- Consumes: `startHandWith`, `applyAction`, `legalActions` from `engine`; `createMathBrain`, `randomVillainModel`, `toObservations` from `brains`; `mulberry32`, `shuffle`, `fullDeck` from `cards`
- Produces:
  - `interface ArenaBot { observe(o: Observation): void; decide(legal: LegalActions): Action }`
  - `type BotFactory = (rng: Rng) => ArenaBot`
  - `BOTS: Record<'math' | 'always-call' | 'always-raise' | 'random', BotFactory>`
  - `playPair(a: BotFactory, b: BotFactory, seed: number, index: number): { net: number; hands: EngineEvent[][] }`. `net` is `a`'s chips over both hands.

Rules:

- The deck rng is `mulberry32((Math.imul(seed, 0x9e3779b1) + index) >>> 0)`.
- Hand `k` of 0 and 1: seats 2000/2000, button seat 0, blinds 10/20, `handNumber = index * 2 + k + 1`. In hand 0 `a` sits on seat 0; in hand 1 `b` does.
- Each hand gets fresh bots. A bot's rng is `mulberry32((seed ^ (index * 4 + k * 2 + seat)) >>> 0)`.
- Each bot receives `toObservations` of the new events for its seat after every step.
- `always-call` checks or calls.
- `always-raise` raises to `legal.raise.min` when it can, and otherwise calls or checks.
- `random` picks uniformly among the legal kinds; a raise goes to `legal.raise.min`.

- [ ] **Step 1: Failing tests**

```ts
it("the math bot against itself nets exactly zero per pair", () => {
  for (let i = 0; i < 10; i++)
    expect(playPair(BOTS.math, BOTS.math, 1, i).net).toBe(0);
});
it("a pair is the same deck with the seats swapped", () => {
  const { hands } = playPair(BOTS["always-call"], BOTS["always-raise"], 5, 3);
  const hole = (h: EngineEvent[], seat: SeatIndex) =>
    h.find((e) => e.type === "holeCards" && e.seat === seat);
  expect(hole(hands[0], 0)?.cards).toEqual(hole(hands[1], 0)?.cards);
});
it("is deterministic for a seed and index", () => {
  expect(playPair(BOTS.random, BOTS.math, 9, 2)).toEqual(
    playPair(BOTS.random, BOTS.math, 9, 2),
  );
});
it("chips are conserved in every hand", () => {
  /* the sum of handEnded stacks is 4000 in both hands */
});
```

- [ ] **Step 2:** Run `npx vitest run tools/arena`. Expected: FAIL.
- [ ] **Step 3:** Implement.
- [ ] **Step 4:** Run. Expected: PASS within 30 s. Commit `feat(arena): duplicate pair of hands between two bots`.

### Task 10: Arena statistics, CLI and workers

**Files:**

- Create: `web/tools/arena/stats.ts`, `web/tools/arena/stats.test.ts`, `web/tools/arena/run.ts`, `web/tools/arena/worker.ts`, `web/tools/arena/cli.ts`, `web/tools/arena/cli.test.ts`, `web/tools/arena/README.md`
- Modify: `web/package.json` (script `"arena": "node tools/arena/cli.ts"`), `web/CLAUDE.md` (one line on `npm run arena`), `web/ARCHITECTURE.md` (arena section), `.gitignore` (`web/arena-out/`)

**Interfaces:**

- Consumes: Task 9 `playPair`, `BOTS`; Task 5 `handRecord`
- Produces:
  - `summarize(nets: number[], bigBlind: number): { pairs: number; hands: number; bbPer100: number; ci95: number }`
  - `runArena(options: { a: string; b: string; pairs: number; seed: number; workers: number; saveHands?: string }): Promise<Report>`
  - `interface Report { a: string; b: string; pairs: number; hands: number; seed: number; bbPer100: number; ci95: number | null; durationMs: number; commit: string }`

Statistics:

- The per-pair value is `v = net / 2 / bigBlind * 100`. `bbPer100` is the mean of `v`.
- `ci95 = 1.96 * sd(v) / sqrt(n)`, with `sd` the sample standard deviation (n − 1).
- With fewer than 2 pairs, `ci95` is `Infinity` in `summarize` and null in the report.
- `hands = 2 * pairs`.

CLI and workers:

- Flags `--a --b --pairs --seed --workers --out --save-hands`. Defaults: 1000 pairs, seed 1, `os.availableParallelism()` workers.
- Unknown bot names exit with code 1 and list the bot names.
- Workers are started with `new Worker(new URL('./worker.ts', import.meta.url), { workerData })`.
- Pair indices are split into contiguous chunks. Results are gathered in index order.
- `--save-hands` writes JSONL of `handRecord` with `humanSeat: null`, `sessionId: arena-<seed>`, `bot: { id: '<a>-vs-<b>', version: '1' }` and `playedAt` from the start of the run.
- `commit` comes from `git rev-parse --short HEAD`, or `unknown`.
- The console output is one line: `a vs b: +12.3 ± 4.5 bb/100 over 10000 hands in 182 s`.

- [ ] **Step 1:** Failing tests in `stats.test.ts`:
  - nets `[40, -40, 40, -40]` at bb 20 give `bbPer100` 0, and `ci95` is `1.96 * 115.47 / 2` within 0.01;
  - `[100]` gives `ci95` `Infinity`.
- [ ] **Step 2:** Failing tests in `cli.test.ts`, which spawn `node tools/arena/cli.ts`:
  - `--a always-call --b always-raise --pairs 8 --seed 4 --workers 1 --out <tmp>/a.json` and the same with `--workers 4` give reports with equal `bbPer100` and `ci95`;
  - `--a nope` exits with 1;
  - `--save-hands` writes 16 lines, each a JSON record.
  - A smoke test runs `runArena` in process with `math` against `always-call`, 20 pairs and 1 worker, and expects `hands` 40 and a finite `bbPer100`.
- [ ] **Step 3:** Run `npx vitest run tools/arena`. Expected: FAIL.
- [ ] **Step 4:** Implement. `workers: 1` runs in process without a thread.
- [ ] **Step 5:** Run `npm test`. Expected: PASS. Then run `npm run arena -- --a math --b always-call --pairs 2000` and paste its line into `tools/arena/README.md` with the machine and the time.
- [ ] **Step 6:** Run `npm run lint && npm test && npm run test:golden && npm run build && npm run test:e2e`. Expected: all green. Commit `feat(arena): bb/100 report over worker threads`.
