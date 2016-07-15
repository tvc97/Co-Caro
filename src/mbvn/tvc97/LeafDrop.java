package mbvn.tvc97;

import javax.microedition.lcdui.game.*;
import javax.microedition.lcdui.*;
import java.util.*;

/**
 * @author Tvc97
 * @forum  http://mbvn.tk
 */
public class LeafDrop {

    Vector v;
    int w, h;
    Game game;
    Image im;
    Sprite s;

    public LeafDrop(Game game) {
        this.game = game;
        w = game.w;
        h = game.h;
        v = new Vector();
        for (int i = 0; i < 10; i++) {
            v.addElement(new Leaf(game.rand(w), game.rand(h, false), game.rand(2, false), 1 + game.rand(2), game.rand(4)));
        }
        im = game.load("cobay");
        s = new Sprite(im, 16, 10);
    }

    public void draw(Graphics g) {
        for (int i = v.size() - 1; i >= 0; i--) {
            Leaf l = (Leaf) v.elementAt(i);
            s.setFrame(l.f);
            s.setPosition(l.x, l.y);
            s.paint(g);
        }
    }

    public void update() {
        for (int i = v.size() - 1; i >= 0; i--) {
            Leaf l = (Leaf) v.elementAt(i);
            l.x += l.vx;
            l.y += l.vy;
            l.c++;
            if (l.c >= 6) {
                l.c = 0;
                l.f++;
                if (l.f > 3) {
                    l.f = 0;
                }
            }
            if (l.x < -16 || l.x > w || l.y > h) {
                l.x = game.rand(w);
                l.y = -game.rand(h);
                l.f = game.rand(4);
            }
        }
    }
}
