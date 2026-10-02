package mbvn.tvc97;

import java.util.Vector;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 * "About" screen. Types out the text from {@code /res/str} one character per
 * frame, word-wrapping to the screen width, with a blinking cursor.
 *
 * @author Tvc97
 * @forum  http://mbvn.tk
 */
public class InfoScreen implements Screen {

    private static final String TITLE = "Giới thiệu"; // "About"
    private static final String BACK_LABEL = "Trở về"; // "Back"

    private static final int HEADER_COLOR = 0xaeef;
    private static final int FOOTER_COLOR = 0xaeed;
    private static final int TITLE_COLOR = 0xffffff;
    private static final int TEXT_COLOR = 0x7ebf;

    /** Cursor blink cycle length in frames; the cursor shows for the last few. */
    private static final int BLINK_PERIOD = 11;
    private static final int BLINK_VISIBLE_FROM = 7;

    private final Game game;
    private final Font textFont;
    private final Font titleFont;
    private final int lineHeight;
    /** Height of the header bar; also used for the footer bar. */
    private final int barHeight;

    /** Lines already fully typed. */
    private final Vector lines = new Vector();
    /** Text not yet moved into {@link #lines}; starts with the line being typed. */
    private String remaining;
    /** How many characters of {@link #remaining} have been typed. */
    private int typedCount;
    /** The visible part of the line being typed. */
    private String typedLine;
    private int blinkTick;

    public InfoScreen(Game game) {
        this.game = game;
        textFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        titleFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
        lineHeight = textFont.getHeight() + 2;
        typedCount = 0;
        try {
            remaining = new String(Resources.readDecoded("/res/str"), "UTF-8");
        } catch (Exception e) {
        }
        barHeight = titleFont.getHeight() + 4;
        typedLine = "_";
        blinkTick = 0;
    }

    public void draw(Graphics g) {
        int width = game.width;
        int height = game.height;

        g.setColor(HEADER_COLOR);
        g.fillRect(0, 0, width, barHeight - 1);
        g.setColor(TITLE_COLOR);
        g.setFont(titleFont);
        g.drawString(TITLE, width / 2, 2, Graphics.HCENTER | Graphics.TOP);

        g.setFont(textFont);
        g.setColor(TEXT_COLOR);
        int lineCount = lines.size();
        for (int i = 0; i < lineCount; i++) {
            g.drawString((String) lines.elementAt(i), 1, lineTop(i), Graphics.TOP | Graphics.LEFT);
        }
        String cursor = blinkTick >= BLINK_VISIBLE_FROM ? "_" : "";
        g.drawString(typedLine + cursor, 1, lineTop(lineCount), Graphics.TOP | Graphics.LEFT);

        g.setColor(FOOTER_COLOR);
        g.fillRect(0, height - barHeight, width, barHeight);
        g.setFont(titleFont);
        g.setColor(TITLE_COLOR);
        g.drawString(BACK_LABEL, width - 1, height - 1, Graphics.BOTTOM | Graphics.RIGHT);
    }

    private int lineTop(int line) {
        return barHeight + line * lineHeight + 1;
    }

    /** Types one more character, ending the line at a newline or the screen edge. */
    public void update() {
        typedCount++;
        blinkTick = (blinkTick + 1) % BLINK_PERIOD;
        if (typedCount >= remaining.length()) {
            return;
        }
        typedLine = remaining.substring(0, typedCount);
        char next = remaining.charAt(typedCount);
        if (next == '\n') {
            finishLine(typedLine, typedCount, 0);
        } else if (textFont.stringWidth(typedLine) >= game.width) {
            if (next == ' ') {
                finishLine(typedLine, typedCount, 0);
            } else {
                // Wrap before the word that crossed the edge.
                int lastSpace = typedLine.lastIndexOf(' ');
                finishLine(typedLine.substring(0, lastSpace), lastSpace, typedCount - lastSpace);
            }
        }
    }

    /**
     * Moves {@code line} into {@link #lines} and continues typing from
     * {@code remaining[cutIndex]}, with {@code nextTypedCount} characters of
     * the new line already shown.
     */
    private void finishLine(String line, int cutIndex, int nextTypedCount) {
        lines.addElement(line.trim());
        remaining = remaining.substring(cutIndex).trim();
        typedCount = nextTypedCount;
        typedLine = "";
    }

    public void keyPressed(int key) {
        if (Keys.isBack(key)) {
            game.showScreen(Game.MENU);
        }
    }
}
