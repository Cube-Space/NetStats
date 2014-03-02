package net.cubespace.NetStats.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class ExtractFile {
    public static void extractFile(String path, File file) {
        InputStream stream = ExtractFile.class.getResourceAsStream(path);
        if (stream == null) {
            throw new IllegalStateException("Could not extract File " + path);
        }

        OutputStream resStreamOut = null;
        int readBytes;
        byte[] buffer = new byte[4096];
        try {
            resStreamOut = new FileOutputStream(file);
            while ((readBytes = stream.read(buffer)) > 0) {
                resStreamOut.write(buffer, 0, readBytes);
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            try {
                stream.close();

                if(resStreamOut != null)
                    resStreamOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
