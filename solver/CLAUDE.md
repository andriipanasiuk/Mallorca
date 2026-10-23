# Working in this package

- Rust toolchain is pinned in `rust-toolchain.toml`. Run every command from `solver/`.
- Before every commit: `cargo fmt --all`, `cargo clippy --all-targets -- -D warnings`, `cargo test`. Also `cargo build -p mallorca-core --target wasm32-unknown-unknown`.
- TDD: the failing test comes first. Fix the code, not the rule: never loosen a tolerance, add an `#[allow]` or delete an assertion.
- No comments in code or config, in any language.
- `mallorca-core` is pure: no `std::time`, no `std::fs`, no `std::env`, no thread-local or global RNG, and it builds for `wasm32-unknown-unknown`. Randomness enters as a `&mut Rng` argument. Threads, files and clocks live in `mallorca-trainer`.
- Slow tests are `#[ignore]` and run with `cargo test --release -- --include-ignored`.
- Artefacts (checkpoints, strategy files) go to `solver/data/`, which is gitignored.
- Cards are `u8` 0..51, `rank * 4 + suit`; text form is two characters, rank `23456789TJQKA`, suit `cdhs`.
- Git: commit scope `solver` (`feat(solver): ...`). Run `git branch --show-current` before committing.
