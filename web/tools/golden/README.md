# Golden harness

Runs the original Java bot and equilator and writes the fixtures the TypeScript port must match.

```bash
bash web/tools/golden/run.sh
```

The script works from any directory. It compiles `src/golden/*.java` with `javac --release 8` from `JAVA_HOME`
(default: the Android Studio JBR) into `build/`, pulling the Java modules `Tools`, `MallorcaCore`,
`MallorcaEquilator` and `MallorcaBrains` from the source path, runs `golden.GoldenMain`, then formats the output
with Prettier. Two runs produce byte-identical files.

## Outputs

| File                                             | Content                                                 |
| ------------------------------------------------ | ------------------------------------------------------- |
| `src/equity/fixtures/equity.json`                | 100 street equities: 34 flops, 33 turns, 33 rivers      |
| `src/brains/fixtures/decisions.json`             | Decisions of the Java math bot, at least 40 per street  |
| `src/brains/fixtures/preflop-strength-java.json` | The 169 by 169 preflop strength table the Java bot uses |

### `equity.json`

`{ hero, board, range: [{ cards, weight }], strength, positivePotential, negativePotential }`. Ranges rotate through
three kinds: every combination without dead cards at weight 1, a random subset with weights 0.25, 0.5 and 1, and
pairs at 1, suited broadways at 1, offsuit broadways at 0.5, suited aces at 0.25. Flop and turn values come from
`PokerEquilatorBrecher.equityVsSpectrum`, river strength from `PokerEquilatorBrecher.strengthVsSpectrum` with
both potentials 0.

### `decisions.json`

`{ id, observations, legal, evByAction: { fold, passive, aggressive }, chosen, java }`.

- The bot is built by `NLMathBotFactory(new UniformRandomAdvisor()).createBot(...)`, so the same villain model
  feeds the profit calculator and the villain spectrum observer. `UniformRandomAdvisor` answers fold, passive,
  aggressive with 1/3 each when the player faces a bet, otherwise 0, 1/2, 1/2.
- Games run through `GameEngine.playGame()` against `ScriptedOpponent`, which picks among the actions legal in the
  TypeScript engine with a seeded `Random`: facing a bet it folds, calls or raises with 1/3 each, otherwise it
  checks or bets with 1/2 each; sizes are a min raise, half pot, three quarters pot, pot or all in.
- `observations` are the bot's view of the current hand up to the decision, in the `Observation` shape of the
  brains layer. The Java bot keeps no state across hands.
- `legal` is computed from the Java game state with the TypeScript engine rules.
- `evByAction` holds the mean of each `RandomVariable` in the `ActionDistribution` the `EVAdvisor` computed;
  a class the solver did not consider is `null`.
- `chosen` is the action after `NLActionPreprocessor`; a raise is `{ kind: 'raise', to }`, the bot's street total.
- `java` carries the Java engine's street, pot, amount to call, bankroll at risk, big blind, the bet size the solver
  evaluated for the aggressive class and the raw Java action, for debugging only.
- A hand is left out when the Java engine and the TypeScript rules disagree on stacks, bets or pot, or when the
  bot's action is not legal under the TypeScript rules. `GoldenMain` prints the counts and the reasons.

### `preflop-strength-java.json`

`PreflopEquilatorImpl` reads `PreflopStrengthTable.array` (`PREFLOP_STRENGTH_TABLE_PATH`, `preflop.eql`, is only read
by `createFromFile()`, which the bot never calls). The array is 170 by 170; row and column 169 hold only `-1`, so
the fixture keeps indices 0 to 168.

- Ranks map to `2 → 0, 3 → 1, … , A → 12`. A hand of ranks `hi ≥ lo` has index `hi * 13 + lo`. Suits are ignored:
  `AKs` and `AKo` share index 167, `22` is 0, `32` is 13, `AA` is 168.
- 91 indices are used. Cells where the row or the column is not of the form `hi * 13 + lo` are `-1`.
- `table[hero][villain]` is the share of showdowns hero wins, ties counting half, over all 5-card boards, with hero
  holding clubs and diamonds and villain hearts and spades (`PreflopTableGenerator`). The lower triangle is
  `1 - strength` of the mirrored cell.
- The values in the code are the values of `preflop.eql` truncated to two decimals (`Math.floor(x * 100) / 100`),
  so `table[i][j] + table[j][i]` can be 0.99.

## Changes to the Java path

- `src/mallorcatour/equilator/PokerEquilatorBrecher.java` is a copy of the original that comes first on the source
  path and shadows it. The one change: in `equityVsSpectrum`, the villain hole cards of the potential computation
  use `intValueForBrecher()` instead of `intValue()` (lines 579 and 580). The bot's decision path does not call this
  method; it only affects `equity.json`.
- Randomness: `GameEngine` and `Advice` draw from the static `Random` in `UniformRandomizer`, seeded with
  `System.nanoTime()`. `DecisionHarness` replaces that field by reflection with `new Random(seed)` before each game.
  The villain model is never sampled: `UniformRandomAdvisor`'s advice throws if `getAction()` is called.
- `DecisionHarness` wraps the `EVAdvisor`'s `IProfitCalculator` by reflection to read each `ActionDistribution`
  without changing it.
