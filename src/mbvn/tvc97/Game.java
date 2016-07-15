package mbvn.tvc97;

import javax.microedition.lcdui.game.*;
import javax.microedition.lcdui.*;
import java.util.*;

/**
 * @author Tvc97
 * @forum  http://mbvn.tk
 */
public class Game extends GameCanvas implements Runnable {

    int w, h;
    boolean showLeaf;
    Graphics g;
    Board board;
    Intro intro;
    Menu menu;
    Info info;
    LevelSelect lvs;
    LeafDrop ld;
    Random rd;
    RMS rms;
    Achievement ach;
    long lastDraw;

    public Game() {
        super(false);
        setFullScreenMode(true);
        w = getWidth();
        h = getHeight();
        g = getGraphics();
        showLeaf = false;
        rd = new Random();
        rms = new RMS();
        board = new Board(this);
        intro = new Intro(this);
        menu = new Menu(this);
        info = new Info(this);
        ld = new LeafDrop(this);
        lvs = new LevelSelect(this);
        ach = new Achievement(this);
        lastDraw = 0;
        rms.load();
    }

    public void draw(Graphics g) {
        g.setColor(0xffffff);
        g.fillRect(0, 0, w, h);
        if (State.intro()) {
            intro.draw(g);
        } else if (State.menu()) {
            menu.draw(g);
        } else if (State.board()) {
            board.draw(g);
        } else if (State.info()) {
            info.draw(g);
        } else if (State.level()) {
            lvs.draw(g);
        } else if (State.ach()) {
            ach.draw(g);
        }
        if (showLeaf) {
            ld.draw(g);
        }
    }

    public void update() {
        if (showLeaf) {
            ld.update();
        }
        if (State.intro()) {
            intro.update();
        } else if (State.menu()) {
            menu.update();
        } else if (State.board()) {
            board.update();
        } else if (State.info()) {
            info.update();
        }
    }

    protected void keyPressed(int k) {
        if (State.board()) {
            board.key(k);
        } else if (State.menu()) {
            menu.key(k);
        } else if (State.info()) {
            info.key(k);
        } else if (State.level()) {
            lvs.key(k);
        } else if (State.ach()) {
            ach.key(k);
        }
        if (k == 42) {
            showLeaf = !showLeaf;
        }
    }

    public void run() {
        while (true) {
            update();
            draw(g);
            flushGraphics();
            sleep();
        }
    }

    void sleep() {
        long delay = 0;
        long ft = now() - lastDraw;
        if (lastDraw != 0) {
            if (ft > 25) {
                delay = Math.max(0, 25 - (ft - 25));
            } else {
                delay = 25;
            }
        } else {
            delay = 25;
        }
        lastDraw = now();
        try {
            Thread.sleep(delay);
        } catch (Exception e) {
        }

    }

    int rand(int start, int limit, boolean abs) {
        if (limit <= start) {
            return limit;
        }
        int i = start + rd.nextInt() % (limit - start);
        return abs ? Math.abs(i) : i;
    }

    int rand(int limit, boolean abs) {
        return rand(0, limit, abs);
    }

    int rand(int start, int limit) {
        return rand(start, limit, true);
    }

    int rand(int limit) {
        return rand(0, limit, true);
    }

    byte[] dec(byte[] inp) {
        int l = inp.length;
        for (int i = 0; i < l; i++) {
            inp[i] = (byte) (((~(inp[i] - 0xBA)) ^ 0xAB) & 0xFF);
        }
        return inp;
    }

    long now() {
        return System.currentTimeMillis();
    }

    public Image load(String s) {
        Image im = null;
        try {
            im = Image.createImage("/res/" + s + ".png");
            Runtime.getRuntime().gc();
        } catch (Exception e) {
        }
        return im;
    }
}
