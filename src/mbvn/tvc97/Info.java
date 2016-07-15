package mbvn.tvc97;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;
import java.io.*;
import java.util.*;

/**
 * @author Tvc97
 * @forum  http://mbvn.tk
 */
public class Info {

    int w, h, sh, lh, ind, top, co;
    Game m;
    Font s, l;
    String str, cur;
    StringBuffer sb;
    Vector v;

    public Info(Game m) {
        this.m = m;
        w = m.w;
        h = m.h;
        s = Font.getFont(0, 0, 8);
        l = Font.getFont(0, 1, 8);
        sh = s.getHeight() + 2;
        lh = l.getHeight();
        ind = 0;
        try {
            InputStream is = getClass().getResourceAsStream("/res/str");
            byte[] b = new byte[is.available()];
            is.read(b);
            str = new String(m.dec(b), "UTF-8");
        } catch (Exception e) {
        }
        top = lh + 4;
        cur = "_";
        v = new Vector();
        co = 0;
    }

    public void draw(Graphics g) {
        g.setColor(0xaeef);
        g.fillRect(0, 0, w, top - 1);
        g.setColor(0xffffff);
        g.setFont(l);
        g.drawString("Giới thiệu", w / 2, 2, Graphics.HCENTER | Graphics.TOP);
        g.setFont(s);
        g.setColor(0x7ebf);
        int size = v.size();
        for (int i = 0; i < size; i++) {
            g.drawString((String) v.elementAt(i), 1, top + i * sh + 1, 20);
        }
        g.drawString(cur + (co > 6 ? "_" : ""), 1, top + size * sh + 1, 20);
        g.setColor(0xaeed);
        g.fillRect(0, h - top, w, top);
        g.setFont(l);
        g.setColor(0xffffff);
        g.drawString("Trở về", w - 1, h - 1, Graphics.BOTTOM | Graphics.RIGHT);
    }

    void update() {
        ind++;
        co++;
        co %= 11;
        if (ind < str.length()) {
            cur = str.substring(0, ind);
            if (str.charAt(ind) == '\n') {
                v.addElement(cur.trim());
                str = str.substring(ind).trim();
                ind = 0;
                cur = "";
            } else if (s.stringWidth(cur) >= w) {
                if (str.charAt(ind) == ' ' || str.charAt(ind) == '\n') {
                    v.addElement(cur.trim());
                    str = str.substring(ind).trim();
                    ind = 0;
                    cur = "";
                } else {
                    int last = cur.lastIndexOf(' ');
                    cur = cur.substring(0, last);
                    str = str.substring(last).trim();
                    v.addElement(cur.trim());
                    ind = ind - last;
                    cur = "";
                }
            }
        }
    }

    void key(int k) {
        if (k == -7) {
            State.CUR = State.MENU;
        }
    }
}
