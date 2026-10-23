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
| `engine` | `cards`, `equity`                     |
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

## engine

- `startHandWith(seats, button, blinds, deck, handNumber)` takes an already shuffled deck and deals from its front.
  Hole cards alternate starting with the seat after the button: `deck[0]` and `deck[2]` go to the big blind seat,
  `deck[1]` and `deck[3]` to the button. The board follows in order with no burn cards: flop `deck[4..6]`, turn
  `deck[7]`, river `deck[8]`. `Hand.deck` holds the undealt cards.
- `Hand.bets` is the street bet per seat, `committed` the total per seat for the hand, `pot` is the sum of
  `committed`. `canRaise` is false for a seat that faces a short all-in raise.
- `seats` stacks are the chips behind. `legalActions` raise amounts are "raise to" the street total and `max` is
  all-in; an uncalled excess returns to its owner in `potAwarded`.
- `acted.amount` is the chips added to the pot by that action. When a hand ends, `street` is `complete`, `toAct`
  is null, `pot` is 0 and the stacks include the awarded pot.
- `showdown.hands` is indexed by seat; `cards` are the hole cards.
- `legalActions` returns no action when nobody is to act.
