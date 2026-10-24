# Realistic Table Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the flat table with the attic-nook photo scene and a CSS 3D game layer, restyle every component to the night palette, and add the "That's enough for tonight" exit.

**Architecture:** A new `Scene` component paints the plate (`<picture>`, portrait or landscape) and exposes the plate's geometry as CSS custom properties. `Table` places zones absolutely over the scene. The felt layer is one element (`table-felt`) that a WebGL layer can replace later. Only `src/ui`, `src/locales`, `src/main.tsx`, styles, assets and tests change.

**Tech Stack:** Preact, Vite, CSS custom properties, `@fontsource` woff2 fonts, Vitest with happy-dom, Playwright.

**Spec:** `docs/superpowers/specs/2026-10-23-realistic-table-design.md`

## Global Constraints

- Run all commands from `web/` after `source ~/.nvm/nvm.sh && nvm use`.
- No comments in code or config.
- UI copy: no all-caps words, no em or en dashes as punctuation. English only through `en.json`.
- `ui` imports only from `game`. `game`, `engine`, `brains`, `equity` and `cards` are untouched.
- Keep every existing `data-testid`, aria label and keyboard shortcut.
- Existing assertions are never loosened or deleted. Only call sites change where a new required prop appears.
- Before each commit: `npx prettier --write <paths>`, `npx eslint <paths>`, `npx tsc --noEmit`, `npx vitest run <paths>`.
- Commits are conventional with the layer as scope, for example `feat(ui): ...`, on branch `worktree-realistic-table-design`.
- Assets: scene plates ≤ 400 KB each and ≤ 900 KB in total with placeholders. Fonts ≤ 120 KB.
- Tap targets ≥ 44px. Focus outline 3px `--color-accent`.
- The scene ignores `prefers-color-scheme`. Both token blocks hold the same night palette.

## Review Focus

1. A short landscape viewport (phone turned sideways, 844×390, or a laptop at 1280×600) gets the desktop layout. All controls must stay fully visible with no overlap and no horizontal scroll (test in Task 4).
2. A plate that fails to load (offline, 404) must still leave a readable table on the felt fallback colour (test in Task 1).
3. Long amounts ("Raise to 4000", "Call 1960") on the 360px small phone must not push buttons off screen (test in Task 5).
4. With `prefers-reduced-motion: reduce`, dealt cards and chips must not animate (test in Task 4).
5. When the leave path is followed by a normal "Take a seat", the farewell line must be gone and hand 1 must start (test in Task 6).

---

### Task 1: Scene plates, geometry and the `Scene` component

**Files:**
- Create: `src/ui/scene/attic-landscape.webp`, `attic-portrait.webp`, `attic-landscape-blur.webp`, `attic-portrait-blur.webp`
- Create: `src/ui/scene.ts`, `src/ui/Scene.tsx`, `src/ui/Scene.test.tsx`
- Create: `src/ui/assets.d.ts` only if `tsc` rejects `*.webp` imports (Vite client types usually cover them, so check `vite/client` in `tsconfig.json` first)
- Modify: `src/ui/styles/layout.css` (scene rules)
- Test: `src/ui/Scene.test.tsx`, `e2e/scene.spec.ts`

**Interfaces:**
- Produces: `type PlateGeometry = { focusX: string; focusY: string; feltTilt: string; opponentY: string; boardY: string; heroBottom: string; cardScale: string }`, `const LANDSCAPE: PlateGeometry`, `const PORTRAIT: PlateGeometry`, `function geometryStyle(g: PlateGeometry, prefix: 'l' | 'p'): Record<string, string>`. The prefix maps each field to `--l-focus-x` or `--p-focus-x` and so on, and CSS picks the set by media query.
- Produces: `function Scene({ children, dimmed }: { children: ComponentChildren; dimmed?: boolean })`. The root is `<div class="scene" data-testid="scene">`, then `<picture class="scene-plate">` with `<source media="(min-aspect-ratio: 1/1)" srcset={landscape}>` and `<img src={portrait} alt="">`, then a `.scene-vignette` div, then `.scene-content` holding the children. `dimmed` adds `scene-dimmed` (blur and darken, used by the start screen).

- [ ] **Step 1: Make the plates.** Convert `../docs/design/table-concepts/plates/attic-nook-2.jpg` to `src/ui/scene/attic-landscape.webp` at 2048 px wide, and the portrait source (`../.superpowers/mock/plates/fp1.png`, or `attic-nook-portrait.jpg` from the concepts folder) to `attic-portrait.webp` at 1152×2048. Use Pillow with quality 78. Make the two placeholders 32 px wide, quality 50. Run `ls -l src/ui/scene`. Expected: each plate ≤ 400 KB, each placeholder ≤ 3 KB.
- [ ] **Step 2: Write the failing test** `src/ui/Scene.test.tsx`:

```tsx
it('renders the landscape source for wide screens and the portrait image', () => {
  const { container } = render(<Scene><p>x</p></Scene>);
  const source = container.querySelector('picture source');
  expect(source?.getAttribute('media')).toBe('(min-aspect-ratio: 1/1)');
  expect(source?.getAttribute('srcset')).toContain('attic-landscape');
  expect(container.querySelector('picture img')?.getAttribute('src')).toContain('attic-portrait');
  expect(container.querySelector('picture img')?.getAttribute('alt')).toBe('');
});
it('exposes every geometry field for both plates as custom properties', () => {
  const { getByTestId } = render(<Scene><p>x</p></Scene>);
  const style = getByTestId('scene').getAttribute('style') ?? '';
  for (const p of ['l', 'p']) {
    for (const name of ['focus-x', 'focus-y', 'felt-tilt', 'opponent-y', 'board-y', 'hero-bottom', 'card-scale']) {
      expect(style).toContain(`--${p}-${name}`);
    }
  }
});
it('dims the scene behind the start screen', () => {
  const { getByTestId } = render(<Scene dimmed><p>x</p></Scene>);
  expect(getByTestId('scene').classList.contains('scene-dimmed')).toBe(true);
});
```

- [ ] **Step 3: Run** `npx vitest run src/ui/Scene.test.tsx`. Expected: FAIL, module not found.
- [ ] **Step 4: Implement `scene.ts` and `Scene.tsx`.** Start from the geometry tuned in the mockups. Landscape: focus `50% 60%`, tilt `56deg`, opponent `45%`, board `70%`, hero bottom `16px`, card scale `clamp(48px, 4.6vw, 72px)`. Portrait: focus `50% 55%`, tilt `50deg`, opponent `39%`, board `58%`, hero bottom `118px`, card scale `clamp(44px, 12vw, 56px)`. The placeholders go in as the `background-image` of `.scene` (inline style), with `background-color: var(--color-felt)` under them. In `layout.css`, `.scene` is `position: fixed; inset: 0`. The image covers it with `object-fit: cover; object-position: var(--focus-x) var(--focus-y)`. A media query maps `--focus-x` and the rest to `--l-*` at `(min-aspect-ratio: 1/1)` and to `--p-*` otherwise.
- [ ] **Step 5: Run** `npx vitest run src/ui/Scene.test.tsx`. Expected: PASS.
- [ ] **Step 6: Write the failing e2e test** `e2e/scene.spec.ts`, `the table stays playable when the plate fails to load`. Route `**/*.webp` to `abort()`, open `FAST_URL`, click `start`, expect `passive` enabled and in the viewport, and expect the computed `background-color` of `[data-testid="scene"]` to equal the felt fallback `rgb(31, 74, 82)`. The test depends on Task 4 wiring `Scene` into `App`, so mark it `test.fixme` here and remove `fixme` in Task 4 Step 6. Note that the `problems` fixture fails on console errors, so the aborted image must not log an error. If Chromium logs one, filter `net::ERR_FAILED` for the aborted plate URLs only, inside this spec.
- [ ] **Step 7: Commit** `feat(ui): add the attic scene plates and Scene component`.

### Task 2: Night palette, fonts and the glass primitives

**Files:**
- Modify: `src/ui/styles/tokens.css`, `package.json`, `package-lock.json`, `src/main.tsx` (font imports)
- Test: `src/ui/contrast.test.ts` (unchanged, must pass)

**Interfaces:**
- Produces tokens used by every later task: `--color-text #f6ecda`, `--color-muted #c9b89c`, `--color-bg #1a130d`, `--color-surface #2a1e14`, `--color-surface-alt #3a2a1c`, `--color-glass rgba(34, 24, 16, 0.5)`, `--color-glass-border rgba(255, 226, 180, 0.2)`, `--color-accent #e3b06e`, `--color-accent-text #2a1a0c`, `--color-danger #e9a48f`, `--color-danger-text #2a1a0c`, `--color-felt #1f4a52`, `--color-felt-text #f6ecda`, `--color-card-face #fbf6ea`, `--color-card-red #b3202a`, `--color-card-black #1c1c1e`, `--color-card-back #1f4a52`, `--color-card-back-line #2c6370`, `--color-highlight #f2c27a`, `--color-chip-terracotta #b0583c`, `--color-chip-teal #2f4f57`, `--color-chip-honey #c99a4a`, `--color-chip-ivory #e9dcc4`, `--font-display 'Cormorant Garamond', Georgia, serif`, `--font-body 'Inter', system-ui, sans-serif`, `--font-numeric 'IBM Plex Mono', ui-monospace, monospace`, `--blur-glass 14px`, `--shadow-glow 0 0 24px rgba(240, 180, 100, 0.35)`.
- Produces the classes `.glass` (background `--color-glass`, `backdrop-filter: blur(var(--blur-glass))`, 1px `--color-glass-border`, and the `--color-surface` fallback under `@supports not (backdrop-filter: blur(1px))`) and `.pill` (radius 999px).

- [ ] **Step 1: Run** `npx vitest run src/ui/contrast.test.ts` to record the baseline. Expected: PASS.
- [ ] **Step 2: Replace both token blocks with the night palette above.** Keep every token name that `contrast.test.ts` reads. The `@media (prefers-color-scheme: dark)` block repeats the same values.
- [ ] **Step 3: Run** `npx vitest run src/ui/contrast.test.ts`. Expected: PASS, all 10 pairs at 4.5:1 or above. If a pair falls short, darken the background token, never the threshold.
- [ ] **Step 4: Install the fonts.** Run `npm i @fontsource/cormorant-garamond @fontsource/inter @fontsource/ibm-plex-mono`. Import only `latin-500.css`, `latin-600.css` (Cormorant), `latin-400.css`, `latin-600.css` (Inter) and `latin-500.css` (Plex Mono) in `main.tsx`.
- [ ] **Step 5: Add `.glass` and `.pill`** to `layout.css`, and set `body { background: var(--color-bg); }`.
- [ ] **Step 6: Run** `npm run build`, then `du -ch dist/assets/*.woff2 | tail -1`. Expected: ≤ 120 KB.
- [ ] **Step 7: Commit** `feat(ui): night palette, self-hosted fonts and glass surfaces`.

### Task 3: Cards and chips

**Files:**
- Modify: `src/ui/Card.tsx`, `src/ui/Pot.tsx`, `src/ui/styles/layout.css`
- Test: `src/ui/Card.test.tsx`, `src/ui/Pot.test.tsx` (new)

**Interfaces:**
- `Card` props are unchanged (`card`, `highlight`). The face SVG has viewBox `0 0 250 350`, a paper gradient, a top-left index (rank `<text>` and suit `<text>`), a rotated copy bottom right, and a large centre pip. The back is a teal weave `<pattern>` with no `<text>`. Each SVG uses a unique pattern or gradient id per instance (`useId` from `preact/hooks`), or a single shared `<defs>` in `Scene`. Pick shared defs if `useId` is not available in the installed Preact.
- `Pot` keeps `data-testid="pot"`, `data-amount` and the `pot.amount` text, and adds a decorative `<span class="chip-stack" aria-hidden="true">` with three `.chip` spans whose colours come from the chip tokens.

- [ ] **Step 1: Write the failing tests.** In `Card.test.tsx`, add `it('draws the corner index twice on a face card')`, which expects `container.querySelectorAll('.card-rank').length` toBe 2. In `Pot.test.tsx`, add `it('shows the amount with decorative chips hidden from assistive tech')`, which expects the text `Pot 140`, `data-amount="140"`, and `.chip-stack` with `aria-hidden="true"` and 3 `.chip` children.
- [ ] **Step 2: Run** `npx vitest run src/ui/Card.test.tsx src/ui/Pot.test.tsx`. Expected: the 2 new tests FAIL and the existing ones PASS.
- [ ] **Step 3: Implement** the new face, back and pot chips. Chips are CSS: `radial-gradient` centre and `repeating-conic-gradient` edge stripes, as in `docs/design/table-concepts/mock.html`.
- [ ] **Step 4: Run** `npx vitest run src/ui`. Expected: PASS, including `copy.test.tsx`, because the corner indices are glyph strings.
- [ ] **Step 5: Commit** `feat(ui): paper card faces, teal backs and chip stacks`.

### Task 4: Table zones over the scene, App shell and table states

**Files:**
- Modify: `src/ui/App.tsx`, `src/ui/Table.tsx`, `src/ui/Seat.tsx`, `src/ui/Board.tsx`, `src/ui/HandLog.tsx`, `src/ui/styles/layout.css`
- Test: `src/ui/Table.test.tsx`, `e2e/layout.spec.ts`, `e2e/scene.spec.ts`

**Interfaces:**
- Consumes: `Scene` (Task 1), the tokens, `.glass` and `.pill` (Task 2).
- `App` wraps the table phase in `<Scene>`. The status row (`blinds`, `hand-number`, the `<details class="menu">` with `new-tournament`) becomes `.scene-top`, a transparent row of `.glass` pills.
- `Table` DOM, in order: `.table-felt` (holding `Board`, then `Pot` or `result`), `Seat` with `position="top"` placed at `--opponent-y`, `Seat` with `position="bottom"` placed by the near rail, the `ActionBar`, then `.table-side` (`HandLog` and `coach`). Each `Seat` stays one `<section data-testid="seat-N">` containing its own cards, because `App.test.tsx` looks for the cards inside the seat element.
- `.table-felt` is the swap point for the later WebGL layer: absolutely positioned at `--board-y`, with `transform: perspective(900px) rotateX(var(--felt-tilt))`.
- New state classes: `.seat-active` on the bottom seat adds a `--shadow-glow` ellipse under the cards. `.tag-thinking` gets a 2.4s opacity pulse. `.board .card` gets a 250ms `deal-in` animation (translate from the dealer side and fade). `.card-highlight` gets a warm `drop-shadow` halo in `--color-highlight`. While `result` is shown, `.table-felt` renders a decorative `<span class="result-chips result-chips-to-top|to-bottom|split" aria-hidden="true">`. The class follows `vm.result.winner` relative to `humanSeat`. It plays a 600ms drift toward the winner's plaque, or splits in both directions. All five animations sit inside `@media (prefers-reduced-motion: no-preference)`.
- Phones (`max-aspect-ratio: 1/1`): `.table-side` is `display: none`, and the menu `<details>` holds a second `HandLog` (`data-testid="hand-log-menu"`). The original `hand-log` stays in the DOM for tests. Hide it with CSS on phones, not by conditional rendering.

- [ ] **Step 1: Write the failing tests.** In `Table.test.tsx`, add:
  - `it('keeps both seats and the felt layer in order')`, expecting `.table-felt` to contain `[data-testid="board"]` and `[data-testid="pot"]`, and `seat-1` to contain 2 `card-back`.
  - `it('marks the player seat active on their turn')`, expecting `seat-0` to have class `seat-active` when `vm.seats[0].isToAct`.
  - `it('sends the pot chips toward the winner')`. With `fixtureResult()` (winner 1, human 0), expect `.result-chips-to-top` with `aria-hidden="true"`. With `winner: null`, expect `.result-chips-split`.
  In `e2e/layout.spec.ts`, add `test('a short landscape screen keeps every control visible')`, which sets the viewport to 844×390 and to 1280×600 in turn and repeats the same checks as the existing test. In `e2e/scene.spec.ts`, add `test('reduced motion turns off the deal animation')` with `page.emulateMedia({ reducedMotion: 'reduce' })`. Play to the flop with `passive` clicks and assert `await page.locator('[data-testid="board"] [data-testid="card"]').first().evaluate((el) => el.getAnimations().length)` is 0.
- [ ] **Step 2: Run** `npx vitest run src/ui/Table.test.tsx`. Expected: the new tests FAIL.
- [ ] **Step 3: Implement** the zones, the App shell and the state styles in `layout.css`. Replace the old flat table rules (`.table-center`, the seat boxes and the `min-aspect-ratio` block) with scene-relative positioning driven by `--opponent-y`, `--board-y`, `--hero-bottom` and `--card-scale`. Desktop places `.table-side` at the top right, 18rem wide, on `.glass`.
- [ ] **Step 4: Run** `npx vitest run src/ui`. Expected: PASS, including the unchanged `App.test.tsx` and `copy.test.tsx`.
- [ ] **Step 5: Run** `npm run test:e2e -- e2e/layout.spec.ts e2e/scene.spec.ts`. Expected: PASS on phone, small-phone and desktop. A failure of the short landscape test means a layout fix, never a removed check.
- [ ] **Step 6: Remove `test.fixme`** from the Task 1 e2e test, then run `npm run test:e2e -- e2e/scene.spec.ts`. Expected: PASS.
- [ ] **Step 7: Commit** `feat(ui): place the table zones on the attic scene`.

### Task 5: Action panel and bet slider

**Files:**
- Modify: `src/ui/ActionBar.tsx` (class names only), `src/ui/BetSlider.tsx` (class names only), `src/ui/styles/layout.css`
- Test: `src/ui/ActionBar.test.tsx`, `e2e/layout.spec.ts`

**Interfaces:**
- Behaviour and test ids are unchanged. The visual order is Raise first and full width, then Fold and Check or Call side by side, done with CSS `order`. DOM order stays Fold, passive, Raise, so keyboard tab order is unchanged.
- Buttons get `.glass .pill` and use `--font-display` at 20px. Raise is filled `--color-accent` with `--shadow-glow`, and Fold uses `--color-danger` text. Presets are small `.glass .pill` buttons. The range uses `accent-color: var(--color-accent)` with a cream thumb.
- Desktop: `.action-bar` is fixed at the bottom right, 380px wide, 24px from the edges. Phone: it is fixed at the bottom across the full width with a 12px gutter and `env(safe-area-inset-bottom)`.

- [ ] **Step 1: Write the failing test.** In `e2e/layout.spec.ts`, add `test('long amounts stay inside the small phone')`. After `start`, click `preset-allin`, then expect `raise` and `passive` in the viewport with ratio 1 and no horizontal scroll. It runs on the `small-phone` project at 360×740.
- [ ] **Step 2: Run** `npm run test:e2e -- e2e/layout.spec.ts`. Expected: FAIL, or PASS only by accident with the old layout. Note which.
- [ ] **Step 3: Implement** the styles. Labels never wrap: `white-space: nowrap`, and the amount in `--font-numeric` shrinks with `font-size: clamp(15px, 4.2vw, 20px)`.
- [ ] **Step 4: Run** `npx vitest run src/ui/ActionBar.test.tsx src/ui/BetSlider.test.tsx` and `npm run test:e2e -- e2e/layout.spec.ts`. Expected: PASS.
- [ ] **Step 5: Commit** `feat(ui): glass action panel and bet slider`.

### Task 6: Start screen, tournament end and the leave path

**Files:**
- Modify: `src/locales/en.json`, `src/ui/StartScreen.tsx`, `src/ui/TournamentOver.tsx`, `src/ui/App.tsx`, `src/main.tsx`
- Modify call sites only: `src/ui/App.test.tsx` (pass `onLeave={vi.fn()}`), `src/ui/copy.test.tsx` (pass `onLeave={vi.fn()}` to `TournamentOver`)
- Test: `src/ui/StartScreen.test.tsx` (new), `src/ui/TournamentOver.test.tsx` (new), `src/ui/copy.test.tsx`, `e2e/game.spec.ts`

**Interfaces:**
- `en.json` changes: `start.cta` becomes "Take a seat". Add `start.farewell` "Sleep well. The table will be here.", `over.title` "Good game" and `over.leave` "That's enough for tonight".
- `StartScreen({ onStart, seed, farewell }: { onStart: () => void; seed: number; farewell?: boolean })`. It renders inside `<Scene dimmed>`. With `farewell`, the tagline `<p class="start-tagline">` shows `start.farewell` and carries `data-testid="farewell"`.
- `TournamentOver({ won, onPlayAgain, onLeave }: { won: boolean; onPlayAgain: () => void; onLeave: () => void })`. It shows the `over.title` heading, then the `over.won` or `over.lost` line, then `play-again` and a new `leave` button (`.glass .pill`).
- `App` props gain `onLeave: () => void` and `farewell: boolean`. It passes `farewell` to `StartScreen` and `onLeave` to `TournamentOver`.
- `main.tsx`: `mount(farewell = false)`. The tournament-over leave calls `mount(true)`, while `onNewTournament` and play-again call `mount()`. The flag only lives in the render, so the next `mount()` clears it.

- [ ] **Step 1: Write the failing tests.**
  - `TournamentOver.test.tsx`: `it('offers to play again or to stop for tonight')` clicks `play-again` and `leave` and expects each spy called once. `it('names the outcome under the title')` expects the text "Good game" and "You won the tournament".
  - `StartScreen.test.tsx`: `it('shows the farewell line in place of the tagline')` expects `farewell` text "Sleep well. The table will be here." and no tagline text. `it('shows the tagline by default')`.
  - `copy.test.tsx`: add `expectFromLocale(render(<StartScreen seed={1} onStart={vi.fn()} farewell />).container)`.
  - `e2e/game.spec.ts`: add `test('leaving for tonight returns to a farewell start screen')`. Reaching tournament-over quickly needs a seed and all-in clicks, so reuse the existing all-in loop from the first test, extracted into a helper `playToTheEnd(page)` in `e2e/fixtures.ts`. Then click `leave`, expect `farewell` visible, click `start`, and expect `hand-number` to contain `1`. Reload the page and expect no `farewell` (Review Focus 5).
- [ ] **Step 2: Run** `npx vitest run src/ui`. Expected: the new tests FAIL and `tsc` reports the missing `onLeave` until the call sites are updated.
- [ ] **Step 3: Implement** the copy, the components, the App props, `main.tsx` and the call-site props in the two existing tests.
- [ ] **Step 4: Run** `npx vitest run src/ui` and `npm run test:e2e -- e2e/game.spec.ts`. Expected: PASS, including the unchanged play-again test.
- [ ] **Step 5: Commit** `feat(ui): attic start screen and a gentle way to stop`.

### Task 7: Full verification and the contact sheet

**Files:**
- Modify: only what the checks below find.

- [ ] **Step 1: Run** `npm run lint && npm test && npm run test:golden && npm run build && npm run test:e2e`. Expected: all green.
- [ ] **Step 2: Run** `du -ch src/ui/scene/*.webp | tail -1` and `du -ch dist/assets/*.woff2 | tail -1`. Expected: ≤ 900 KB and ≤ 120 KB.
- [ ] **Step 3: Run** `npm run contact` and review `e2e/contact/phone.png` and `e2e/contact/desktop.png`, one sheet per device. Check every state in the brief: start, to act, thinking, action tag, street dealt, all-in run-out, showdown with a halo, split, tournament over. Text must be legible over the plate and nothing may overlap. Fix and repeat until clean.
- [ ] **Step 4: Commit** any fixes as `fix(ui): ...`.
