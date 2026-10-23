# CFR core and trainer: design

Sub-project 1 of issue #4. It produces a trained heads-up strategy file and the numbers that say how good it is.
The bot does not play in the browser yet; that is sub-project 2.

## Context

The current brain in `web/src/brains` looks one action ahead with one bet size, models the opponent as
`randomVillainModel` (a range that never narrows), values a call as an immediate showdown and acts
deterministically. Issue #4 replaces it with a strategy trained by self-play with CFR.

Issue #4 is split into three sub-projects, each with its own spec, plan and PR:

1. **Core and trainer** (this spec): Rust hand evaluator, card and action abstraction, game tree, MCCFR trainer
   with checkpoints, strategy file format, local best response and a baseline match. Independent of #3.
2. **Brain in the browser**: WASM build of the core, strategy lookup, turn and river subgame solver, a new `Brain`
   in `web/src/brains` with off-tree bet translation and sampling through `Rng`. Integration into the session and
   the arena depends on #3.
3. **Difficulty levels**: style profiles, early checkpoints, an arena result per level.

This spec fixes the contracts later sub-projects depend on: the strategy file format and the core's purity rules.

## Goals

- Game: heads-up NLHE cash, blinds 10/20. Before every hand a seat with less than 2000 chips is topped up to
  2000 and the other keeps its chips, so the effective stack is at least 100 bb and can be deeper (3500 against
  2500 is 125 bb). The blueprint is trained at exactly 100 bb.
- Deeper stacks in v1: preflop and flop play the 100 bb blueprint as if the chips beyond 100 bb were absent;
  an all-in in the tree means the real remaining stack, and an opponent bet above 100 bb translates to the
  tree's all-in. Turn and river are solved at the real depth by the subgame solver of sub-project 2.
  Measurement reports how much this loses at 125 and 150 bb.
- One full training run fits in one night (8 to 10 hours) on an M3 Pro with 12 cores and 18 GB of memory.
- Every checkpoint gets a local best response figure in mbb/hand and a baseline match result in bb/100.
- The spec documents how to improve the strategy and which number each change should move.

## Non-goals

- WASM bindings, a server, the subgame solver, the TS `Brain`: sub-project 2.
- Off-tree bet translation and the play-time mapping of deeper stacks: sub-project 2. Evaluation opponents here
  use on-tree sizes only.
- Blueprints for depths other than 100 bb: a lever in section 8, not v1.
- Difficulty levels: sub-project 3.
- Cloud training: only if the measurements show the abstraction is the limit.
- Code from b-inary/postflop-solver: reference reading only, its licence is likely AGPL.

## 1. Layout

```
solver/
  Cargo.toml          workspace
  CLAUDE.md
  crates/core/        mallorca-core, library
  crates/trainer/     mallorca-trainer, CLI binary
  presets/            tiny.toml, v1.toml
  data/               generated artefacts, gitignored
```

- `mallorca-core` modules: `cards`, `eval` (hand evaluator), `iso` (hand isomorphism), `abstraction` (buckets and
  action sizes), `tree` (abstract betting tree), `cfr`, `strategy` (file format), `measure` (LBR and match),
  `games` (Kuhn and Leduc for tests).
- `mallorca-trainer` subcommands: `buckets`, `tree`, `calibrate`, `train`, `export`, `lbr`, `match`.
- The core uses no system time, no thread-local or global random source and no file system. Randomness comes in
  as a seeded RNG argument, I/O happens in the trainer. This keeps the core buildable for
  `wasm32-unknown-unknown`, which CI checks.
- Root `CLAUDE.md` gains a line that development also happens in `solver/`, with its own `solver/CLAUDE.md`:
  `cargo fmt`, `cargo clippy --all-targets -- -D warnings`, `cargo test` before every commit, TDD, no comments in
  code or config, conventional commits with scope `solver` (`feat(solver): ...`).
- `.github/workflows/solver-ci.yml` runs fmt, clippy, tests and the wasm32 build of the core on changes under
  `solver/`.
- `solver/data/` holds bucket tables, trees, checkpoints and exported strategies. It is gitignored; artefacts are
  rebuilt from a preset and a seed.

## 2. Game and abstraction

### Rules

- Two seats, the button posts the small blind and acts first preflop, last postflop. Bets are "raise to" the
  street total, as in `web/src/engine`. The minimum raise is the previous raise increment; a short all-in does not
  reopen the betting.
- Cards are `u8` 0..51, `rank * 4 + suit`, rank 0..12 for `2..A`, suit 0..3 for `c d h s`. This equals the `Card`
  numbering in `web/src/cards`.

### Hand evaluator

- A port of the HenryRLee PokerHandEvaluator algorithm (perfect hash over rank multisets plus a flush table), the
  algorithm the `phe` package implements. The plan checks the licence of both before porting.
- Output matches `pheEvaluator.rank` in `web/src/equity/evaluator.ts`: `7462 - phe`, so 0 is the worst high card
  and 7461 a royal flush; 5, 6 and 7 cards.
- Golden test: `web/tools/evaluator-golden/` holds a Node script that writes a fixture of random 5, 6 and 7 card
  hands with their TS ranks into `solver/crates/core/tests/fixtures/`. The Rust test checks every hand.
- Exhaustive test: all 2,598,960 five-card hands give exactly 7462 distinct ranks and the known count per
  category.

### Hand isomorphism

- Suit isomorphism per street after Waugh ("A Fast and Optimal Hand Isomorphism Algorithm"): an index for every
  canonical (hole, board) pair.
- Class counts are tested: 169 preflop, 1,286,792 flop, 13,960,050 turn, 123,156,254 river.

### Card abstraction (v1)

- Preflop: the 169 classes, lossless.
- River: hand strength against a uniform random opponent hand, ties counting half, computed per canonical board
  by ranking all live hands once and counting with card removal.
- Turn and flop: E[HS²] over the remaining cards, aggregated from the river table.
- Buckets are equal-population percentiles of that value per street. `v1` uses 128 buckets on each of flop, turn
  and river; `tiny` uses 8.
- Bucket tables are `u8` per canonical index, written by `buckets` to `solver/data/`. The river table is about
  123 MB.

### Action abstraction (v1)

Sizes come from the preset TOML. Values for `v1`:

- Preflop, in big blinds, raise to:
  - Button first action: fold, limp, open to 2.5 bb, all-in.
  - Big blind facing a limp: check, raise to 3.5 bb, all-in.
  - Facing an open: fold, call, 3-bet to 3 times the open, all-in.
  - Facing a 3-bet: fold, call, 4-bet to 2.2 times the 3-bet, all-in.
  - Facing a 4-bet or more: fold, call, all-in.
- Postflop, in fractions of the pot:
  - First bet of a street: 0.5, 0.75, 1, 1.5, 2 pot, all-in.
  - First raise: pot-sized raise, all-in.
  - Further raises: all-in only.
  - At most three aggressive actions per street, then only fold or call.
- Sizes are rounded to whole chips. A size above two thirds of the remaining stack becomes all-in. Sizes below the
  legal minimum become the minimum. Equal sizes after rounding collapse into one action.
- The preset is part of the abstraction fingerprint.

## 3. Trainer

### Tree

- `tree` builds the abstract betting tree once from the preset: decision nodes with the acting seat, street, pot,
  street bets and child actions; terminal nodes for folds and showdowns; chance transitions between streets.
- Every decision node gets a dense id and an offset into the flat arrays. An infoset is `(node, bucket)` and its
  storage starts at `offset[node] + bucket * actions[node]`.

### Storage

- Two flat `f32` arrays for all streets: cumulative regrets and cumulative strategy weights.
- Before training, the trainer prints the infoset count and the memory estimate and refuses to start when the
  estimate exceeds `memory_limit_gb` in the preset (12 by default).

### Algorithm

- External sampling MCCFR, alternating the traverser each iteration, with regret matching.
- Discounted CFR with α = 1.5, β = 0, γ = 2, applied every `discount_interval` iterations; the values sit in the
  preset so Linear CFR is a parameter change.
- Parallel: `threads` workers (12 for `v1`) run iterations against the shared arrays without locks, Hogwild
  style, writing through atomic `u32` cells that hold `f32` bits. A run with one thread and a fixed seed is
  bit-reproducible; a parallel run is not, and the docs say so.
- Chance sampling deals the full deck once per iteration from the iteration seed; buckets for both seats on every
  street are looked up through the isomorphism index and the tables.

### Checkpoints

- A checkpoint is written every `checkpoint_minutes` and on Ctrl-C: header (format version, fingerprint,
  iteration count, elapsed time), then both arrays.
- `train` resumes from the latest checkpoint whose fingerprint matches the preset and refuses one that does not.
- Average positive regret per iteration is logged at each checkpoint as the convergence signal.

### Calibration

- `calibrate` runs `train` for a fixed time (30 minutes by default), measures iterations per second and prints
  the iterations a night will reach. It is the first step before every new preset.

## 4. Strategy file format

The contract between the trainer and sub-project 2. Little-endian binary.

```
magic            4 bytes  "MLCS"
format_version   u16      1
fingerprint      32 bytes SHA-256 of preset TOML, serialized tree and bucket tables
small_blind      u32
big_blind        u32
stack            u32
streets          u8       bitmask, bit 0 preflop .. bit 3 river
iterations       u64
checkpoint       u32
tree             length-prefixed serialized tree
buckets          length-prefixed bucket tables for the included postflop streets
probabilities    u8 per action per infoset, in tree order
```

- Each infoset's probabilities sum to exactly 255, rounded from the average strategy by largest remainder. An
  infoset never visited gets the uniform distribution.
- The browser export includes preflop and flop only: the flop bucket table is about 1.3 MB, turn and river are
  solved at play time in sub-project 2. `export --streets all` writes a full file for tools.
- `export` prints the file size raw and gzipped. The target for the browser file is 20 MB gzipped; if `v1`
  exceeds it, sub-project 2 decides how to shrink it.
- A reader rejects an unknown `format_version`.

## 5. Measurement

- **CFR correctness:** the same `cfr` code solves Kuhn and Leduc poker in tests, and their exact exploitability
  must fall below a fixed bound (Kuhn 1 mbb/hand within 100k iterations, Leduc 10 mbb/hand within 1M). Exact best
  response in the bucketed full game is not computed: it needs the joint distribution of bucket sequences across
  streets and costs more than it tells.
- **Convergence on the real game:** average positive regret from the checkpoint log.
- **Local best response** after Lisý and Bowling (2017): the LBR seat sees its real cards, keeps a Bayesian range
  of the strategy's 1326 hands through its average strategy, calls preflop and on every postflop decision picks
  the action with the best one-step value among fold, call, 0.5 pot, 1 pot and all-in, assuming the strategy
  checks and calls to showdown afterwards. `lbr` plays N hands from both seats and prints mbb/hand with a 95%
  interval.
- **Baseline match:** `match` plays duplicate deals (every deck twice with seats swapped) between the strategy and
  always call, always raise (the largest on-tree raise) and random (uniform over legal on-tree actions), and prints
  bb/100 with a 95% interval for each.
- Every opponent here acts on the tree's sizes, so no bet translation is needed.
- **Depth:** `lbr` and `match` take `--stack` (2000 by default). At a deeper stack both seats still act on the
  100 bb tree, chip amounts of `RaiseTo` stay as in the tree, and an all-in commits the real remaining stack.
  The runbook measures at 2000, 2500 and 3000 chips to show what the 100 bb blueprint loses at depth.

## 6. Testing

- TDD for every module: the failing test first.
- Unit tests: evaluator golden and exhaustive checks, isomorphism counts and round trips, bucket table
  monotonicity (higher strength never gets a lower bucket), tree construction for the preset rules (raise cap,
  all-in conversion, collapsing sizes, minimum raise), DCFR discounting, regret matching, u8 quantization sums.
- Algorithm tests: Kuhn and Leduc exploitability bounds.
- End to end on `tiny`: `buckets`, `tree`, `train` for a few seconds, `export`, read the file back, check the
  fingerprint, the header and that every infoset sums to 255; `lbr` and `match` run on it and print a number.
- Slow tests (exhaustive evaluator, full isomorphism counts) are `#[ignore]` by default and run in CI with
  `--include-ignored`.

## 7. Done when

- CI is green: fmt, clippy, tests including the ignored ones, wasm32 build of the core.
- A `v1` overnight run finishes with checkpoints, an exported browser file and its size.
- LBR gives a mbb/hand figure for every checkpoint of the run, and it falls from checkpoint to checkpoint.
- The `v1` strategy beats always call, always raise and random with the 95% interval above zero.
- The figures of the first run are recorded in `solver/RESULTS.md` as the baseline for later versions.

## 8. How to improve

The first run sets the baseline; no threshold is fixed before it. Each lever below names the number it should
move. Change one lever at a time, record the preset, the figures and the run time in `solver/RESULTS.md`.

1. **More iterations.** Resume from the checkpoint for a second night. Moves LBR while average positive regret is
   still falling; when it flattens, the abstraction is the limit, not the training time.
2. **More buckets.** 128 to 256 or 512 per street, within the memory limit. Moves LBR; costs memory linearly and
   iterations to converge.
3. **Better buckets.** Potential-aware buckets on flop and turn (k-means over equity histograms with earth mover's
   distance) and opponent cluster hand strength (OCHS) on the river. Usually moves LBR more than bucket count at
   the same memory.
4. **Regret-based pruning.** Skip actions with very negative regret in most iterations. Moves iterations per
   second, so more training per night.
5. **Bet sizes.** Add the sizes LBR exploits most often, drop the ones the strategy never uses. Moves LBR; every
   size multiplies the tree.
6. **Vector CFR over public chance** (public chance sampling). Faster convergence per iteration; a larger rewrite
   of `cfr`.
7. **Deeper blueprints.** Train presets with `stack` 3000 and 4000 and pick the nearest blueprint by effective
   stack at the start of a hand. Worth it when the arena from #3 shows deep effective stacks are common and the
   `--stack 2500` and `--stack 3000` figures are clearly worse than at 2000. Costs one night and one browser file
   per depth.
8. **Cloud.** Only when the levers above are spent and the memory or time limit of the local machine is the
   blocker.
