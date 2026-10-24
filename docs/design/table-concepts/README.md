# Table concepts

Exploration for the realistic table, 2026-10-23. Each background is an AI-generated photo plate (ByteDance Seedream 5 Pro through ElevenLabs), with the game layer drawn on top in `mock.html`. Full-resolution originals and prompts are in the ElevenLabs flow "Mallorca table plates".

Chosen direction: **attic nook** (`plates/attic-nook-2.jpg`). It is a cozy evening game for two close people: warm low light, midnight teal felt, snow outside. Not a casino and not a bar. The player sits at the centre of the table and the opponent sits directly opposite.

## Plates

| File | Mood | Notes |
| --- | --- | --- |
| `attic-nook-1.jpg`, `attic-nook-2.jpg` | Attic nook, fairy lights, small gable window with a snowy village | Chosen (2). Rectangular table, centred seat |
| `attic-nook-portrait.jpg` | The same room, 9:16 for phones | Generated from `attic-nook-2` as a reference with `aspect_ratio` set to 9:16 on the node. A prompt alone does not change the aspect ratio |
| `attic-window-1.jpg`, `attic-window-2.jpg` | Chalet attic, fabric pendant lamp, large window with a snowy forest behind the opponent | Strong alternative, oval table, centred seat |
| `attic-low-1.jpg`, `attic-low-2.jpg` | Same attic with a lower camera | Rejected: thin strip of felt, a chair back in the foreground |
| `cabin-fireplace-1.jpg`, `cabin-fireplace-2.jpg` | Cabin living room, fireplace, bookshelves, window to the side | Alternative, centred seat |
| `club-1.jpg`, `club-2.jpg` | Private club, bar bokeh, teal felt | Rejected mood (bar), but this is where the teal felt and warm light came from |
| `club-pool-1.jpg`, `club-pool-2.jpg` | Club | Rejected: the model drew a pool table |
| `backroom-1.jpg`, `backroom-2.jpg` | Back room, brass lamp, green felt | Rejected: corner view, casino mood |
| `finca-1.jpg` to `finca-3.jpg` | Mallorcan terrace at night | Rejected: table low in the frame |

## Mockups

`mockups/*.jpg` holds desktop (1440×810) and phone (390×844) captures of each mood with the game layer on top. Landscape plates are cropped for the phone captures. The real build needs a dedicated portrait plate.

## mock.html

This is the throwaway prototype that produced the mockups, not product code. It reads `plates/<p>.jpg` relative to itself and takes query parameters: `v` sets the UI style (`a`, `b`, `c`, `d`), `p` sets the plate file name, `d=phone` switches to the phone layout, `side=1` puts the action panel on the right, and any `--css-var=value` overrides layout variables such as `--board-y`.
