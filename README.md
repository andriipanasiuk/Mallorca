# Mallorca
Texas Holder poker bot

### Short algrorithm
Based on neural networks and decision-tree model.
Calculation of actions' profit is based on prediction of opponent game model.

### Build
Gradle is using as build system. Project is separated to few gradle projects (submodules).
Gradle plugin for Eclipse is used so project can be opened in Eclipse as well as Intellij IDEA.

### Web game
`web/` holds a TypeScript browser game: heads-up No-Limit Hold'em against a port of the Mallorca math bot.
The Java modules above stay as the archive and as the reference for golden tests. See `web/CLAUDE.md` and `web/ARCHITECTURE.md`.
The deploy workflow publishes the game to https://andrewforester.github.io/mallorca/ once GitHub Pages is enabled for the repository.
