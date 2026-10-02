package mbvn.tvc97;

import javax.microedition.lcdui.Graphics;

/**
 * One full-screen view of the game. {@link Game} forwards the frame loop and
 * key events to whichever screen is currently shown.
 *
 * @author Tvc97
 */
interface Screen {

    /** Paints the screen. Called once per frame after {@link #update()}. */
    void draw(Graphics g);

    /**
     * Advances animations by one frame (about 25 ms).
     *
     * @return true if the screen looks different and must be redrawn
     */
    boolean update();

    /** Handles a key press while this screen is shown. */
    void keyPressed(int key);
}
