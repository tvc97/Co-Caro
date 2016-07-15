package mbvn.tvc97;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;
import java.io.*;

/**
 * @author Tvc97
 */
public class Intro {

    int w, h, v;
    long last;
    Image img;
    Game m;
    boolean dis;

    public Intro(Game m) {
        this.m = m;
        w = m.w;
        h = m.h;
        v = 0;
        try {
            InputStream is = getClass().getResourceAsStream("/res/logo_" + (w >= 240 ? "big" : "small"));
            byte[] b = new byte[is.available()];
            is.read(b);
            b = m.dec(b);
            img = Image.createImage(b, 0, b.length);
        } catch (Exception e) {
        }
        last = m.now();
        dis = false;
    }

    public void draw(Graphics g) {
        g.setColor(0xffffff);
        g.fillRect(0, 0, w, h);
        g.drawImage(img, w / 2, h / 2, Graphics.HCENTER | Graphics.VCENTER);
    }

    void update() {
        if (m.now() - last > 2500) {
            State.CUR = State.MENU;
        }
    }
}
