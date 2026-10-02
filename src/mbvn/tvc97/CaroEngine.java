package mbvn.tvc97;

import java.util.Date;
import java.util.Random;

/**
 * Board model, win detection and computer opponent for Caro (Gomoku).
 *
 * <p>The board is a {@value #SIZE}x{@value #SIZE} grid indexed as
 * {@code cells[x][y]}. Five stones in a row (horizontal, vertical or
 * diagonal) win.
 *
 * <h3>How the computer picks a move</h3>
 * Every run of five cells ("window") that contains stones of only one player
 * adds a weight to each of its cells: 1, 10, 100, 1000 or 10000 for 1-5
 * stones, doubled when the cells just outside the window do not hold that
 * player's stone. Summing these weights once for the human's stones
 * (defense) and once for the computer's stones (attack) gives two heat maps;
 * the level decides how they are combined (see {@link #findBestMove()}).
 *
 * @author    Tvc97
 * @algorithm JCaro - BK02 Team (http://bk02.sourceforge.net/jcaro/)
 */
public class CaroEngine {

    /** Board width and height in cells. */
    static final int SIZE = 30;
    /** Stones in a row needed to win. */
    static final int WIN_LENGTH = 5;

    /** Cell values. */
    static final int EMPTY = 0;
    static final int HUMAN = 1;
    static final int COMPUTER = 2;

    /** Difficulty levels, in menu order. Also index the saved statistics. */
    static final int LEVEL_BEGINNER = 0;
    static final int LEVEL_NORMAL = 1;
    static final int LEVEL_EXPERT = 2;

    /** Stone owner per cell: {@link #EMPTY}, {@link #HUMAN} or {@link #COMPUTER}. */
    int[][] cells;
    int level;

    /** Result of {@link #findBestMove()}. */
    int bestX, bestY;

    /** Start cell and direction of the winning line found by {@link #checkWin()}. */
    int winX, winY, winDx, winDy;

    /** Scratch heat map filled by {@link #evaluate(int)}. */
    private int[][] scores;
    /** Copy of the heat map for the human's stones (cells the computer should block). */
    private int[][] defenseScores;
    private final Random random;

    public CaroEngine() {
        random = new Random(new Date().getTime());
    }

    /** Clears the board for a new game. */
    void reset() {
        cells = new int[SIZE][SIZE];
        scores = new int[SIZE][SIZE];
        defenseScores = new int[SIZE][SIZE];
        level = LEVEL_BEGINNER;
    }

    static boolean isInside(int x, int y) {
        return x >= 0 && x < SIZE && y >= 0 && y < SIZE;
    }

    // ------------------------------------------------------------------
    // Win detection
    // ------------------------------------------------------------------

    /**
     * Looks for five in a row anywhere on the board.
     * On success the line is stored in {@link #winX}, {@link #winY},
     * {@link #winDx} and {@link #winDy}.
     */
    public boolean checkWin() {
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                if (cells[x][y] != EMPTY
                        && (isWinningLine(x, y, 0, 1)
                        || isWinningLine(x, y, 1, 0)
                        || isWinningLine(x, y, 1, 1)
                        || isWinningLine(x, y, 1, -1))) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Checks the five cells starting at (x, y) in direction (dx, dy); records a hit. */
    private boolean isWinningLine(int x, int y, int dx, int dy) {
        int stone = cells[x][y];
        for (int step = 1; step < WIN_LENGTH; step++) {
            int cx = x + step * dx;
            int cy = y + step * dy;
            if (!isInside(cx, cy) || cells[cx][cy] != stone) {
                return false;
            }
        }
        winX = x;
        winY = y;
        winDx = dx;
        winDy = dy;
        return true;
    }

    // ------------------------------------------------------------------
    // Computer opponent
    // ------------------------------------------------------------------

    /**
     * Chooses the computer's next move and stores it in {@link #bestX},
     * {@link #bestY}.
     *
     * <ul>
     * <li>{@link #LEVEL_NORMAL}: play the single hottest cell of either map
     *     (defense wins ties).</li>
     * <li>{@link #LEVEL_BEGINNER} and {@link #LEVEL_EXPERT}: same, unless
     *     both maps peak at a similar magnitude (see {@link #sameMagnitude}).
     *     Then play the hottest attack cell if it scores 1000 or more,
     *     otherwise the cell with the highest combined score:
     *     {@code attack + defense} on expert, {@code 2 * attack + defense}
     *     on beginner.</li>
     * </ul>
     */
    public void findBestMove() {
        evaluate(HUMAN);
        for (int x = 0; x < SIZE; x++) {
            System.arraycopy(scores[x], 0, defenseScores[x], 0, SIZE);
        }
        int[] defense = hottestEmptyCell();

        evaluate(COMPUTER);
        int[] attack = hottestEmptyCell();

        int defenseMax = defense[0];
        int attackMax = attack[0];
        int[] simpleChoice = attackMax > defenseMax ? attack : defense;

        if (level == LEVEL_NORMAL || !sameMagnitude(defenseMax, attackMax)) {
            setBest(simpleChoice);
        } else if (attackMax >= 1000) {
            setBest(attack);
        } else if (level == LEVEL_EXPERT) {
            setBest(bestCombinedCell(1));
        } else {
            setBest(bestCombinedCell(2));
        }
    }

    private void setBest(int[] scoreAndCell) {
        bestX = scoreAndCell[1];
        bestY = scoreAndCell[2];
    }

    /**
     * Finds the empty cell with the highest value in {@link #scores}.
     * Ties are broken at random.
     *
     * @return {@code {score, x, y}}
     */
    private int[] hottestEmptyCell() {
        int max = 0;
        int cellX = 1;
        int cellY = 1;
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                if (cells[x][y] != EMPTY) {
                    continue;
                }
                if (max < scores[x][y]) {
                    max = scores[x][y];
                    cellX = x;
                    cellY = y;
                } else if (max == scores[x][y] && random.nextInt() % 2 == 1) {
                    cellX = x;
                    cellY = y;
                }
            }
        }
        return new int[]{max, cellX, cellY};
    }

    /**
     * Finds the empty cell maximising {@code attackWeight * attack + defense}.
     * Expects {@link #scores} to hold the attack map.
     *
     * @return {@code {score, x, y}}
     */
    private int[] bestCombinedCell(int attackWeight) {
        int max = 0;
        int cellX = 1;
        int cellY = 1;
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                int value = scores[x][y] * attackWeight + defenseScores[x][y];
                if (cells[x][y] == EMPTY && max < value) {
                    max = value;
                    cellX = x;
                    cellY = y;
                }
            }
        }
        return new int[]{max, cellX, cellY};
    }

    /**
     * True when both scores have the same leading digit at the highest of the
     * thousands, hundreds or tens place where either is non-zero.
     */
    private static boolean sameMagnitude(int a, int b) {
        for (int unit = 1000; unit >= 10; unit /= 10) {
            int digitA = a / unit;
            int digitB = b / unit;
            if (digitA > 0 || digitB > 0) {
                return digitA == digitB;
            }
        }
        return true;
    }

    /** Fills {@link #scores} with the heat map for {@code player}'s stones. */
    private void evaluate(int player) {
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                scores[x][y] = 0;
            }
        }
        int last = WIN_LENGTH - 1;
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                if (y + last < SIZE) {
                    scoreWindow(player, x, y, 0, 1);
                }
                if (x + last < SIZE && y + last < SIZE) {
                    scoreWindow(player, x, y, 1, 1);
                }
                if (x + last < SIZE) {
                    scoreWindow(player, x, y, 1, 0);
                }
                if (x + last < SIZE && y >= last) {
                    scoreWindow(player, x, y, 1, -1);
                }
            }
        }
    }

    /**
     * Adds the weight of the five-cell window starting at (x, y) in direction
     * (dx, dy) to every cell of that window. Windows holding stones of both
     * players, or none of {@code player}'s, add nothing.
     */
    private void scoreWindow(int player, int x, int y, int dx, int dy) {
        int humanStones = 0;
        int computerStones = 0;
        int cx = x;
        int cy = y;
        for (int k = 0; k < WIN_LENGTH; k++) {
            if (cells[cx][cy] == HUMAN) {
                humanStones++;
            } else if (cells[cx][cy] == COMPUTER) {
                computerStones++;
            }
            cx += dx;
            cy += dy;
        }
        if (humanStones > 0 && computerStones > 0) {
            return;
        }
        int stones = player == COMPUTER ? computerStones : humanStones;
        if (stones == 0) {
            return;
        }

        int weight = 1;
        for (int k = 2; k <= stones; k++) {
            weight *= 10;
        }

        // (cx, cy) is now the cell just after the window; check it and the
        // cell just before the window.
        boolean notFlankedByOwnStone = true;
        if (isInside(cx, cy)) {
            notFlankedByOwnStone = cells[cx][cy] != player;
        }
        cx -= (WIN_LENGTH + 1) * dx;
        cy -= (WIN_LENGTH + 1) * dy;
        if (isInside(cx, cy)) {
            notFlankedByOwnStone = notFlankedByOwnStone && cells[cx][cy] != player;
        }
        if (notFlankedByOwnStone) {
            weight *= 2;
        }

        cx = x;
        cy = y;
        for (int k = 0; k < WIN_LENGTH; k++) {
            scores[cx][cy] += weight;
            cx += dx;
            cy += dy;
        }
    }
}
