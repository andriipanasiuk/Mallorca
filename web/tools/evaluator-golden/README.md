# Evaluator golden fixture

Writes 20,000 random hands each of 5, 6 and 7 cards with their `pheEvaluator.rank`, one per line as `<cards>,<rank>`. The Rust evaluator in `solver/crates/core` must reproduce every line.

Run from `web/` after `npm ci`:

```
node tools/evaluator-golden/write.ts ../solver/crates/core/tests/fixtures/evaluator.txt
```
