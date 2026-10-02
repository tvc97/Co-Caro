package mbvn.tvc97;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Splash screen: shows the logo for a moment, then opens the main menu.
 *
 * @author Tvc97
 */
public class IntroScreen implements Screen {

    private static final long DISPLAY_MILLIS = 2500;
    private static final int BACKGROUND_COLOR = 0xffffff;

    private final Game game;
    private final long startTime;
    private Image logo;

    public IntroScreen(Game game) {
        this.game = game;
        try {
            byte[] png = Resources.readDecoded(
                    "/res/logo_" + (game.isLargeScreen() ? "big" : "small"));
            logo = Image.createImage(png, 0, png.length);
        } catch (Exception e) {
        }
        startTime = System.currentTimeMillis();
    }

    public void draw(Graphics g) {
        g.setColor(BACKGROUND_COLOR);
        g.fillRect(0, 0, game.width, game.height);
        g.drawImage(logo, game.width / 2, game.height / 2, Graphics.HCENTER | Graphics.VCENTER);
    }

    public boolean update() {
        if (System.currentTimeMillis() - startTime > DISPLAY_MILLIS) {
            game.showScreen(Game.MENU);
        }
        return false;
    }

    public void keyPressed(int key) {
    }
}
