# Mallorca solver

A Rust workspace for a CFR poker trainer.

- `crates/core`: pure game logic, cards, RNG and (later) the CFR core. Builds for `wasm32-unknown-unknown`.
- `crates/trainer`: the command line trainer that owns threads, files and clocks.

Start with `CLAUDE.md` for the working rules.

Checkpoints are keyed by a fingerprint of blinds, stack, buckets, bet sizes, the tree and the bucket tables, so `name`, `threads`, `checkpoint_minutes`, `memory_limit_gb` and the `[cfr]` parameters may change between resumes.

## Overnight runbook for `v1`

Run everything from `solver/` in release mode. `D=data/v1` below; the preset is `presets/v1.toml` (stack 2000,
128 buckets per street, 12 threads, a checkpoint every 60 minutes). Bucket counts are capped at 256 by the `u8`
bucket tables; 512 buckets need a new table format.

```
cargo build --release -p mallorca-trainer
T=target/release/mallorca-trainer
P=presets/v1.toml
D=data/v1
mkdir -p $D
```

1. Bucket tables, once per bucket configuration (the `tiny` build takes about 25 s in release; `v1` has more
   buckets but the same table sizes):
   `$T buckets --preset $P --out $D`
2. Check the tree and the memory estimate: `$T tree --preset $P`
3. Measure the speed and get the iteration count for each night length:
   `$T calibrate --preset $P --data $D --minutes 30`
   It trains in a scratch directory and deletes it, so it never touches real checkpoints.
4. Train overnight: `$T train --preset $P --data $D --minutes 540`
   Checkpoints are written every `checkpoint_minutes` and on exit. Ctrl-C stops after the current iterations and
   writes a final checkpoint; a second Ctrl-C aborts without one.
5. For each checkpoint `N` (files `$D/checkpoints/N.ckpt`), export both variants and measure:
   - browser file: `$T export --preset $P --data $D --checkpoint N --streets browser --out $D/v1-N-browser.bin`
     (preflop and flop only; its gzip size is printed and goes into `RESULTS.md`)
   - full file: `$T export --preset $P --data $D --checkpoint N --streets all --out $D/v1-N-all.bin`
     (all streets, needed by `lbr` and `match`)
   - exploitability: `$T lbr --strategy $D/v1-N-all.bin --hands 1000000`
6. Strength against fixed opponents, on the last checkpoint:
   `$T match --strategy $D/v1-last-all.bin --opponent all --deals 200000`
7. Deeper stacks, on the last checkpoint only, for both `lbr` and `match`:
   `$T lbr --strategy $D/v1-last-all.bin --hands 1000000 --stack 2500` and `--stack 3000`
   `$T match --strategy $D/v1-last-all.bin --opponent all --deals 200000 --stack 2500` and `--stack 3000`

`lbr` prints mbb/hand with a 95% interval; `match` prints bb/100 with a 95% interval per opponent. `train` prints
the average positive regret at every checkpoint. Copy the figures into `RESULTS.md`.

### Expected artefact sizes

Bucket tables, identical for every preset because they are indexed by canonical hand class:

| File | Size |
| --- | --- |
| `flop.bkt` | 1.3 MB (1,286,792 bytes) |
| `turn.bkt` | 14 MB (13,960,050 bytes) |
| `river.bkt` | 123 MB (123,156,254 bytes) |
| `river_hs.f32` | 493 MB (492,625,016 bytes) |

The `v1` tree (`tree --preset presets/v1.toml`): 41,410 nodes, 1,950,352 infosets, 5.3M action slots at 128
buckets (5,302,197). Each action slot holds an `f32` regret and an `f64` strategy sum, 12 bytes, so a `v1`
checkpoint is 63,626,430 bytes (a 66 byte header, then all regrets, then all strategy sums). The `memory estimate`
is action slots * 12 bytes (`tree` prints that) plus the three bucket tables (`train` adds them); `train` checks it
against `memory_limit_gb` before training starts.

### Resuming

Run the same `train` command again. It loads the latest checkpoint in `$D/checkpoints` and continues counting
iterations from it; `--minutes` or `--iterations` apply to the new session. A checkpoint is accepted only if its
fingerprint matches (blinds, stack, buckets, bet sizes, tree and bucket tables); a mismatch is an error, never a
silent restart. `name`, `threads`, `checkpoint_minutes`, `memory_limit_gb` and the `[cfr]` parameters may change
between resumes. To start over, use a new `--data` directory or remove `$D/checkpoints`.
