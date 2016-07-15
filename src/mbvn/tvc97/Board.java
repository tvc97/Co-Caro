package mbvn.tvc97;

import javax.microedition.lcdui.game.*;
import javax.microedition.lcdui.*;
import java.util.*;

/**
 * @author Tvc97
 * @forum  http://mbvn.tk
 */
public class Board {

    Game game;
    int w, h, pw, bw, x, y, xb, yb, lx, ly, dx, dy, c, c2, type;
    Table table;
    Image im, comp, huma, back, undo, trans;
    boolean gameover, win, hum;
    Font big;
    Vector step;

    public Board(Game game) {
        this.game = game;
        w = game.w;
        h = game.h;
        type = 0;
        big = Font.getFont(0, Font.STYLE_BOLD, Font.SIZE_LARGE);

        try {
            im = Image.createImage("/res/xo_" + (w < 240 ? "small.png" : "big.png"));
            comp = game.load("com");
            huma = game.load("hum");
            back = game.load("back");
            undo = game.load("undo");
            trans = game.load("opacity");
        } catch (Exception e) {
        }

        pw = im.getWidth() / 2;
        bw = pw * 30;

        step = new Vector();
        table = new Table();
        init();
    }

    public void init() {
        table.cell = new int[30][30];
        table.score = new int[30][30];
        table.score1 = new int[30][30];
        table.level = 0;
        for (int i = 0; i < 30; i++) {
            for (int j = 0; j < 30; j++) {
                table.cell[i][j] = 0;
            }
        }
        step.setSize(0);
        table.n = 30;
        table.m = 30;
        x = 15;
        y = 15;
        dx = 0;
        dy = 0;
        c = 0;
        lx = -(bw - w);
        ly = -(bw - h);
        win = false;
        gameover = false;
        hum = true;
        c2 = 30;
        Runtime.getRuntime().gc();
    }

    public void draw(Graphics g) {

        g.setClip(0, 0, w, h);

        int tmpxb = x * pw;
        int tmpyb = y * pw;
        xb = w / 2 - tmpxb - pw / 2;
        yb = h / 2 - tmpyb - pw / 2;
        if (xb > 0) {
            xb = 0;
            dx = 0;
        }
        if (yb > 0) {
            dy = 0;
            yb = 0;
        }
        if (xb < lx) {
            xb = lx;
        }
        if (yb < ly) {
            yb = ly;
        }

        g.setColor(type == 0 ? 0xffcccc : 0xccffcc);
        g.fillRect(dx + xb + x * pw, dy + yb + y * pw, pw, pw);

        g.setColor(0xe0e0e0);
        for (int i = 0; i < 31; i++) {
            g.drawLine(dx + xb, dy + yb + i * pw, dx + xb + bw, dy + yb + i * pw);
        }
        for (int i = 0; i < 31; i++) {
            g.drawLine(dx + xb + i * pw, dy + yb, dx + xb + i * pw, dy + yb + bw);
        }
        for (int i = 0; i < 30; i++) {
            for (int j = 0; j < 30; j++) {
                if (table.cell[i][j] != 0) {
                    int cx = table.cell[i][j] == 1 ? (type == 0 ? 0 : pw) : (type == 0 ? pw : 0);
                    g.drawRegion(im, cx, 0, pw, pw, Sprite.TRANS_NONE, dx + xb + i * pw, dy + yb + j * pw, 20);
                }
            }
        }
        if (hum) {
            g.drawImage(huma, 2, 2, 20);
        } else {
            g.drawImage(comp, 2, 2, 20);
        }

        if (gameover) {
            int sxline = xb + table.wx * pw + pw / 2;
            int syline = yb + table.wy * pw + pw / 2;
            int txline = xb + pw / 2 + table.wx * pw + table.wdx * 4 * pw;
            int tyline = yb + pw / 2 + table.wy * pw + table.wdy * 4 * pw;
            g.setColor(0xff0000);
            g.drawLine(dx + sxline, dy + syline, dx + txline, dy + tyline);

            g.setFont(big);
            int nx = w / 20 + 1;
            int ny = h / 20 + 1;
            for (int i = 0; i < ny; i++) {
                for (int j = 0; j < nx; j++) {
                    g.drawImage(trans, j * 20, i * 20, 0);
                }
            }
            g.setColor(type == 0 ? 0xff0000 : 0x00ff00);
            g.drawString(win ? "BẠN ĐÃ THẮNG :)" : "BẠN ĐÃ THUA :(", w / 2, h / 2, Graphics.HCENTER | Graphics.BASELINE);
        }
        g.drawImage(back, w - 2, h - 2, Graphics.BOTTOM | Graphics.RIGHT);
        g.drawImage(undo, 2, h - 2, Graphics.BOTTOM | Graphics.LEFT);

    }

    public void update() {
        dx = dx * 70 / 100;
        dy = dy * 70 / 100;
    }

    public void key(int k) {
        if (!gameover) {
            if (k == -1 || k == 50 || k == 49 || k == 51) {
                y = Math.max(0, y - 1);
                addTransition(0, -pw);
            }
            if (k == -2 || k == 56 || k == 55 || k == 57) {
                y = Math.min(29, y + 1);
                addTransition(0, pw);
            }
            if (k == -3 || k == 52 || k == 49 || k == 55) {
                x = Math.max(0, x - 1);
                addTransition(-pw, 0);
            }
            if (k == -4 || k == 54 || k == 51 || k == 57) {
                x = Math.min(29, x + 1);
                addTransition(pw, 0);
            }
        }
        if (k == -5 || k == 53) {
            if (table.cell[x][y] == 0) {
                table.cell[x][y] = 1;
                if (table.checkwin()) {
                    gameover = true;
                    win = true;
                    game.rms.update(table.level, true);
                    step.addElement(new Step(x, y, 100, 100));
                } else {
                    hum = false;
                    game.draw(game.g);
                    game.flushGraphics();
                    try {
                        Thread.sleep(300);
                    } catch (Exception e) {
                    }
                    table.findSolution();
                    table.cell[table.resX][table.resY] = 2;
                    step.addElement(new Step(x, y, table.resX, table.resY));
                    dx = (table.resX - x) * pw;
                    dy = (table.resY - y) * pw;
                    x = table.resX;
                    y = table.resY;
                    if (table.checkwin()) {
                        gameover = true;
                        win = false;
                        game.rms.update(table.level, false);
                    }
                    hum = true;
                }
            }
        }
        if (k == -6) {
            undo();
        }
        if (k == -7) {
            State.CUR = State.MENU;
        }
    }
    
    public void computerFirst() {
        table.cell[15][15] = 2;
    }

    void undo() {
        int size = step.size();
        Step s, s2 = null;
        if (size > 0) {
            s = (Step) step.elementAt(size - 1);
            if (size > 1) {
                s2 = (Step) step.elementAt(size - 2);
            }
            table.cell[s.px][s.py] = 0;
            if (s.cx != 100 && s.cy != 100) {
                table.cell[s.cx][s.cy] = 0;
            }

            step.removeElementAt(size - 1);
            if (gameover) {
                gameover = false;
            }
            if (size > 1) {
                dx = (s2.cx - x) * pw;
                dy = (s2.cy - y) * pw;
                x = s2.cx;
                y = s2.cy;
            }
        }

    }

    void addTransition(int dx, int dy) {
        if (dx == 0) {
            if (yb > ly && yb < 0) {
                this.dy += dy;
            }
        }
        if (dy == 0) {
            if (xb > lx && xb < 0) {
                this.dx += dx;
            }
        }
    }
}
