package mbvn.tvc97;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.GameCanvas;

/**
 * Full-screen canvas that owns all screens and runs the frame loop.
 *
 * <p>Exactly one {@link Screen} is shown at a time; screens switch with
 * {@link #showScreen(int)}. Each frame (about 25 ms) the current screen is
 * updated, then drawn. The star key toggles a decorative falling-pieces
 * effect drawn on top of every screen.
 *
 * @author Tvc97
 * @forum  http://mbvn.tk
 */
public class Game extends GameCanvas implements Runnable {

    /** Screen ids for {@link #showScreen(int)}. */
    static final int INTRO = 0;
    static final int MENU = 1;
    static final int LEVEL_SELECT = 2;
    static final int BOARD = 3;
    static final int INFO = 4;
    static final int ACHIEVEMENTS = 5;

    private static final int FRAME_MILLIS = 25;
    private static final int LARGE_SCREEN_MIN_WIDTH = 240;
    private static final int BACKGROUND_COLOR = 0xffffff;

    final int width, height;
    final AchievementStore achievements;

    /** Both stone sprites side by side: X on the left, O on the right. */
    final Image pieces;
    /** "Back" soft-key label, drawn bottom-right on most screens. */
    final Image backIcon;

    final BoardScreen board;

    private final Graphics graphics;
    private final Screen[] screens = new Screen[6];
    private int currentScreen = INTRO;

    private final LeafDrop leafDrop;
    private boolean showLeaves;
    private long lastFrameTime;

    public Game() {
        super(false);
        setFullScreenMode(true);
        width = getWidth();
        height = getHeight();
        graphics = getGraphics();
        showLeaves = false;
        achievements = new AchievementStore();

        pieces = Resources.loadImage(isLargeScreen() ? "xo_big" : "xo_small");
        backIcon = Resources.loadImage("back");

        board = new BoardScreen(this);
        screens[BOARD] = board;
        screens[INTRO] = new IntroScreen(this);
        screens[MENU] = new MenuScreen(this);
        screens[INFO] = new InfoScreen(this);
        leafDrop = new LeafDrop(width, height);
        screens[LEVEL_SELECT] = new LevelSelectScreen(this);
        screens[ACHIEVEMENTS] = new AchievementScreen(this);
        lastFrameTime = 0;
        achievements.load();
    }

    /** True on screens wide enough for the large artwork. */
    boolean isLargeScreen() {
        return width >= LARGE_SCREEN_MIN_WIDTH;
    }

    /** Switches to the screen with the given id ({@link #MENU}, ...). */
    void showScreen(int screenId) {
        currentScreen = screenId;
    }

    /** Draws and shows a frame immediately, outside the normal frame loop. */
    void repaintNow() {
        draw(graphics);
        flushGraphics();
    }

    public void run() {
        while (true) {
            update();
            draw(graphics);
            flushGraphics();
            waitForNextFrame();
        }
    }

    private void update() {
        if (showLeaves) {
            leafDrop.update();
        }
        screens[currentScreen].update();
    }

    private void draw(Graphics g) {
        g.setColor(BACKGROUND_COLOR);
        g.fillRect(0, 0, width, height);
        screens[currentScreen].draw(g);
        if (showLeaves) {
            leafDrop.draw(g);
        }
    }

    protected void keyPressed(int key) {
        screens[currentScreen].keyPressed(key);
        if (key == Canvas.KEY_STAR) {
            showLeaves = !showLeaves;
        }
    }

    /**
     * Sleeps one frame. If the time since the previous call exceeded a frame,
     * the overrun is taken off this sleep so the frame rate catches up.
     */
    private void waitForNextFrame() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastFrameTime;
        long delay = FRAME_MILLIS;
        if (lastFrameTime != 0 && elapsed > FRAME_MILLIS) {
            delay = Math.max(0, 2 * FRAME_MILLIS - elapsed);
        }
        lastFrameTime = now;
        try {
            Thread.sleep(delay);
        } catch (Exception e) {
        }
    }
}
