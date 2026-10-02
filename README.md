# "Caro" chess (Gomoku) J2ME game

Five-in-a-row against the computer on a 30x30 board, for Java ME phones.

## System requirements
<ul>
  <li>MicroEdition-Configuration : CLDC-1.0</li>
  <li>MicroEdition-Profile : MIDP-2.0</li>
</ul>

## Controls

| Key | Action |
| --- | --- |
| Arrows / 2 4 6 8 | Move cursor or menu selection |
| 1 3 7 9 | Move cursor diagonally (in game) |
| Fire / 5 | Select, place a stone |
| Left soft key | Undo last turn (in game), start game (setup screen) |
| Right soft key | Back to main menu |
| * | Toggle the falling-pieces effect |

## Difficulty levels

Every level completes its own five and blocks yours.

| Level | Strategy | Result vs. next level (1000 AI-vs-AI games) |
| --- | --- | --- |
| Tập sự (Beginner) | Random pick among its 3 best-looking cells | loses 943 of 1000 to Normal |
| Bình thường (Normal) | Hottest cell of the attack/defense heat maps | loses 674 of 1000 to Expert |
| Cao thủ (Expert) | Normal + plays/blocks open fours and forks first | — |

## Code structure

All sources are in `src/mbvn/tvc97`, resources in `src/res`.

| Class | Role |
| --- | --- |
| `Midlet` | Entry point; starts the game loop, saves statistics on exit |
| `Game` | Canvas and frame loop (~25 ms); owns the screens and shared images |
| `Screen` | Interface every screen implements: `draw`, `update`, `keyPressed` |
| `IntroScreen`, `MenuScreen`, `LevelSelectScreen`, `BoardScreen`, `InfoScreen`, `AchievementScreen` | The six screens |
| `CaroEngine` | Board model, win detection and computer opponent |
| `Move` | One turn (human stone + computer reply), kept for undo |
| `AchievementStore` | Win/loss counts per level, saved in the record store |
| `LeafDrop`, `Leaf` | Decorative falling-pieces effect |
| `Keys`, `Resources` | Key-code helpers; resource loading and decoding |

Sources must stay Java 1.3 compatible (no generics, enums, annotations or
for-each loops) and avoid `float`/`double`, which CLDC 1.0 lacks.

## Building

Open the project in NetBeans with the Java ME SDK 3.0 platform, or run the
`build.xml` Ant script from that environment. Output goes to `dist/`.

## Credits
<ul>
  <li>Game Algorithm: http://bk02.sourceforge.net/jcaro/</li>
</ul>
