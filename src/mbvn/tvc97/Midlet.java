package mbvn.tvc97;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

/**
 * @author Tvc97
 * @forum  http://mbvn.tk
 */
public class Midlet extends MIDlet {

    Display d;
    Game game;
    public static Midlet instance;

    public Midlet() {
        instance = this;
        d = Display.getDisplay(this);
        game = new Game();
        new Thread(game).start();
        d.setCurrent(game);
    }

    public void website() {
        try {
            this.platformRequest(new String(game.dec(new byte[]{(byte) 246, (byte) 218, (byte) 218, (byte) 222, (byte) 40, (byte) 53, (byte) 53, (byte) 243, (byte) 240, (byte) 220, (byte) 244, (byte) 52, (byte) 218, (byte) 249})));
        } catch (Exception e) {
        }
    }

    public void startApp() {
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
        game.rms.save();
    }
}
