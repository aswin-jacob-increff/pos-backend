package org.example.util;

import org.example.exception.ApiException;
import org.springframework.web.multipart.MultipartFile;

public class FileValidationUtil {
    
    private static final int MAX_FILE_SIZE = 5000; // Maximum number of rows
    
    /**
     * Validates TSV file upload
     * @param file MultipartFile to validate
     * @throws ApiException if validation fails
     */
    public static void validateTsvFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException("Please upload a valid non-empty file.");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".tsv")) {
            throw new ApiException("Please upload a valid .tsv file.");
        }
    }
    
    /**
     * Validates file size (number of rows)
     * @param rowCount Number of rows in the file
     * @throws ApiException if file is too large
     */
    public static void validateFileSize(int rowCount) {
        if (rowCount > MAX_FILE_SIZE) {
            throw new ApiException("File upload limit exceeded: Maximum " + MAX_FILE_SIZE + " rows allowed.");
        }
    }
} 