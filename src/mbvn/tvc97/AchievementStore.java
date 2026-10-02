package mbvn.tvc97;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import javax.microedition.rms.RecordStore;

/**
 * Win/loss counts per difficulty level, persisted in the device's record
 * store.
 *
 * <p>Storage format (record 1 of store {@value #STORE_NAME}): for each level
 * in {@link CaroEngine} order, two big-endian ints: wins, then losses.
 * Load and save failures are ignored; the counts simply stay as they are.
 *
 * @author Tvc97
 */
class AchievementStore {

    private static final String STORE_NAME = "save";
    private static final int LEVEL_COUNT = 3;
    private static final int WINS = 0;
    private static final int LOSSES = 1;

    /** {@code counts[level][WINS or LOSSES]}. */
    private final int[][] counts = new int[LEVEL_COUNT][2];

    void record(int level, boolean humanWon) {
        counts[level][humanWon ? WINS : LOSSES]++;
    }

    int getWins(int level) {
        return counts[level][WINS];
    }

    int getLosses(int level) {
        return counts[level][LOSSES];
    }

    void load() {
        try {
            RecordStore store = RecordStore.openRecordStore(STORE_NAME, true);
            if (store.getNumRecords() > 0) {
                DataInputStream in = new DataInputStream(
                        new ByteArrayInputStream(store.getRecord(1)));
                for (int level = 0; level < LEVEL_COUNT; level++) {
                    counts[level][WINS] = in.readInt();
                    counts[level][LOSSES] = in.readInt();
                }
                in.close();
            }
            store.closeRecordStore();
        } catch (Exception e) {
        }
    }

    void save() {
        try {
            RecordStore store = RecordStore.openRecordStore(STORE_NAME, true);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bytes);
            for (int level = 0; level < LEVEL_COUNT; level++) {
                out.writeInt(counts[level][WINS]);
                out.writeInt(counts[level][LOSSES]);
            }
            byte[] data = bytes.toByteArray();
            if (store.getNumRecords() > 0) {
                store.setRecord(1, data, 0, data.length);
            } else {
                store.addRecord(data, 0, data.length);
            }
            out.close();
            store.closeRecordStore();
        } catch (Exception e) {
        }
    }
}
