# Mallorca solver

A Rust workspace for a CFR poker trainer.

- `crates/core`: pure game logic, cards, RNG and (later) the CFR core. Builds for `wasm32-unknown-unknown`.
- `crates/trainer`: the command line trainer that owns threads, files and clocks.

Start with `CLAUDE.md` for the working rules.
