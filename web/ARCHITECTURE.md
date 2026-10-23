# Architecture

## Package layout

```
web/
  src/cards/     Card, parse and format ("As Kd"), seeded RNG, shuffle
  src/equity/    hand evaluation and equity
  src/engine/    heads-up NLHE rules as a pure reducer, tournament and blind schedule
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
| `engine` | `cards`                               |
| `brains` | `cards`, `equity`, `engine`           |
| `game`   | `cards`, `equity`, `engine`, `brains` |
| `ui`     | `game`                                |

`brains` takes only types from `engine`: `Action` and observations.

`ui` imports only from `game`. For the table, `game` re-exports `Card`, `formatCard`, `rankOf` and `suitOf`
from `cards`.

ESLint enforces the table with a `no-restricted-imports` rule per directory.

## Purity

`cards`, `equity`, `engine` and `brains` use no DOM, no `Math.random`, no `Date` and no `performance`.
ESLint enforces it with `no-restricted-globals` and `no-restricted-properties`. Randomness enters as an `Rng`
argument.

## cards

- `Card` is a number 0..51, `rank * 4 + suit`. `rankOf` returns 2..14, `suitOf` returns 0..3 for `c d h s`.
- `parseCard("As")`, `parseCards("As Kd")`, `formatCard`, `fullDeck()`.
- `Rng` has `next(): number` in [0, 1). `mulberry32(seed)` implements it. `shuffle(items, rng)` is Fisher-Yates
  and returns a copy.

## Conventions

- Chips are integers.
- Cards in fixtures and text are two-character strings.
- Tests sit next to the code as `*.test.ts`.
