package mbvn.tvc97;

import java.util.Random;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.Sprite;

/**
 * Decorative effect: animated sprites drifting down over the screen.
 * A sprite that leaves the screen respawns above it at a random column.
 * Toggled with the star key (see {@link Game}).
 *
 * @author Tvc97
 * @forum  http://mbvn.tk
 */
public class LeafDrop {

    private static final int LEAF_COUNT = 10;
    private static final int SPRITE_WIDTH = 16;
    private static final int SPRITE_HEIGHT = 10;
    private static final int FRAME_COUNT = 4;
    /** Game frames each animation frame is shown for. */
    private static final int TICKS_PER_FRAME = 6;

    private final int width, height;
    private final Random random = new Random();
    private final Vector leaves = new Vector();
    private final Sprite sprite;

    public LeafDrop(int width, int height) {
        this.width = width;
        this.height = height;
        for (int i = 0; i < LEAF_COUNT; i++) {
            leaves.addElement(new Leaf(
                    randomBelow(width),
                    randomSigned(height),
                    randomSigned(2),
                    1 + randomBelow(2),
                    randomBelow(FRAME_COUNT)));
        }
        sprite = new Sprite(Resources.loadImage("cobay"), SPRITE_WIDTH, SPRITE_HEIGHT);
    }

    public void draw(Graphics g) {
        for (int i = leaves.size() - 1; i >= 0; i--) {
            Leaf leaf = (Leaf) leaves.elementAt(i);
            sprite.setFrame(leaf.frame);
            sprite.setPosition(leaf.x, leaf.y);
            sprite.paint(g);
        }
    }

    public void update() {
        for (int i = leaves.size() - 1; i >= 0; i--) {
            Leaf leaf = (Leaf) leaves.elementAt(i);
            leaf.x += leaf.vx;
            leaf.y += leaf.vy;

            leaf.frameTicks++;
            if (leaf.frameTicks >= TICKS_PER_FRAME) {
                leaf.frameTicks = 0;
                leaf.frame = (leaf.frame + 1) % FRAME_COUNT;
            }

            boolean offScreen = leaf.x < -SPRITE_WIDTH || leaf.x > width || leaf.y > height;
            if (offScreen) {
                leaf.x = randomBelow(width);
                leaf.y = -randomBelow(height);
                leaf.frame = randomBelow(FRAME_COUNT);
            }
        }
    }

    /** Random value in {@code [0, limit)}; returns {@code limit} if it is not positive. */
    private int randomBelow(int limit) {
        return limit <= 0 ? limit : Math.abs(random.nextInt() % limit);
    }

    /** Random value in {@code (-limit, limit)}; returns {@code limit} if it is not positive. */
    private int randomSigned(int limit) {
        return limit <= 0 ? limit : random.nextInt() % limit;
    }
}
