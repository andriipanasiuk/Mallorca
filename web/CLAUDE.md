# Working in this package

- Node: `source ~/.nvm/nvm.sh && nvm use` before any npm command; the version comes from `.nvmrc`. Run commands from `web/`.
- Read `ARCHITECTURE.md` before adding a module or crossing a layer; it is the contract.
- Before every commit: `npx prettier --write <paths>`, `npx eslint <paths>`, `npx tsc --noEmit`, `npx vitest run <paths>`. Before a PR: `npm run lint`, `npm test`, `npm run test:golden`, `npm run build`, `npm run test:e2e`.
- TDD: the failing test comes first. Fix the code, not the rule: never loosen a tolerance, add an ignore, a suppression or delete an assertion. A rule changes only in its own commit.
- Layers import only downward: `ui` to `game` to `engine` and `brains` to `equity` to `cards`. ESLint enforces it per directory.
- `cards`, `equity`, `engine` and `brains` use no DOM, no `Math.random`, no `Date` and no `performance`. Randomness enters as an `Rng` argument.
- Chips are integers. Cards in fixtures and text are two-character strings: rank `23456789TJQKA`, suit `cdhs`.
- No comments in code or config, in any language.
- UI copy: no all-caps words, no em or en dashes as sentence punctuation.
- `npm run contact` builds, plays a seeded game and writes `e2e/contact/phone.png` and `e2e/contact/desktop.png` (gitignored); it is not part of `test:e2e`.
- Review screenshots as one contact sheet per device, not one file at a time.
- Git: feature branches, atomic conventional commits with the layer as scope (`feat(engine): ...`), never push to `main`, PRs are squash-merged. Run `git branch --show-current` before committing.
- Deploy: `.github/workflows/web-deploy.yml` publishes `web/dist` to GitHub Pages on every push to `master` that touches `web/`, built with `BASE_PATH=/mallorca/`. Pages must be set to "GitHub Actions" in the repository settings, a one-time manual step for the owner.
