package mbvn.tvc97;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 * Table of wins and losses per difficulty level.
 *
 * @author Tvc97
 * @forum  http://mbvn.tk
 */
public class AchievementScreen implements Screen {

    private static final String TITLE = "Bảng thành tích"; // "Achievements"
    private static final String WINS_LABEL = "Thắng"; // "Wins"
    private static final String LOSSES_LABEL = "Thua"; // "Losses"
    /** Horizontal padding added to each column header's text width. */
    private static final int COLUMN_PADDING = 30;

    private static final int BACKGROUND_COLOR = 0xffffff;
    private static final int TITLE_BAR_COLOR = 0x6e9f;
    private static final int TITLE_COLOR = 0xffffff;
    private static final int TEXT_COLOR = 0x8ebf;
    private static final int DIVIDER_COLOR = 0xe0e0e0;

    private final Game game;
    private final Font font;
    private final Font boldFont;
    private final int top;
    /** Center x of the "wins" column. */
    private final int winsColumnX;
    /** Center x of the "losses" column (rightmost). */
    private final int lossesColumnX;

    public AchievementScreen(Game game) {
        this.game = game;
        font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        boldFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
        top = game.height / 2 - boldFont.getHeight() * 4;
        int winsWidth = boldFont.stringWidth(WINS_LABEL) + COLUMN_PADDING;
        int lossesWidth = boldFont.stringWidth(LOSSES_LABEL) + COLUMN_PADDING;
        lossesColumnX = game.width - winsWidth / 2;
        winsColumnX = game.width - winsWidth - lossesWidth / 2;
    }

    public void draw(Graphics g) {
        int width = game.width;
        int height = game.height;
        int boldHeight = boldFont.getHeight();
        int anchor = Graphics.HCENTER | Graphics.TOP;

        g.setColor(BACKGROUND_COLOR);
        g.fillRect(0, 0, width, height);
        g.setColor(TITLE_BAR_COLOR);
        g.setFont(boldFont);
        g.fillRect(0, 0, width, boldHeight + 8);
        g.setColor(TITLE_COLOR);
        g.drawString(TITLE, width / 2, 4, anchor);

        g.setColor(TEXT_COLOR);
        g.drawString(LOSSES_LABEL, lossesColumnX, top, anchor);
        g.drawString(WINS_LABEL, winsColumnX, top, anchor);

        AchievementStore stats = game.achievements;
        for (int level = 0; level < LevelSelectScreen.LEVEL_NAMES.length; level++) {
            int rowTop = top + (font.getHeight() + 8) * (level + 1);
            g.setColor(TEXT_COLOR);
            g.setFont(boldFont);
            g.drawString(LevelSelectScreen.LEVEL_NAMES[level], 4, rowTop, Graphics.TOP | Graphics.LEFT);
            g.setFont(font);
            g.drawString(String.valueOf(stats.getWins(level)), winsColumnX, rowTop, anchor);
            g.drawString(String.valueOf(stats.getLosses(level)), lossesColumnX, rowTop, anchor);
            g.setColor(DIVIDER_COLOR);
            int dividerY = rowTop + boldHeight + 4;
            g.drawLine(0, dividerY, width, dividerY);
        }
        g.drawImage(game.backIcon, width - 2, height - 2, Graphics.BOTTOM | Graphics.RIGHT);
    }

    public boolean update() {
        return false;
    }

    public void keyPressed(int key) {
        if (Keys.isBack(key)) {
            game.showScreen(Game.MENU);
        }
    }
}
