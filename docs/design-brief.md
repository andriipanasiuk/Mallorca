# Design brief: Mallorca heads-up poker

## Product

A web game: one person plays heads-up No-Limit Texas Hold'em against the Mallorca bot, a tournament with
2000 chips each and rising blinds, until one side has all the chips. Later the same table becomes a training
mode, where the bot also explains its moves and suggests better ones, so the layout needs room for a hint
or coach panel that is empty in version one.

## Devices

Two layouts, equally important:

- Phone, portrait: bot at the top, table in the middle, the player's cards and controls at the bottom within
  thumb reach.
- Desktop, landscape: the table in the centre, controls under it, a hand log on the side.

## Elements

| Element    | Content                                                                       |
| ---------- | ----------------------------------------------------------------------------- |
| Seat       | name, stack, current bet, dealer button, "thinking" state for the bot          |
| Hole cards | two cards per seat; the bot's are face down until showdown                     |
| Board      | up to five community cards, dealt as flop, turn and river                      |
| Pot        | total pot                                                                      |
| Action bar | Fold, Check or Call with amount, Raise with amount; disabled states            |
| Bet slider | from minimum raise to all-in, presets half pot, pot and all-in                 |
| Hand log   | readable lines: "Bot raises to 60", "Flop Q♠ J♥ 7♦"                            |
| Status     | blind level, hand number, menu (new tournament, seed)                          |

## States

1. Start screen with "Start tournament".
2. Player to act: controls enabled, only legal actions offered.
3. Bot thinking: controls disabled, the bot seat shows it is thinking.
4. Action label: a short tag near the seat that just acted ("Raise 120", "Check").
5. Street dealt, and an all-in run-out where the remaining board is dealt with no betting.
6. Showdown: both hands open, winning hand named and highlighted, pot moves to the winner, split pot case.
7. Tournament over: win or loss, "Play again".

## Handoff

The implementation renders a ready view-model and styles it only with CSS tokens on `:root` (colours, fonts,
radii, spacing, shadows) and a dark theme. Cards are an SVG component that can take the designer's assets.
Components: Table, Seat, Card, Board, Pot, ActionBar, BetSlider, HandLog, StartScreen, TournamentOver.
A contact sheet with every state on both devices is produced from the running build for review.
