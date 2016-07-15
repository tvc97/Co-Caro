package mbvn.tvc97;

import javax.microedition.lcdui.*;

/**
 * @author Tvc97
 * @forum  http://mbvn.tk
 */
public class Menu {

    int w, h, fh, top, ind, d, dx;
    Font f, sm;
    Game game;
    String[] menu;

    public Menu(Game game) {
        this.game = game;
        w = game.w;
        h = game.h;
        f = Font.getFont(0, Font.STYLE_BOLD, 8);
        sm = Font.getFont(0, 0, 8);
        fh = game.board.im.getHeight();
        d = fh / 2;
        menu = new String[]{"Chơi mới", "Thành tích", "Giới thiệu", "Diễn đàn", "Thoát"};
        top = h / 2;
        ind = 0;
        dx = 0;
    }

    public void draw(Graphics g) {
        for (int i = 0; i < 5; i++) {
            if (i == ind) {
                g.setFont(f);
                int sw = f.stringWidth(menu[i]);
                int lc = w / 2 - sw / 2 - 10;
                int rc = w / 2 + sw / 2 + 10;
                Image im = game.board.im;
                int ih = im.getHeight();
                g.setColor(0xeeeeee);
                g.fillRoundRect(lc - dx - ih, top + i * fh, (w / 2 - (lc - dx - ih)) * 2, fh, 4, 4);
                g.drawRegion(im, 0, 0, ih, ih, 0, lc - dx, top + d + i * fh, Graphics.RIGHT | Graphics.VCENTER);
                g.drawRegion(im, ih, 0, ih, ih, 0, rc + dx, top + d + i * fh, Graphics.LEFT | Graphics.VCENTER);
                g.setColor(0x8ecf);
                g.drawString(menu[i], w / 2, top + i * fh + 2, Graphics.HCENTER | Graphics.TOP);
            } else {
                g.setFont(sm);
                g.setColor(0x8ecf);
                g.drawString(menu[i], w / 2, top + i * fh + 2, Graphics.HCENTER | Graphics.TOP);
            }
        }
        g.setFont(sm);
        g.setColor(0xd0d0d0);
        g.drawString("Phím * bật/tắt hiệu ứng", w / 2, 0, Graphics.TOP | Graphics.HCENTER);
    }

    public void update() {
        dx = dx * 6 / 10;
    }

    public void key(int k) {
        if (k == -1 || k == 50) {
            ind--;
            if (ind < 0) {
                ind = 4;
            }
            int sw = f.stringWidth(menu[ind]);
            dx = w / 2 - sw / 2 - 4;
        }
        if (k == -2 || k == 56) {
            ind++;
            ind %= 5;
            int sw = f.stringWidth(menu[ind]);
            dx = w / 2 - sw / 2 - 4;
        }
        if (k == -5 || k == 53) {
            if (ind == 0) {
                State.CUR = State.LEVEL;
            } else if (ind == 2) {
                State.CUR = State.INFO;
            } else if (ind == 3) {
                Midlet.instance.website();
            } else if (ind == 4) {
                Midlet.instance.destroyApp(true);
                Midlet.instance.notifyDestroyed();
            } else if (ind == 1) {
                State.CUR = State.ACH;
            }
        }
    }
}
