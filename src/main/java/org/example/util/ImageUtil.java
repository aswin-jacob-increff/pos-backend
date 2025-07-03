package org.example.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Base64;
import org.springframework.web.multipart.MultipartFile;

public class ImageUtil {

    public static String encodeImageToBase64(File imageFile) throws IOException {
        byte[] fileContent = Files.readAllBytes(imageFile.toPath());
        return Base64.getEncoder().encodeToString(fileContent);
    }

    public static String encodeMultipartFileToBase64(MultipartFile multipartFile) throws IOException {
        byte[] fileContent = multipartFile.getBytes();
        return Base64.getEncoder().encodeToString(fileContent);
    }

    public static void decodeBase64ToImage(String base64Image, String outputFilePath) throws IOException {
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        try (OutputStream out = new FileOutputStream(outputFilePath)) {
            out.write(imageBytes);
        }
    }
}
