package mbvn.tvc97;

import javax.microedition.lcdui.Canvas;

/**
 * Key codes and key-group helpers shared by all screens.
 *
 * <p>Navigation keys accept both the device's raw joystick/soft-key codes
 * (the negative values used by Nokia and Sony Ericsson handsets) and the
 * equivalent numeric keypad keys (2/4/6/8 to move, 5 to select).
 *
 * @author Tvc97
 */
final class Keys {

    static final int UP = -1;
    static final int DOWN = -2;
    static final int LEFT = -3;
    static final int RIGHT = -4;
    static final int FIRE = -5;
    static final int SOFT_LEFT = -6;
    static final int SOFT_RIGHT = -7;

    private Keys() {
    }

    static boolean isUp(int key) {
        return key == UP || key == Canvas.KEY_NUM2;
    }

    static boolean isDown(int key) {
        return key == DOWN || key == Canvas.KEY_NUM8;
    }

    static boolean isLeft(int key) {
        return key == LEFT || key == Canvas.KEY_NUM4;
    }

    static boolean isRight(int key) {
        return key == RIGHT || key == Canvas.KEY_NUM6;
    }

    static boolean isSelect(int key) {
        return key == FIRE || key == Canvas.KEY_NUM5;
    }

    /** The right soft key always means "back to the main menu". */
    static boolean isBack(int key) {
        return key == SOFT_RIGHT;
    }
}
