package org.example.util;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.io.File;

public class Base64ToPdfUtil {

    public static File saveBase64AsPdf(String base64, String filePath) throws Exception {
        byte[] pdfBytes = Base64.getDecoder().decode(base64);
        File file = new File(filePath);

        // Ensure parent directories exist
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (OutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(pdfBytes);
        }

        return file;
    }
}
