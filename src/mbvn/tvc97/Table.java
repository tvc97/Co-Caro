package mbvn.tvc97;

import java.util.*;

/**
 * @author    Tvc97
 * @forum     http://mbvn.tk
 * @algorithm JCaro - BK02 Team
 */
public class Table {

    int[][] cell;
    int[][] score;
    int[][] score1;
    int m, n, kh, resX, resY, level = 1;
    int wx, wy, wdx, wdy;
    Random r;

    public Table() {
        Date d = new Date();
        r = new Random(d.getTime());
    }

    void tinh(int x, int y, int dx, int dy) {
        int i, j, k, d1, d2, s;

        i = x;
        j = y;
        d1 = 0;
        d2 = 0;
        for (k = 0; k <= 4; k++) {
            if (cell[i][j] == 1) {
                d1++;
            }
            if (cell[i][j] == 2) {
                d2++;
            }
            i += dx;
            j += dy;
        }
        if ((d1 > 0) && (d2 > 0)) {
            return;
        }
        if (kh == 2) {
            d1 = d2;
        }
        if (d1 == 0) {
            return;
        }
        s = 1;
        for (k = 2; k <= d1; k++) {
            s *= 10;
        }

        boolean ok = true;
        if ((i >= 0) && (i < m) && (j >= 0) && (j < n)) {
            ok = (cell[i][j] != kh);
        }
        i = i - 6 * dx;
        j = j - 6 * dy;
        if ((i >= 0) && (i < m) && (j >= 0) && (j < n)) {
            ok = ok && (cell[i][j] != kh);
        }
        if (ok) {
            s *= 2;
        }

        i = x;
        j = y;
        for (k = 0; k <= 4; k++) {
            score[i][j] += s;
            i += dx;
            j += dy;
        }
    }

    public void evaluate() {
        int i, j;

        for (i = 0; i < m; i++) {
            for (j = 0; j < n; j++) {
                score[i][j] = 0;
            }
        }

        for (i = 0; i < m; i++) {
            for (j = 0; j < n; j++) {

                if (j + 4 < n) {
                    tinh(i, j, 0, 1);
                }

                if ((i + 4 < m) && (j + 4 < n)) {
                    tinh(i, j, 1, 1);
                }

                if (i + 4 < m) {
                    tinh(i, j, 1, 0);
                }

                if ((i + 4 < m) && (j >= 4)) {
                    tinh(i, j, 1, -1);
                }
            }
        }
    }

    public boolean check5(int i, int j, int dx, int dy) {
        int i1, j1, t, s;
        i1 = i;
        j1 = j;
        s = 1;
        for (t = 1; t <= 4; t++) {
            i1 += dx;
            j1 += dy;
            if ((i1 >= 0) && (i1 < m) && (j1 >= 0) && (j1 < n)) {
                if (cell[i][j] == cell[i1][j1]) {
                    s++;
                }
            }
        }
        if (s == 5) {
            return true;
        } else {
            return false;
        }
    }

    public boolean checkwin() {
        System.out.println(level);
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (cell[i][j] != 0) {
                    if (check5(i, j, 0, 1)) {
                        wx = i;
                        wy = j;
                        wdx = 0;
                        wdy = 1;
                        return true;
                    }
                    if (check5(i, j, 1, 0)) {
                        wx = i;
                        wy = j;
                        wdx = 1;
                        wdy = 0;
                        return true;
                    }
                    if (check5(i, j, 1, 1)) {
                        wx = i;
                        wy = j;
                        wdx = 1;
                        wdy = 1;
                        return true;
                    }
                    if (check5(i, j, 1, -1)) {
                        wx = i;
                        wy = j;
                        wdx = 1;
                        wdy = -1;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    boolean equivalent(int d1, int d2) {
        int e1, e2, t, i;
        t = 1000;
        for (i = 1; i <= 3; i++) {
            e1 = d1 / t;
            e2 = d2 / t;
            if ((e1 > 0) || (e2 > 0)) {
                if (e1 == e2) {
                    return true;
                } else {
                    return false;
                }
            }
            t = t / 10;
        }
        return true;
    }

    public void findSolution() {
        int max, max1, max2;
        int i, j, t, li = 1, lj = 1, li1 = 1, lj1 = 1, li2 = 1, lj2 = 1;

        max1 = 0;
        kh = 1;
        evaluate();
        for (i = 0; i < m; i++) {
            for (j = 0; j < n; j++) {
                score1[i][j] = score[i][j];
                if (cell[i][j] == 0) {
                    if (max1 < score[i][j]) {
                        max1 = score[i][j];
                        li1 = i;
                        lj1 = j;
                    } else if (max1 == score[i][j]) {
                        t = r.nextInt() % 2;
                        if (t == 1) {
                            li1 = i;
                            lj1 = j;
                        }
                    }
                }
            }
        }

        max2 = 0;
        kh = 2;
        evaluate();
        for (i = 0; i < m; i++) {
            for (j = 0; j < n; j++) {
                if (cell[i][j] == 0) {
                    if (max2 < score[i][j]) {
                        max2 = score[i][j];
                        li2 = i;
                        lj2 = j;
                    } else if (max2 == score[i][j]) {
                        t = r.nextInt() % 2;
                        if (t == 1) {
                            li2 = i;
                            lj2 = j;
                        }
                    }
                }
            }
        }
        if (level == 1) {
            if (max1 < max2) {
                resX = li2;
                resY = lj2;
            } else {
                resX = li1;
                resY = lj1;
            }
        } else {
            if (equivalent(max1, max2)) {
                max = 0;
                if (max2 >= 1000) {
                    resX = li2;
                    resY = lj2;
                } else if (level == 2) {
                    for (i = 0; i < m; i++) {
                        for (j = 0; j < n; j++) {
                            if (cell[i][j] == 0) {
                                if (max < score[i][j] + score1[i][j]) {
                                    max = score[i][j] + score1[i][j];
                                    li = i;
                                    lj = j;
                                }
                            }
                        }
                    }
                    resX = li;
                    resY = lj;
                } else {
                    for (i = 0; i < m; i++) {
                        for (j = 0; j < n; j++) {
                            if (cell[i][j] == 0) {
                                if (max < score[i][j] * 2 + score1[i][j]) {
                                    max = score[i][j] * 2 + score1[i][j];
                                    li = i;
                                    lj = j;
                                }
                            }
                        }
                    }
                    resX = li;
                    resY = lj;
                }
            } else {
                if (max1 < max2) {
                    resX = li2;
                    resY = lj2;
                } else {
                    resX = li1;
                    resY = lj1;
                }
            }
        }
    }
}
