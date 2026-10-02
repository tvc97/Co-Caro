package mbvn.tvc97;

import java.io.InputStream;
import javax.microedition.lcdui.Image;

/**
 * Loads bundled resources from the {@code /res} folder of the JAR.
 *
 * <p>Some resources (the logo and the "about" text) are stored scrambled so
 * they cannot be edited in the JAR directly; {@link #decode(byte[])} reverses
 * that scrambling.
 *
 * @author Tvc97
 */
final class Resources {

    private Resources() {
    }

    /**
     * Loads {@code /res/<name>.png}.
     *
     * @return the image, or {@code null} if it cannot be loaded
     */
    static Image loadImage(String name) {
        Image image = null;
        try {
            image = Image.createImage("/res/" + name + ".png");
            Runtime.getRuntime().gc();
        } catch (Exception e) {
        }
        return image;
    }

    /**
     * Reads a scrambled resource and returns its decoded bytes.
     *
     * @param path absolute resource path, e.g. {@code /res/str}
     * @throws Exception if the resource is missing or cannot be read
     */
    static byte[] readDecoded(String path) throws Exception {
        // Resolve through an application class so the MIDlet JAR is searched.
        // A class literal is avoided: it needs classes CLDC 1.0 lacks.
        InputStream in = new Resources().getClass().getResourceAsStream(path);
        try {
            byte[] data = new byte[in.available()];
            in.read(data);
            return decode(data);
        } finally {
            in.close();
        }
    }

    /** Unscrambles {@code data} in place and returns it. */
    static byte[] decode(byte[] data) {
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (((~(data[i] - 0xBA)) ^ 0xAB) & 0xFF);
        }
        return data;
    }
}
