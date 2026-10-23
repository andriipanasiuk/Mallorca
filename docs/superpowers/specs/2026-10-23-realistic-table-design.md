# Realistic table: a cozy evening game in an attic

## Goal

Replace the flat table with a near-photographic first-person scene. The player sits at a card table in a
small attic room on a winter night and plays heads-up against Mallorca, who sits in the chair directly
opposite.

The mood is a quiet evening game between two close people. Warm low light, midnight teal felt, snow outside.
It is not a casino and not a bar. Nothing in the visuals, copy or flow pushes the player to keep playing, and
leaving is as easy as playing again.

Success for this stage:

- Every state from `docs/design-brief.md` renders on the new scene on a phone (390×844) and a desktop
  (1440×900), and the contact sheet reads well on both.
- The existing unit and e2e tests pass unchanged. `data-testid` values, keyboard shortcuts and the view-model
  stay as they are.
- No horizontal scroll, and every control is fully in the viewport on both devices (`e2e/layout.spec.ts`).
- The tournament-over dialog offers a way to stop as well as to play again.

## Decisions

- **Room:** the attic nook, `docs/design/table-concepts/plates/attic-nook-2.jpg`, with its portrait twin
  `attic-nook-portrait.jpg`. The other explored rooms stay in `docs/design/table-concepts/`.
- **Seat geometry:** the camera is the player, centred on the near side and looking straight across. The
  opponent's chair is centred on the far side. There are no corner views.
- **Technique, stage one:** a photo plate background with the game layer in CSS 3D (perspective and
  `rotateX`) on top. There are no new rendering dependencies. The table layer is one component, so a later
  WebGL layer (three.js: dealt cards with real light, the player's hands) can replace it without touching
  the rest of the UI.
- **Always night:** the scene ignores `prefers-color-scheme`. Both token blocks in `tokens.css` carry the same
  night palette, so `contrast.test.ts` keeps checking both blocks unchanged.
- **Copy** stays English-only through `en.json`.

## Scene

`web/public/scene/` holds:

| File                         | Use                                                   |
| ---------------------------- | ----------------------------------------------------- |
| `attic-landscape.webp`       | aspect ratio at least 1:1, about 2048 px wide, ≤ 400 KB |
| `attic-portrait.webp`        | aspect ratio below 1:1, about 1152×2048, ≤ 400 KB     |
| `attic-landscape-blur.webp`  | about 32 px wide placeholder, ≤ 3 KB                  |
| `attic-portrait-blur.webp`   | the same for portrait                                  |

The plates are AI-generated (ByteDance Seedream 5 Pro through ElevenLabs). Before a public release the owner
confirms that the ElevenLabs plan allows commercial use.

`Scene` renders the background with `<picture>` and a `(min-aspect-ratio: 1/1)` media source. The blurred
placeholder sits underneath until the full plate loads. A vignette sits on top. The scene covers the viewport
with `object-fit: cover`. The focal point is set per plate so the felt centre stays in frame on any aspect
ratio between 9:19.5 and 21:9.

Each plate has a geometry record, a plain object in `src/ui/scene.ts`, exposed as CSS custom properties on
the scene root:

| Property         | Meaning                                                             |
| ---------------- | ------------------------------------------------------------------- |
| `--focus-x/y`    | `object-position` of the plate                                      |
| `--felt-tilt`    | `rotateX` angle that lays the board flat on the felt                |
| `--opponent-y`   | vertical position of the opponent zone (plaque and face-down cards) |
| `--board-y`      | vertical position of the board and pot                              |
| `--hero-bottom`  | distance of the player's cards from the bottom edge                 |
| `--card-scale`   | board card size relative to the viewport                            |

Swapping the room means replacing the plate files and this record.

## Visual language

**Tokens** (`tokens.css`, identical in both blocks):

| Token                     | Value                    | Note                                     |
| ------------------------- | ------------------------ | ---------------------------------------- |
| `--color-text`            | `#f6ecda`                | cream                                    |
| `--color-muted`           | `#c9b89c`                |                                          |
| `--color-surface`         | `#2a1e14`                | solid fallback of the glass, for tests   |
| `--color-glass`           | `rgba(34, 24, 16, 0.5)`  | with `backdrop-filter: blur(14px)`       |
| `--color-glass-border`    | `rgba(255, 226, 180, 0.2)` |                                        |
| `--color-accent`          | `#e3b06e`                | amber, Raise and the player's turn       |
| `--color-accent-text`     | `#2a1a0c`                |                                          |
| `--color-danger`          | `#e9a48f`                | Fold text on glass                       |
| `--color-felt`            | `#1f4a52`                | fallback felt behind the plate           |
| `--color-card-face`       | `#fbf6ea`                | card paper                               |
| `--color-card-red`        | `#b3202a`                |                                          |
| `--color-card-black`      | `#1c1c1e`                |                                          |
| `--color-card-back`       | `#1f4a52`                | teal weave in tune with the felt         |
| chip colours              | terracotta `#b0583c`, teal `#2f4f57`, honey `#c99a4a`, ivory `#e9dcc4` |      |

Every text and background pair listed in `contrast.test.ts` keeps at least 4.5:1. Glass is checked against
its solid fallback.

**Type:** Cormorant Garamond (names, buttons, log, dialogs), Inter (small labels), IBM Plex Mono (numbers).
The fonts are self-hosted as woff2 through `@fontsource` packages, latin subset only, so the static build
makes no third-party requests.

**Cards:** a new SVG face with paper gradient, classic corner indices and a large centre pip. The back is a
teal weave. `Card` keeps its props, test ids and aria labels.

**Chips:** CSS radial and conic gradients in the four chip colours. They are not images.

**Controls:** pill-shaped glass buttons. Raise is filled amber with a soft glow. Fold uses `--color-danger`
text. Disabled controls drop to 45% opacity. Focus keeps the visible 3px outline in `--color-accent`. Tap
targets stay at least 44px.

## Layout

The whole viewport is the scene. There is no page background and no separate status bar strip: blinds, hand
number and the menu sit as small glass text at the top.

**Desktop** (aspect ratio at least 1:1):

- Opponent zone in front of the far chair: glass plaque (name, stack, dealer button, action tag or
  "Thinking…") with the two face-down cards on the felt below it.
- Board and pot centred on the felt, tilted by `--felt-tilt`. The bet tag shows next to each seat's plaque.
- The player's two cards lie by the near rail, centred and slightly fanned. The player's plaque sits beside
  them.
- Action panel at the bottom right: presets and slider, Raise full width, then Fold and Check or Call.
- Hand log and the coach panel at the top right, on glass. The coach panel stays hidden while empty.

**Phone** (aspect ratio below 1:1):

- The same zones stacked on the portrait plate: opponent, board and pot, the player's cards with the plaque
  under them, then the action panel across the bottom.
- The hand log moves into the menu.

## States

| State | Treatment |
| --- | --- |
| Player to act | A soft amber glow under the player's cards. Controls are enabled. There is no timer. |
| Bot thinking | The opponent plaque shows "Thinking…" with a slow pulse. Controls are disabled. |
| Action tag | The tag shows by the plaque of the seat that acted, as today. |
| Street dealt | New board cards slide in from the dealer side of the felt in about 250 ms. With `prefers-reduced-motion` they appear without movement. |
| All-in run-out | The same as street dealt, one street at a time. |
| Showdown | Both hands are face up. The winning cards get a warm halo, chips drift to the winner and the result line is calm, using the existing result copy. A split pot shows both halves. |
| Tournament over | A small glass note: "Good game", with "Play again" and "That's enough for tonight". |
| Start screen | The room, blurred and dimmed, with the title, the existing tagline and one button: "Take a seat". |

"Play again" keeps today's behaviour, which `e2e/game.spec.ts` checks: a new idle session and the start
screen. "That's enough for tonight" (`data-testid="leave"`) also opens a new idle session, but the start screen
shows a farewell line in place of the tagline: "Sleep well. The table will be here." The farewell flag lives in
`main.tsx` next to the seed and is cleared once the next tournament starts.

New strings in `en.json`: `start.cta` becomes "Take a seat". New keys are `start.farewell`, `over.title`
("Good game") and `over.leave` ("That's enough for tonight"). The existing `over.won` and `over.lost` stay as
the line under the title. All copy follows the project rules: no all-caps words and no dashes as punctuation.

## Components

All changes are in `src/ui`. `game`, `engine`, `brains`, `equity` and `cards` are untouched, so the layer rules
hold.

| Component | Change |
| --- | --- |
| `Scene` (new) | Plate `<picture>`, placeholder, vignette, geometry CSS properties |
| `scene.ts` (new) | Geometry records for the two plates |
| `App` | Wraps everything in `Scene`. Status moves into the scene top bar. Wires `onLeave`. |
| `Table` | Zone layout over the scene. The felt layer is one element (`table-felt`) that a WebGL layer can replace later. |
| `Seat` | Glass plaque. Cards render in the felt layer for the opponent and by the rail for the player. |
| `Card` | New SVG face and back |
| `Board`, `Pot` | Restyled. The pot gets CSS chips. |
| `ActionBar`, `BetSlider` | Restyled and placed. No logic change. |
| `HandLog` | Restyled. Sits in the menu on phones. |
| `StartScreen` | Scene background, new CTA, optional farewell line |
| `main.tsx` | `mount({ farewell })` for the leave path |
| `TournamentOver` | New layout and the leave button |
| `tokens.css`, `layout.css` | Night palette and the scene layout |

## Testing

- All existing unit tests, `test:golden` and e2e pass. Test ids and keyboard behaviour do not change.
- New unit tests, failing first:
  - `scene.test.ts`: each plate's geometry record has every property, and `Scene` renders the portrait and
    landscape sources.
  - `TournamentOver`: the leave button calls `onLeave`, and Play again calls `onPlayAgain`.
  - `copy.test.tsx` covers the new strings through the existing locale check.
- `e2e/layout.spec.ts` runs unchanged on phone and desktop projects.
- A new e2e check: after "That's enough for tonight" the start screen shows the farewell line, and after the
  next "Take a seat" a new tournament starts at hand 1.
- `StartScreen` renders the farewell line in place of the tagline when the flag is set.
- `npm run contact` produces both contact sheets. They are reviewed before the PR, and every state from the
  brief must be legible over the plate.
- Budget: the scene assets add at most 900 KB to the first load. Fonts add at most 120 KB.

## Out of scope

- The WebGL table layer, real dealing physics and the player's visible hands (the next stage).
- Sound.
- Other rooms or a room picker.
- Coach panel content.
