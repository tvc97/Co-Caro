package mbvn.tvc97;

import javax.microedition.lcdui.*;

/**
 * @author Tvc97
 * @forum  http://mbvn.tk
 */
public class Achievement {

    int w, h, fh, fbh, top;
    Font f, fb;
    Game game;
    String[] menu;
    int[] sw;

    public Achievement(Game game) {
        this.game = game;
        w = game.w;
        h = game.h;
        f = Font.getFont(0, 0, 8);
        fb = Font.getFont(0, 1, 8);
        fh = f.getHeight();
        fbh = fb.getHeight();
        top = h / 2 - fbh * 4;
        menu = new String[]{"Thắng", "Thua", "Tập sự", "Bình thường", "Cao thủ"};
        sw = new int[5];
        for (int i = 0; i < 5; i++) {
            sw[i] = fb.stringWidth(menu[i]) + 30;
        }
    }

    public void draw(Graphics g) {
        g.setColor(0xffffff);
        g.fillRect(0, 0, w, h);
        g.setColor(0x6e9f);
        g.setFont(fb);
        g.fillRect(0, 0, w, fbh + 8);
        g.setColor(0xffffff);
        g.drawString("Bảng thành tích", w / 2, 4, Graphics.TOP | Graphics.HCENTER);

        g.setColor(0x8ebf);
        int r1 = w - sw[0] / 2;
        int r2 = w - sw[0] - sw[1] / 2;
        g.drawString(menu[1], r1, top, Graphics.HCENTER | Graphics.TOP);
        g.drawString(menu[0], r2, top, Graphics.HCENTER | Graphics.TOP);

        for (int i = 0; i < 3; i++) {
            g.setColor(0x8ebf);
            g.setFont(fb);
            int top_m = top + (fh + 8) * (i + 1);
            g.drawString(menu[i + 2], 4, top_m, Graphics.TOP | Graphics.LEFT);
            g.setFont(f);
            g.drawString("" + game.rms.achieve[i][0] + "", r2, top_m, Graphics.HCENTER | Graphics.TOP);
            g.drawString("" + game.rms.achieve[i][1] + "", r1, top_m, Graphics.HCENTER | Graphics.TOP);
            g.setColor(0xe0e0e0);
            g.drawLine(0, top_m + fbh + 4, w, top_m + fbh + 4);
        }
        g.drawImage(game.board.back, w - 2, h - 2, Graphics.BOTTOM | Graphics.RIGHT);
    }

    public void update() {
    }

    public void key(int k) {
        if (k == -7) {
            State.CUR = State.MENU;
        }
    }
}
