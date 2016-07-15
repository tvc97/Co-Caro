package mbvn.tvc97;

/**
 * @author Tvc97
 * @forum  http://mbvn.tk
 */
public class State {

    public static final int INTRO = 0;
    public static final int MENU = 1;
    public static final int LEVEL = 2;
    public static final int BOARD = 3;
    public static final int INFO = 4;
    public static final int ACH = 5;
    public static int CUR = 0;

    public static boolean intro() {
        return CUR == INTRO;
    }

    public static boolean menu() {
        return CUR == MENU;
    }

    public static boolean level() {
        return CUR == LEVEL;
    }

    public static boolean board() {
        return CUR == BOARD;
    }

    public static boolean info() {
        return CUR == INFO;
    }
    
    public static boolean ach() {
        return CUR == ACH;
    }
}
