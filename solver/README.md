# Mallorca solver

A Rust workspace for a CFR poker trainer.

- `crates/core`: pure game logic, cards, RNG and (later) the CFR core. Builds for `wasm32-unknown-unknown`.
- `crates/trainer`: the command line trainer that owns threads, files and clocks.

Start with `CLAUDE.md` for the working rules.

Checkpoints are keyed by a fingerprint of blinds, stack, buckets, bet sizes, the tree and the bucket tables, so `name`, `threads`, `checkpoint_minutes`, `memory_limit_gb` and the `[cfr]` parameters may change between resumes.
