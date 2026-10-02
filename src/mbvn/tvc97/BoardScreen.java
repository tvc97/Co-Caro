package mbvn.tvc97;

import java.util.Vector;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;

/**
 * The game itself: a scrolling {@value CaroEngine#SIZE}x{@value CaroEngine#SIZE}
 * board with a cursor.
 *
 * <p>Keys: arrows or 2/4/6/8 move the cursor (1/3/7/9 diagonally), fire or 5
 * places a stone, the left soft key undoes the last turn, the right soft key
 * returns to the menu. After each human move the computer answers at once.
 *
 * <p>The view keeps the cursor centered, clamped to the board edges. When the
 * cursor jumps, the view eases toward its new position over a few frames.
 *
 * @author Tvc97
 * @forum  http://mbvn.tk
 */
public class BoardScreen implements Screen {

    private static final int GRID_COLOR = 0xe0e0e0;
    private static final int WIN_LINE_COLOR = 0xff0000;
    /** Cursor tint and result-text colors, by piece style (X or O). */
    private static final int[] CURSOR_COLORS = {0xffcccc, 0xccffcc};
    private static final int[] RESULT_COLORS = {0xff0000, 0x00ff00};
    private static final String WIN_TEXT = "BẠN ĐÃ THẮNG :)"; // "You won :)"
    private static final String LOSE_TEXT = "BẠN ĐÃ THUA :("; // "You lost :("

    /** Size of the translucent tile used to dim the board after a game ends. */
    private static final int DIM_TILE_SIZE = 20;
    /** Pause before the computer moves, so its turn is visible. */
    private static final int COMPUTER_DELAY_MILLIS = 300;
    /** Percentage of the scroll animation kept each frame. */
    private static final int SCROLL_DAMPING_PERCENT = 70;

    private static final int CENTER = CaroEngine.SIZE / 2;
    private static final int LAST_CELL = CaroEngine.SIZE - 1;

    private final Game game;
    final CaroEngine engine = new CaroEngine();

    private final int cellSize;
    private final int boardPixels;
    /** Most negative allowed board origin (board's right/bottom edge on screen edge). */
    private final int minOriginX, minOriginY;

    private final Image computerTurnIcon, humanTurnIcon, undoIcon, dimTile;
    private final Font resultFont;

    /** Turn history for undo; holds {@link Move}s. */
    private final Vector moves = new Vector();

    /** 0: human plays X, 1: human plays O. Index into the sprite sheet. */
    private int pieceStyle;
    private int cursorX, cursorY;
    /** Screen position of the board's top-left corner, from the last draw. */
    private int originX, originY;
    /** Remaining scroll animation in pixels; decays toward 0 each frame. */
    private int scrollX, scrollY;
    private boolean gameOver;
    private boolean humanWon;
    private boolean humansTurn;

    public BoardScreen(Game game) {
        this.game = game;
        pieceStyle = 0;
        resultFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_LARGE);

        computerTurnIcon = Resources.loadImage("com");
        humanTurnIcon = Resources.loadImage("hum");
        undoIcon = Resources.loadImage("undo");
        dimTile = Resources.loadImage("opacity");

        cellSize = game.pieces.getWidth() / 2;
        boardPixels = cellSize * CaroEngine.SIZE;
        minOriginX = -(boardPixels - game.width);
        minOriginY = -(boardPixels - game.height);
        reset();
    }

    /**
     * Starts a new game.
     *
     * @param level          one of the {@code CaroEngine.LEVEL_*} constants
     * @param pieceStyle     0 if the human plays X, 1 for O
     * @param computerFirst  whether the computer opens in the center
     */
    void startNewGame(int level, int pieceStyle, boolean computerFirst) {
        reset();
        engine.level = level;
        this.pieceStyle = pieceStyle;
        if (computerFirst) {
            engine.cells[CENTER][CENTER] = CaroEngine.COMPUTER;
        }
    }

    private void reset() {
        engine.reset();
        moves.setSize(0);
        cursorX = CENTER;
        cursorY = CENTER;
        scrollX = 0;
        scrollY = 0;
        humanWon = false;
        gameOver = false;
        humansTurn = true;
        Runtime.getRuntime().gc();
    }

    // ------------------------------------------------------------------
    // Drawing
    // ------------------------------------------------------------------

    public void draw(Graphics g) {
        int width = game.width;
        int height = game.height;
        g.setClip(0, 0, width, height);
        updateOrigin();

        int left = originX + scrollX;
        int top = originY + scrollY;

        g.setColor(CURSOR_COLORS[pieceStyle]);
        g.fillRect(left + cursorX * cellSize, top + cursorY * cellSize, cellSize, cellSize);

        g.setColor(GRID_COLOR);
        for (int i = 0; i <= CaroEngine.SIZE; i++) {
            g.drawLine(left, top + i * cellSize, left + boardPixels, top + i * cellSize);
        }
        for (int i = 0; i <= CaroEngine.SIZE; i++) {
            g.drawLine(left + i * cellSize, top, left + i * cellSize, top + boardPixels);
        }
        drawStones(g, left, top);

        g.drawImage(humansTurn ? humanTurnIcon : computerTurnIcon, 2, 2, Graphics.TOP | Graphics.LEFT);
        if (gameOver) {
            drawGameOver(g, left, top);
        }
        g.drawImage(game.backIcon, width - 2, height - 2, Graphics.BOTTOM | Graphics.RIGHT);
        g.drawImage(undoIcon, 2, height - 2, Graphics.BOTTOM | Graphics.LEFT);
    }

    /**
     * Places the board so the cursor is centered on screen, without showing
     * space beyond the board edges. Reaching the top/left edge also cancels
     * the scroll animation on that axis.
     */
    private void updateOrigin() {
        originX = game.width / 2 - cursorX * cellSize - cellSize / 2;
        originY = game.height / 2 - cursorY * cellSize - cellSize / 2;
        if (originX > 0) {
            originX = 0;
            scrollX = 0;
        }
        if (originY > 0) {
            scrollY = 0;
            originY = 0;
        }
        if (originX < minOriginX) {
            originX = minOriginX;
        }
        if (originY < minOriginY) {
            originY = minOriginY;
        }
    }

    private void drawStones(Graphics g, int left, int top) {
        int[][] cells = engine.cells;
        for (int x = 0; x < CaroEngine.SIZE; x++) {
            for (int y = 0; y < CaroEngine.SIZE; y++) {
                if (cells[x][y] != CaroEngine.EMPTY) {
                    int sprite = cells[x][y] == CaroEngine.HUMAN ? pieceStyle : 1 - pieceStyle;
                    g.drawRegion(game.pieces, sprite * cellSize, 0, cellSize, cellSize, Sprite.TRANS_NONE,
                            left + x * cellSize, top + y * cellSize, Graphics.TOP | Graphics.LEFT);
                }
            }
        }
    }

    /** Strikes through the winning five, dims the screen and shows the result. */
    private void drawGameOver(Graphics g, int left, int top) {
        int half = cellSize / 2;
        int lastStep = CaroEngine.WIN_LENGTH - 1;
        int startX = left + engine.winX * cellSize + half;
        int startY = top + engine.winY * cellSize + half;
        int endX = startX + engine.winDx * lastStep * cellSize;
        int endY = startY + engine.winDy * lastStep * cellSize;
        g.setColor(WIN_LINE_COLOR);
        g.drawLine(startX, startY, endX, endY);

        g.setFont(resultFont);
        int columns = game.width / DIM_TILE_SIZE + 1;
        int rows = game.height / DIM_TILE_SIZE + 1;
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                g.drawImage(dimTile, column * DIM_TILE_SIZE, row * DIM_TILE_SIZE, Graphics.TOP | Graphics.LEFT);
            }
        }
        g.setColor(RESULT_COLORS[pieceStyle]);
        g.drawString(humanWon ? WIN_TEXT : LOSE_TEXT, game.width / 2, game.height / 2,
                Graphics.HCENTER | Graphics.BASELINE);
    }

    public void update() {
        scrollX = scrollX * SCROLL_DAMPING_PERCENT / 100;
        scrollY = scrollY * SCROLL_DAMPING_PERCENT / 100;
    }

    // ------------------------------------------------------------------
    // Input and turns
    // ------------------------------------------------------------------

    public void keyPressed(int key) {
        if (!gameOver) {
            moveCursor(key);
        }
        if (Keys.isSelect(key)) {
            playHumanMove();
        }
        if (key == Keys.SOFT_LEFT) {
            undo();
        }
        if (Keys.isBack(key)) {
            game.showScreen(Game.MENU);
        }
    }

    /** Moves the cursor one cell; the corner number keys move diagonally. */
    private void moveCursor(int key) {
        if (Keys.isUp(key) || key == Canvas.KEY_NUM1 || key == Canvas.KEY_NUM3) {
            cursorY = Math.max(0, cursorY - 1);
            startScroll(0, -cellSize);
        }
        if (Keys.isDown(key) || key == Canvas.KEY_NUM7 || key == Canvas.KEY_NUM9) {
            cursorY = Math.min(LAST_CELL, cursorY + 1);
            startScroll(0, cellSize);
        }
        if (Keys.isLeft(key) || key == Canvas.KEY_NUM1 || key == Canvas.KEY_NUM7) {
            cursorX = Math.max(0, cursorX - 1);
            startScroll(-cellSize, 0);
        }
        if (Keys.isRight(key) || key == Canvas.KEY_NUM3 || key == Canvas.KEY_NUM9) {
            cursorX = Math.min(LAST_CELL, cursorX + 1);
            startScroll(cellSize, 0);
        }
    }

    /**
     * Adds a scroll animation for a one-cell cursor step, but only while the
     * board is not pinned to an edge on that axis (otherwise the view stays
     * put and only the cursor moves).
     */
    private void startScroll(int deltaX, int deltaY) {
        if (deltaX == 0 && originY > minOriginY && originY < 0) {
            scrollY += deltaY;
        }
        if (deltaY == 0 && originX > minOriginX && originX < 0) {
            scrollX += deltaX;
        }
    }

    /** Places the human's stone at the cursor, then lets the computer answer. */
    private void playHumanMove() {
        int x = cursorX;
        int y = cursorY;
        if (engine.cells[x][y] != CaroEngine.EMPTY) {
            return;
        }
        engine.cells[x][y] = CaroEngine.HUMAN;
        if (engine.checkWin()) {
            endGame(true);
            moves.addElement(new Move(x, y, Move.NO_REPLY, Move.NO_REPLY));
            return;
        }

        // Show the computer's turn indicator before it starts thinking.
        humansTurn = false;
        game.repaintNow();
        try {
            Thread.sleep(COMPUTER_DELAY_MILLIS);
        } catch (Exception e) {
        }

        engine.findBestMove();
        int replyX = engine.bestX;
        int replyY = engine.bestY;
        engine.cells[replyX][replyY] = CaroEngine.COMPUTER;
        moves.addElement(new Move(x, y, replyX, replyY));
        jumpCursorTo(replyX, replyY);
        if (engine.checkWin()) {
            endGame(false);
        }
        humansTurn = true;
    }

    private void endGame(boolean humanWon) {
        gameOver = true;
        this.humanWon = humanWon;
        game.achievements.record(engine.level, humanWon);
    }

    /** Takes back the last turn (the human's stone and the computer's reply). */
    private void undo() {
        int count = moves.size();
        if (count == 0) {
            return;
        }
        Move last = (Move) moves.elementAt(count - 1);
        engine.cells[last.humanX][last.humanY] = CaroEngine.EMPTY;
        if (last.hasComputerReply()) {
            engine.cells[last.computerX][last.computerY] = CaroEngine.EMPTY;
        }
        moves.removeElementAt(count - 1);
        gameOver = false;

        if (count > 1) {
            Move previous = (Move) moves.elementAt(count - 2);
            jumpCursorTo(previous.computerX, previous.computerY);
        }
    }

    /** Moves the cursor to a cell, animating the view from the old position. */
    private void jumpCursorTo(int x, int y) {
        scrollX = (x - cursorX) * cellSize;
        scrollY = (y - cursorY) * cellSize;
        cursorX = x;
        cursorY = y;
    }
}
