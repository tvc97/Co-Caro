package mbvn.tvc97;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * New-game setup: pick the difficulty, the human's stone (X or O) and who
 * moves first. Up/down moves between rows, left/right changes the focused
 * row's value, select or the left soft key starts the game.
 *
 * <p>Touch: tap the left or right half of the level or first-player row to
 * step its value back or forward, tap X or O to pick a stone, tap the play
 * icon to start.
 *
 * @author Tvc97
 * @forum  http://mbvn.tk
 */
public class LevelSelectScreen implements Screen {

    /** Level names, indexed by the {@code CaroEngine.LEVEL_*} constants. */
    static final String[] LEVEL_NAMES = {
        "Tập sự", // Beginner
        "Bình thường", // Normal
        "Cao thủ" // Expert
    };
    private static final String[] FIRST_PLAYER_NAMES = {
        "Người", // Human
        "Máy" // Computer
    };

    private static final int ROW_LEVEL = 0;
    private static final int ROW_PIECE = 1;
    private static final int ROW_FIRST_PLAYER = 2;
    private static final int ROW_COUNT = 3;
    /** Number of choices per row. */
    private static final int[] CHOICE_COUNTS = {LEVEL_NAMES.length, 2, FIRST_PLAYER_NAMES.length};
    private static final int FIRST_PLAYER_COMPUTER = 1;

    private static final int LABEL_COLOR = 0xa0a0a0;
    private static final int VALUE_COLOR = 0xaeef;
    private static final int FOCUS_COLOR = 0xff0000;
    private static final int X_TINT_COLOR = 0xffdddd;
    private static final int O_TINT_COLOR = 0xaaffaa;

    private final Game game;
    private final Font font;
    private final int rowHeight;
    private final int top;
    private final Image playIcon;

    private int focusedRow;
    /** Current choice for each row. */
    private final int[] choices = new int[ROW_COUNT];

    public LevelSelectScreen(Game game) {
        this.game = game;
        font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
        rowHeight = font.getHeight() + 4;
        top = game.height / 2 - rowHeight * 4;
        focusedRow = ROW_LEVEL;
        playIcon = Resources.loadImage("play");
    }

    public void draw(Graphics g) {
        int centerX = game.width / 2;
        int iconSize = game.pieces.getHeight();
        int piecesTop = top + rowHeight * 3;
        int anchor = Graphics.HCENTER | Graphics.TOP;

        g.setFont(font);
        g.setColor(LABEL_COLOR);
        g.drawString("Mức chơi", centerX, top, anchor); // "Level"
        g.drawString("Quân cờ", centerX, top + rowHeight * 2, anchor); // "Your piece"
        g.drawString("Đánh trước", centerX, piecesTop + iconSize + 4, anchor); // "Moves first"

        g.setColor(VALUE_COLOR);
        g.drawString(withArrows(LEVEL_NAMES[choices[ROW_LEVEL]], ROW_LEVEL),
                centerX, top + rowHeight, anchor);
        g.drawString(withArrows(FIRST_PLAYER_NAMES[choices[ROW_FIRST_PLAYER]], ROW_FIRST_PLAYER),
                centerX, piecesTop + rowHeight + iconSize + 8, anchor);

        // X and O side by side, the chosen one on a tinted square.
        int chosenLeft = centerX - iconSize + choices[ROW_PIECE] * iconSize;
        g.setColor(choices[ROW_PIECE] == 0 ? X_TINT_COLOR : O_TINT_COLOR);
        g.fillRect(chosenLeft, piecesTop, iconSize, iconSize);
        g.drawImage(game.pieces, centerX, piecesTop, anchor);
        if (focusedRow == ROW_PIECE) {
            g.setColor(FOCUS_COLOR);
            g.drawRect(chosenLeft, piecesTop, iconSize, iconSize);
        }

        g.drawImage(game.backIcon, game.width - 2, game.height - 2, Graphics.BOTTOM | Graphics.RIGHT);
        g.drawImage(playIcon, 2, game.height - 2, Graphics.BOTTOM | Graphics.LEFT);
    }

    /** Wraps the value in "&lt; &gt;" when its row has focus. */
    private String withArrows(String value, int row) {
        return row == focusedRow ? "< " + value + " >" : value;
    }

    public boolean update() {
        return false;
    }

    public void keyPressed(int key) {
        if (Keys.isUp(key)) {
            focusedRow = wrap(focusedRow - 1, ROW_COUNT);
        }
        if (Keys.isDown(key)) {
            focusedRow = wrap(focusedRow + 1, ROW_COUNT);
        }
        if (Keys.isLeft(key)) {
            changeChoice(-1);
        }
        if (Keys.isRight(key)) {
            changeChoice(1);
        }
        if (Keys.isSelect(key) || key == Keys.SOFT_LEFT) {
            startGame();
        }
        if (Keys.isBack(key)) {
            game.showScreen(Game.MENU);
        }
    }

    public void tapped(int x, int y) {
        int centerX = game.width / 2;
        int iconSize = game.pieces.getHeight();
        int piecesTop = top + rowHeight * 3;
        int firstPlayerTop = piecesTop + iconSize + 4;

        if (game.touchesBottomLeft(playIcon, x, y)) {
            startGame();
        } else if (game.touchesBackIcon(x, y)) {
            game.showScreen(Game.MENU);
        } else if (y >= top && y < top + rowHeight * 2) {
            focusedRow = ROW_LEVEL;
            changeChoice(x < centerX ? -1 : 1);
        } else if (y >= top + rowHeight * 2 && y < piecesTop + iconSize
                && x >= centerX - iconSize && x < centerX + iconSize) {
            focusedRow = ROW_PIECE;
            choices[ROW_PIECE] = x < centerX ? 0 : 1;
        } else if (y >= firstPlayerTop && y < firstPlayerTop + rowHeight * 2 + 4) {
            focusedRow = ROW_FIRST_PLAYER;
            changeChoice(x < centerX ? -1 : 1);
        }
    }

    public void dragged(int dx, int dy) {
    }

    private void startGame() {
        game.board.startNewGame(choices[ROW_LEVEL], choices[ROW_PIECE],
                choices[ROW_FIRST_PLAYER] == FIRST_PLAYER_COMPUTER);
        game.showScreen(Game.BOARD);
    }

    private void changeChoice(int delta) {
        choices[focusedRow] = wrap(choices[focusedRow] + delta, CHOICE_COUNTS[focusedRow]);
    }

    /** Wraps {@code value} into {@code [0, count)}; handles {@code value == -1}. */
    private static int wrap(int value, int count) {
        return (value + count) % count;
    }
}
