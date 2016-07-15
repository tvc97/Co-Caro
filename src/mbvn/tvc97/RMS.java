package mbvn.tvc97;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.rms.*;
import java.io.*;

/**
 * @author Tvc97
 * @forum  http://mbvn.tk
 */
class RMS {

    public int[][] achieve = new int[][]{{0, 0}, {0, 0}, {0, 0}};

    public void update(int level, boolean win) {
        if (win) {
            achieve[level][0]++;
        } else {
            achieve[level][1]++;
        }
    }

    public void load() {
        try {
            RecordStore rs = RecordStore.openRecordStore("save", true);
            if (rs.getNumRecords() > 0) {
                byte[] b = rs.getRecord(1);
                ByteArrayInputStream bais = new ByteArrayInputStream(b);
                DataInputStream dis = new DataInputStream(bais);
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 2; j++) {
                        achieve[i][j] = dis.readInt();
                    }
                }
                bais.close();
                dis.close();
            }
            rs.closeRecordStore();
        } catch (Exception e) {
        }
    }

    public void save() {
        try {
            RecordStore rs = RecordStore.openRecordStore("save", true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 2; j++) {
                    dos.writeInt(achieve[i][j]);
                }
            }
            byte[] b = baos.toByteArray();
            if (rs.getNumRecords() > 0) {
                rs.setRecord(1, b, 0, b.length);
            } else {
                rs.addRecord(b, 0, b.length);
            }
            baos.close();
            dos.close();
            rs.closeRecordStore();
        } catch (Exception e) {
        }
    }
}
