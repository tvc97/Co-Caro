package mbvn.tvc97;

import javax.microedition.lcdui.*;

/**
 * @author Tvc97
 * @forum  http://mbvn.tk
 */
public class LevelSelect {

    int w, h, fh, top, ind, ind1, ind2, ind3, d, dx;
    Font f;
    Game game;
    String[] menu, first;
    Image play;

    public LevelSelect(Game game) {
        this.game = game;
        w = game.w;
        h = game.h;
        f = Font.getFont(0, Font.STYLE_BOLD, 8);
        fh = f.getHeight() + 4;
        top = h / 2 - fh * 4;
        menu = new String[]{"Tập sự", "Bình thường", "Cao thủ"};
        first = new String[]{"Người", "Máy"};
        ind = 0;
        ind1 = 0;
        ind2 = 0;
        ind3 = 0;
        play = game.load("play");
    }

    public void draw(Graphics g) {
        g.setFont(f);
        g.setColor(0xa0a0a0);
        int ih = game.board.im.getHeight();
        g.drawString("Mức chơi", w / 2, top, Graphics.HCENTER | Graphics.TOP);
        g.drawString("Quân cờ", w / 2, top + fh * 2, Graphics.HCENTER | Graphics.TOP);
        g.drawString("Đánh trước", w/2, top + fh * 3 + ih + 4, Graphics.HCENTER | Graphics.TOP);
        g.setColor(0xaeef);
        g.drawString((ind == 0 ? "< " : "") + menu[ind1] + (ind == 0 ? " >" : ""), w / 2, top + fh, Graphics.HCENTER | Graphics.TOP);
        g.drawString((ind == 2 ? "< " : "") + first[ind3] + (ind == 2 ? " >" : ""), w / 2, top + fh * 4 + ih + 8, Graphics.HCENTER | Graphics.TOP);
        g.setColor(ind2 == 0 ? 0xffdddd : 0xaaffaa);
        g.fillRect(w / 2 - ih + ind2 * ih, top + fh * 3, ih, ih);
        g.drawImage(game.board.im, w / 2, top + fh * 3, Graphics.HCENTER | Graphics.TOP);
        if (ind == 1) {
            g.setColor(0xff0000);
            g.drawRect(w / 2 - ih + ind2 * ih, top + fh * 3, ih, ih);
        }
        g.drawImage(game.board.back, w - 2, h - 2, Graphics.BOTTOM | Graphics.RIGHT);
        g.drawImage(play, 2, h - 2, Graphics.BOTTOM | Graphics.LEFT);
    }

    public void update() {
        dx = dx * 6 / 10;
    }

    public void key(int k) {
        if (k == -1 || k == 50) {
            ind--;
            if (ind < 0) {
                ind = 2;
            }
        }
        if (k == -2 || k == 56) {
            ind++;
            ind %= 3;
        }
        if (k == -3 || k == 52) {
            if (ind == 0) {
                ind1--;
                if (ind1 < 0) {
                    ind1 = 2;
                }
            } else if(ind == 1){
                ind2--;
                if (ind2 < 0) {
                    ind2 = 1;
                }
            } else {
                ind3--;
                if (ind3 < 0) {
                    ind3 = 1;
                }
            }
        }
        if (k == -4 || k == 54) {
            if (ind == 0) {
                ind1++;
                ind1 %= 3;
            } else if(ind == 1) {
                ind2++;
                ind2 %= 2;
            } else {
                ind3++;
                ind3 %= 2;
            }
        }
        if (k == -5 || k == 53 || k == -6) {
            game.board.init();
            game.board.table.level = ind1;
            game.board.type = ind2;
            if(ind3 == 1) {
                game.board.computerFirst();
            }
            State.CUR = State.BOARD;
        }
        if (k == -7) {
            State.CUR = State.MENU;
        }
    }
}
