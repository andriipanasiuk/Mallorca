# CFR bot in the arena and the browser

Sub-project 2 of issue #4, first part: the `v1` blueprint plays in the TypeScript arena against the math bot and in the
browser game against a person. The subgame solver for the turn and river stays a later lever.

## Goal and success

- `npm run arena -- --a cfr --b math --pairs N` prints bb/100 with a 95% interval, recorded in `solver/RESULTS.md`
  and `web/tools/arena/README.md`.
- The start screen offers a choice of opponent: the math bot (default) or the CFR bot.
- One strategy file and one WASM module serve both the arena and the browser.

## Decisions

1. **Blueprint on every street.** The browser cannot load the turn and river bucket tables (14 MB and 123 MB). Both
   are percentile buckets over a scalar, so the bucket is computed at play time from the hand and the percentile
   thresholds:
   - river: hand strength against the 990 live opponent holes, the same integer counts and `f32` division as
     `river_strength`;
   - turn: mean of squared river strength over the 46 river cards, computed on the canonical representative of
     the turn class with the same card order and `f32` accumulation as `aggregate_hs2`.
   Both are bit-identical to the trainer's values, so the bucket equals the table's bucket. The flop keeps its
   table (1.3 MB); its value would need about 2M evaluations per hand.
2. **Strategy format version 2.** Same layout as version 1 except the bucket section: for each postflop street a
   kind byte (0 table, 1 thresholds) followed by either `u32 length + table` or `u16 count + count x f32`
   thresholds (`count = buckets - 1`). Readers accept versions 1 and 2. `export --streets play` writes version 2
   with all four streets, the flop table and turn and river thresholds; before writing it checks that the
   thresholds reproduce the turn and river tables on every class.
3. **Play-time translation in `mallorca-core`** (module `bot`), pure and tested on the tree:
   - real chips map to tree chips by `tree_bb / real_bb`; tree sizes that exceed the effective stack become all-in;
   - an opponent raise maps to one of the two neighbouring tree sizes (all-in counts as the largest) by the
     pseudo-harmonic mapping on pot fractions, sampled with a draw from the caller; outside the range it clamps;
   - the bot's own raise keeps the tree's fraction of the pot after the call and applies it to the real pot,
     clamped to the legal range; an illegal tree action degrades to the nearest legal one (never fold when a
     check is free);
   - when the cursor no longer matches the real hand (a tree terminal while the hand goes on, the wrong seat, or a
     different facing-a-bet state), it resynchronises to the decision node of the same street, seat and
     facing-a-bet state whose pot and price are nearest in log space.
4. **`mallorca-wasm`**: a `cdylib` with a plain `extern "C"` ABI (no wasm-bindgen, nothing to install): `alloc`,
   `load`, `bot_new`, `bot_free`, `hand_started`, `hole_cards`, `street`, `acted`, `decide`, `decided_to`.
   Randomness enters as `f64` draws from the TypeScript `Rng`, so arena runs stay reproducible by seed.
5. **Web**: a new layer `web/src/cfr/` (may import `cards`, `engine` types and `brains` types) with the WASM
   loader and `createCfrBrain`, which implements `Brain`. `evByAction` is non-finite, which hand records already
   store as `null`. The `.wasm` is built by `npm run wasm` (cargo from `solver/`, toolchain pinned there), run
   before `test`, `build` and `arena`, and never committed. The strategy is committed gzipped as
   `web/src/cfr/v1.mlcs.gz` and inflated with `DecompressionStream`.
6. **Arena**: bot `cfr` loads the same files from disk; each worker instantiates its own module.
7. **Game**: the start screen gets an opponent choice; the worker receives the bot kind in its first message.
   A CFR bot that fails to load or answer falls back to the math bot through the existing `withFallback`.
8. **CI**: `web-ci.yml` and `web-deploy.yml` install the pinned Rust toolchain with the wasm target and also run on
   `solver/**` changes; `solver-ci.yml` also builds `mallorca-wasm` for wasm32.

## Out of scope

Subgame solving, difficulty levels, blueprints for other depths, showing the bot's probabilities in the UI.
