package mbvn.tvc97;

/**
 *
 * @author Tvc97
 */
public class Leaf {

    int x, y, vx, vy, c, f;
    boolean i;

    public Leaf(int x, int y, int vx, int vy, int f) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.f = f;
        c = 0;
        f = 0;
        i = true;
    }
}
