package mbvn.tvc97;

/**
 * One falling sprite of the {@link LeafDrop} effect.
 *
 * @author Tvc97
 */
class Leaf {

    /** Position in pixels. */
    int x, y;
    /** Velocity in pixels per frame. */
    int vx, vy;
    /** Current animation frame of the sprite sheet. */
    int frame;
    /** Game frames shown since the animation frame last changed. */
    int frameTicks;

    Leaf(int x, int y, int vx, int vy, int frame) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.frame = frame;
        frameTicks = 0;
    }
}
