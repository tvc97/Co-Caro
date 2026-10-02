package mbvn.tvc97;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;

/**
 * Application entry point. Creates the {@link Game} canvas, starts its frame
 * loop and saves statistics on exit.
 *
 * @author Tvc97
 * @forum  http://mbvn.tk
 */
public class Midlet extends MIDlet {

    private static final String FORUM_URL = "http://mbvn.tk";

    /** The running MIDlet, used by the menu to open the forum or quit. */
    public static Midlet instance;

    private final Game game;

    public Midlet() {
        instance = this;
        Display display = Display.getDisplay(this);
        game = new Game();
        new Thread(game).start();
        display.setCurrent(game);
    }

    /** Asks the phone to open the forum in its browser. */
    public void openForum() {
        try {
            platformRequest(FORUM_URL);
        } catch (Exception e) {
        }
    }

    /** Saves statistics and closes the application. */
    public void exit() {
        destroyApp(true);
        notifyDestroyed();
    }

    public void startApp() {
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
        game.achievements.save();
    }
}
