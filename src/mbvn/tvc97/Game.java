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
 * updated, and redrawn only if something changed: an animation step, a key
 * press, a screen switch, or the canvas becoming visible again. Static
 * screens therefore cost almost no CPU or battery. The star key toggles a
 * decorative falling-pieces effect drawn on top of every screen.
 *
 * <p>On touch screens a touch that stays within {@value #TAP_SLOP} pixels of
 * where it started is a tap ({@link Screen#tapped}); anything further is a
 * drag ({@link Screen#dragged}).
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
    /** How far a touch may move and still count as a tap. */
    private static final int TAP_SLOP = 8;
    /** Extra margin around corner icons that still counts as touching them. */
    private static final int ICON_TOUCH_MARGIN = 8;

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
    /** Last touch position, and whether the current touch became a drag. */
    private int touchX, touchY;
    private boolean dragging;

    /** Set from the UI thread when the next frame must be drawn. */
    private volatile boolean redrawRequested = true;

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

    /** Turns the falling-pieces effect on or off. */
    void toggleLeaves() {
        showLeaves = !showLeaves;
        redrawRequested = true;
    }

    /**
     * True if (x, y) touches an icon drawn in the bottom-left corner
     * (the left soft-key action: undo, play).
     */
    boolean touchesBottomLeft(Image icon, int x, int y) {
        return x <= 2 + icon.getWidth() + ICON_TOUCH_MARGIN
                && y >= height - 2 - icon.getHeight() - ICON_TOUCH_MARGIN;
    }

    /** True if (x, y) touches the "back" icon in the bottom-right corner. */
    boolean touchesBackIcon(int x, int y) {
        return x >= width - 2 - backIcon.getWidth() - ICON_TOUCH_MARGIN
                && y >= height - 2 - backIcon.getHeight() - ICON_TOUCH_MARGIN;
    }

    /** Switches to the screen with the given id ({@link #MENU}, ...). */
    void showScreen(int screenId) {
        currentScreen = screenId;
        redrawRequested = true;
    }

    public void run() {
        while (true) {
            if (update()) {
                draw(graphics);
                flushGraphics();
            }
            waitForNextFrame();
        }
    }

    /**
     * Advances one frame.
     *
     * @return true if the frame must be redrawn
     */
    private boolean update() {
        boolean changed = false;
        if (showLeaves) {
            leafDrop.update();
            changed = true;
        }
        if (screens[currentScreen].update()) {
            changed = true;
        }
        // Clear the request before drawing so a key pressed during the draw
        // still triggers the next frame.
        if (redrawRequested) {
            redrawRequested = false;
            changed = true;
        }
        return changed;
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
            toggleLeaves();
        }
        redrawRequested = true;
    }

    protected void pointerPressed(int x, int y) {
        touchX = x;
        touchY = y;
        dragging = false;
    }

    protected void pointerDragged(int x, int y) {
        if (!dragging && Math.abs(x - touchX) <= TAP_SLOP && Math.abs(y - touchY) <= TAP_SLOP) {
            return;
        }
        dragging = true;
        screens[currentScreen].dragged(x - touchX, y - touchY);
        touchX = x;
        touchY = y;
        redrawRequested = true;
    }

    protected void pointerReleased(int x, int y) {
        if (!dragging) {
            screens[currentScreen].tapped(x, y);
            redrawRequested = true;
        }
    }

    /** The screen may have been overwritten while hidden (a call, a dialog). */
    protected void showNotify() {
        redrawRequested = true;
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
