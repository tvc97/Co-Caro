package mbvn.tvc97;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Main menu. The selected item is highlighted and framed by an X and an O
 * that slide in from the screen edges whenever the selection changes.
 *
 * @author Tvc97
 * @forum  http://mbvn.tk
 */
public class MenuScreen implements Screen {

    private static final String[] ITEMS = {
        "Chơi mới", // New game
        "Thành tích", // Achievements
        "Giới thiệu", // About
        "Diễn đàn", // Forum
        "Thoát" // Exit
    };
    private static final int ITEM_NEW_GAME = 0;
    private static final int ITEM_ACHIEVEMENTS = 1;
    private static final int ITEM_ABOUT = 2;
    private static final int ITEM_FORUM = 3;
    private static final int ITEM_EXIT = 4;

    private static final String HINT = "Phím * bật/tắt hiệu ứng"; // "* toggles effects"

    private static final int TEXT_COLOR = 0x8ecf;
    private static final int HIGHLIGHT_COLOR = 0xeeeeee;
    private static final int HINT_COLOR = 0xd0d0d0;
    /** Gap in pixels between the selected label and the X/O icons. */
    private static final int ICON_GAP = 10;

    private final Game game;
    private final Font selectedFont;
    private final Font itemFont;
    /** Row height; equals the stone icon height. */
    private final int itemHeight;
    private final int top;

    private int selected;
    /** Extra distance of the X/O icons from their resting place; decays each frame. */
    private int slideOffset;

    public MenuScreen(Game game) {
        this.game = game;
        selectedFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
        itemFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        itemHeight = game.pieces.getHeight();
        top = game.height / 2;
        selected = 0;
        slideOffset = 0;
    }

    public void draw(Graphics g) {
        int centerX = game.width / 2;
        for (int i = 0; i < ITEMS.length; i++) {
            int rowTop = top + i * itemHeight;
            if (i == selected) {
                g.setFont(selectedFont);
                drawSelectionFrame(g, ITEMS[i], rowTop);
            } else {
                g.setFont(itemFont);
            }
            g.setColor(TEXT_COLOR);
            g.drawString(ITEMS[i], centerX, rowTop + 2, Graphics.HCENTER | Graphics.TOP);
        }
        g.setFont(itemFont);
        g.setColor(HINT_COLOR);
        g.drawString(HINT, centerX, 0, Graphics.TOP | Graphics.HCENTER);
    }

    /** Draws the highlight bar and the X/O icons around the selected item. */
    private void drawSelectionFrame(Graphics g, String label, int rowTop) {
        Image icons = game.pieces;
        int iconSize = icons.getHeight();
        int centerX = game.width / 2;
        int labelWidth = selectedFont.stringWidth(label);
        int leftIconRight = centerX - labelWidth / 2 - ICON_GAP - slideOffset;
        int rightIconLeft = centerX + labelWidth / 2 + ICON_GAP + slideOffset;
        int rowMiddle = rowTop + itemHeight / 2;

        int barLeft = leftIconRight - iconSize;
        g.setColor(HIGHLIGHT_COLOR);
        g.fillRoundRect(barLeft, rowTop, (centerX - barLeft) * 2, itemHeight, 4, 4);
        g.drawRegion(icons, 0, 0, iconSize, iconSize, 0,
                leftIconRight, rowMiddle, Graphics.RIGHT | Graphics.VCENTER);
        g.drawRegion(icons, iconSize, 0, iconSize, iconSize, 0,
                rightIconLeft, rowMiddle, Graphics.LEFT | Graphics.VCENTER);
    }

    public boolean update() {
        int previous = slideOffset;
        slideOffset = slideOffset * 6 / 10;
        return slideOffset != previous;
    }

    public void keyPressed(int key) {
        if (Keys.isUp(key)) {
            select((selected + ITEMS.length - 1) % ITEMS.length);
        }
        if (Keys.isDown(key)) {
            select((selected + 1) % ITEMS.length);
        }
        if (Keys.isSelect(key)) {
            activate(selected);
        }
    }

    /** Selects an item and starts the icons sliding in from the screen edges. */
    private void select(int item) {
        selected = item;
        slideOffset = game.width / 2 - selectedFont.stringWidth(ITEMS[item]) / 2 - 4;
    }

    private void activate(int item) {
        switch (item) {
            case ITEM_NEW_GAME:
                game.showScreen(Game.LEVEL_SELECT);
                break;
            case ITEM_ACHIEVEMENTS:
                game.showScreen(Game.ACHIEVEMENTS);
                break;
            case ITEM_ABOUT:
                game.showScreen(Game.INFO);
                break;
            case ITEM_FORUM:
                Midlet.instance.openForum();
                break;
            case ITEM_EXIT:
                Midlet.instance.exit();
                break;
        }
    }
}
