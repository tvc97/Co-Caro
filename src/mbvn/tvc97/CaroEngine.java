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
 * <ol>
 * <li>On every level it completes its own five if it can, otherwise blocks
 *     the human's five.</li>
 * <li>Otherwise it builds two heat maps (see {@link #evaluate}): how much
 *     each empty cell helps the computer's lines (attack) and the human's
 *     lines (defense). The level then decides:
 *     <ul>
 *     <li>{@link #LEVEL_BEGINNER}: a random cell among the
 *         {@value #BEGINNER_CHOICES} best by attack + defense, so it often
 *         misses threats.</li>
 *     <li>{@link #LEVEL_NORMAL}: the hottest cell of either map.</li>
 *     <li>{@link #LEVEL_EXPERT}: like normal, but first plays or blocks
 *         forks and open fours (see {@link #searchMove}).</li>
 *     </ul></li>
 * </ol>
 *
 * @author    Tvc97
 * @algorithm Heat maps based on JCaro - BK02 Team (http://bk02.sourceforge.net/jcaro/)
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

    /** Beginner picks at random among this many top cells. */
    private static final int BEGINNER_CHOICES = 3;
    /** Expert examines this many top cells for forcing patterns. */
    private static final int SEARCH_MOVES = 20;

    /** Threat levels returned by {@link #threatLevel}. */
    private static final int THREAT_NONE = 0;
    private static final int THREAT_FORK = 1;
    private static final int THREAT_UNSTOPPABLE = 2;

    /** The four line directions: vertical, horizontal and both diagonals. */
    private static final int[] DIRECTION_X = {0, 1, 1, 1};
    private static final int[] DIRECTION_Y = {1, 0, 1, -1};

    /** Stone owner per cell: {@link #EMPTY}, {@link #HUMAN} or {@link #COMPUTER}. */
    int[][] cells;
    int level;

    /** Result of {@link #findBestMove()}. */
    int bestX, bestY;

    /** Start cell and direction of the winning line found by {@link #checkWin()}. */
    int winX, winY, winDx, winDy;

    /** Heat maps for the computer's lines (attack) and the human's (defense). */
    private int[][] attackScores;
    private int[][] defenseScores;
    /** Bounding box of all stones, updated by {@link #findStones()}. */
    private int minX, maxX, minY, maxY;
    private final Random random;

    public CaroEngine() {
        random = new Random(new Date().getTime());
    }

    /** Clears the board for a new game. */
    void reset() {
        cells = new int[SIZE][SIZE];
        attackScores = new int[SIZE][SIZE];
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

    /** Length of {@code player}'s unbroken line through (x, y) in one direction, counting (x, y). */
    private int lineLength(int x, int y, int dx, int dy, int player) {
        int length = 1;
        for (int cx = x + dx, cy = y + dy; isInside(cx, cy) && cells[cx][cy] == player; cx += dx, cy += dy) {
            length++;
        }
        for (int cx = x - dx, cy = y - dy; isInside(cx, cy) && cells[cx][cy] == player; cx -= dx, cy -= dy) {
            length++;
        }
        return length;
    }

    /** True if {@code player} stone at empty cell (x, y) would complete five in direction d. */
    private boolean makesFive(int x, int y, int d, int player) {
        return lineLength(x, y, DIRECTION_X[d], DIRECTION_Y[d], player) >= WIN_LENGTH;
    }

    /**
     * Finds an empty cell where {@code player} would complete five.
     *
     * @return true if found; the cell is stored in {@link #bestX}, {@link #bestY}
     */
    private boolean findFiveMaker(int player) {
        for (int x = Math.max(0, minX - 1); x <= Math.min(SIZE - 1, maxX + 1); x++) {
            for (int y = Math.max(0, minY - 1); y <= Math.min(SIZE - 1, maxY + 1); y++) {
                if (cells[x][y] != EMPTY) {
                    continue;
                }
                for (int d = 0; d < 4; d++) {
                    if (makesFive(x, y, d, player)) {
                        bestX = x;
                        bestY = y;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // ------------------------------------------------------------------
    // Computer opponent
    // ------------------------------------------------------------------

    /** Chooses the computer's next move and stores it in {@link #bestX}, {@link #bestY}. */
    public void findBestMove() {
        if (!findStones()) {
            bestX = SIZE / 2;
            bestY = SIZE / 2;
            return;
        }
        if (findFiveMaker(COMPUTER) || findFiveMaker(HUMAN)) {
            return;
        }
        evaluate(COMPUTER, attackScores);
        evaluate(HUMAN, defenseScores);

        int[] candidates = topCells(level == LEVEL_BEGINNER ? BEGINNER_CHOICES : SEARCH_MOVES);
        if (level == LEVEL_NORMAL || candidates.length == 0) {
            // No cell near the stones is free: fall back to any empty cell.
            greedyMove();
        } else if (level == LEVEL_BEGINNER) {
            setBest(candidates[Math.abs(random.nextInt()) % candidates.length]);
        } else {
            searchMove(candidates);
        }
    }

    /**
     * Updates the stone bounding box.
     *
     * @return false if the board is empty
     */
    private boolean findStones() {
        minX = SIZE;
        minY = SIZE;
        maxX = -1;
        maxY = -1;
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                if (cells[x][y] != EMPTY) {
                    minX = Math.min(minX, x);
                    maxX = Math.max(maxX, x);
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        return maxX >= 0;
    }

    /** Plays the hottest cell of either heat map (defense wins ties); equal cells are picked at random. */
    private void greedyMove() {
        int[] attack = hottestEmptyCell(attackScores);
        int[] defense = hottestEmptyCell(defenseScores);
        setBest(attack[0] > defense[0] ? attack : defense);
    }

    /**
     * Expert move: recognises forcing patterns that the heat maps only
     * approximate. In order of priority it plays
     * <ol>
     * <li>its own double threat that cannot be stopped (an open four or two fours),</li>
     * <li>a block of the human's such threat,</li>
     * <li>its own fork (a four plus an open three, or two open threes),</li>
     * <li>a block of the human's fork,</li>
     * <li>otherwise the {@link #greedyMove()} choice.</li>
     * </ol>
     * Only the most promising cells by heat map are examined.
     */
    private void searchMove(int[] candidates) {
        int[] bestThreat = new int[2];
        int[] bestCell = new int[2];
        for (int i = 0; i < candidates.length; i++) {
            int x = candidates[i] / SIZE;
            int y = candidates[i] % SIZE;
            for (int side = 0; side < 2; side++) {
                int threat = threatLevel(x, y, side == 0 ? COMPUTER : HUMAN);
                if (threat > bestThreat[side]) {
                    bestThreat[side] = threat;
                    bestCell[side] = candidates[i];
                }
            }
        }
        // Attack wins ties: a fork of our own is at least as fast as theirs.
        for (int threat = THREAT_UNSTOPPABLE; threat >= THREAT_FORK; threat--) {
            if (bestThreat[0] == threat) {
                setBest(bestCell[0]);
                return;
            }
            if (bestThreat[1] == threat) {
                setBest(bestCell[1]);
                return;
            }
        }
        greedyMove();
    }

    /**
     * How dangerous a {@code player} stone at empty cell (x, y) would be:
     * {@link #THREAT_UNSTOPPABLE}, {@link #THREAT_FORK} or {@link #THREAT_NONE}.
     */
    private int threatLevel(int x, int y, int player) {
        cells[x][y] = player;
        int fours = 0;
        int openThrees = 0;
        for (int d = 0; d < 4; d++) {
            int fiveMakers = countFiveMakers(x, y, d, player);
            if (fiveMakers > 0) {
                fours += fiveMakers;
            } else if (isOpenThree(x, y, d, player)) {
                openThrees++;
            }
        }
        cells[x][y] = EMPTY;

        if (fours >= 2) {
            return THREAT_UNSTOPPABLE;
        }
        if (fours + openThrees >= 2) {
            return THREAT_FORK;
        }
        return THREAT_NONE;
    }

    /** Empty cells on the line through (x, y) in direction d where {@code player} would make five. */
    private int countFiveMakers(int x, int y, int d, int player) {
        int count = 0;
        for (int step = -(WIN_LENGTH - 1); step < WIN_LENGTH; step++) {
            int cx = x + step * DIRECTION_X[d];
            int cy = y + step * DIRECTION_Y[d];
            if (isInside(cx, cy) && cells[cx][cy] == EMPTY && makesFive(cx, cy, d, player)) {
                count++;
            }
        }
        return count;
    }

    /**
     * True if {@code player} can turn the line through (x, y) in direction d
     * into an open four (two ways to make five) with one more stone.
     */
    private boolean isOpenThree(int x, int y, int d, int player) {
        for (int step = -(WIN_LENGTH - 1); step < WIN_LENGTH; step++) {
            int cx = x + step * DIRECTION_X[d];
            int cy = y + step * DIRECTION_Y[d];
            if (isInside(cx, cy) && cells[cx][cy] == EMPTY) {
                cells[cx][cy] = player;
                boolean openFour = countFiveMakers(cx, cy, d, player) >= 2;
                cells[cx][cy] = EMPTY;
                if (openFour) {
                    return true;
                }
            }
        }
        return false;
    }

    // ------------------------------------------------------------------
    // Heat maps
    // ------------------------------------------------------------------

    private void setBest(int[] scoreAndCell) {
        bestX = scoreAndCell[1];
        bestY = scoreAndCell[2];
    }

    private void setBest(int cell) {
        bestX = cell / SIZE;
        bestY = cell % SIZE;
    }

    /**
     * Finds the empty cell with the highest value in {@code scores}.
     * Ties are broken at random.
     *
     * @return {@code {score, x, y}}
     */
    private int[] hottestEmptyCell(int[][] scores) {
        int max = 0;
        int cellX = SIZE / 2;
        int cellY = SIZE / 2;
        int ties = 0;
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                if (cells[x][y] != EMPTY) {
                    continue;
                }
                if (scores[x][y] > max || ties == 0) {
                    max = scores[x][y];
                    cellX = x;
                    cellY = y;
                    ties = 1;
                } else if (scores[x][y] == max) {
                    ties++;
                    if (Math.abs(random.nextInt()) % ties == 0) {
                        cellX = x;
                        cellY = y;
                    }
                }
            }
        }
        return new int[]{max, cellX, cellY};
    }

    /**
     * The {@code count} empty cells with the highest attack + defense score,
     * best first, each encoded as {@code x * SIZE + y}. Only cells near
     * existing stones (score above 0) are returned, so there may be fewer.
     */
    private int[] topCells(int count) {
        int[] cellsFound = new int[count];
        int[] values = new int[count];
        int found = 0;
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                int value = attackScores[x][y] + defenseScores[x][y];
                if (cells[x][y] != EMPTY || value == 0) {
                    continue;
                }
                // Insertion into the sorted top list.
                int i = found < count ? found++ : count;
                while (i > 0 && values[i - 1] < value) {
                    if (i < count) {
                        values[i] = values[i - 1];
                        cellsFound[i] = cellsFound[i - 1];
                    }
                    i--;
                }
                if (i < count) {
                    values[i] = value;
                    cellsFound[i] = x * SIZE + y;
                }
            }
        }
        int[] result = new int[found];
        System.arraycopy(cellsFound, 0, result, 0, found);
        return result;
    }

    /**
     * Fills {@code scores} with the heat map for {@code player}'s stones:
     * every five-cell window holding only that player's stones adds 1, 10,
     * 100 or 1000 (for 1-4 stones) to each of its cells, doubled when the
     * cells just outside the window do not hold that player's stone.
     */
    private void evaluate(int player, int[][] scores) {
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                scores[x][y] = 0;
            }
        }
        // Windows away from every stone add nothing, so only start windows
        // near the stones' bounding box.
        int reach = WIN_LENGTH - 1;
        int fromX = Math.max(0, minX - reach);
        int toX = Math.min(SIZE - 1, maxX + reach);
        int fromY = Math.max(0, minY - reach);
        int toY = Math.min(SIZE - 1, maxY + reach);
        for (int x = fromX; x <= toX; x++) {
            for (int y = fromY; y <= toY; y++) {
                if (y + reach < SIZE) {
                    scoreWindow(player, scores, x, y, 0, 1);
                }
                if (x + reach < SIZE && y + reach < SIZE) {
                    scoreWindow(player, scores, x, y, 1, 1);
                }
                if (x + reach < SIZE) {
                    scoreWindow(player, scores, x, y, 1, 0);
                }
                if (x + reach < SIZE && y >= reach) {
                    scoreWindow(player, scores, x, y, 1, -1);
                }
            }
        }
    }

    /** Adds one window's weight to its cells; see {@link #evaluate}. */
    private void scoreWindow(int player, int[][] scores, int x, int y, int dx, int dy) {
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
