package org.example.util;

import org.example.exception.ApiException;
import org.example.model.form.InventoryForm;
import org.example.model.data.TsvUploadResult;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class InventoryTsvParser {

    public static List<InventoryForm> parse(InputStream inputStream) throws Exception {
        List<InventoryForm> inventoryForms = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String header = reader.readLine(); // Expecting: barcode<TAB>quantity
            if (header == null || !header.toLowerCase().contains("barcode") || !header.toLowerCase().contains("quantity")) {
                throw new IllegalArgumentException("Missing or invalid header: Expected 'barcode<TAB>quantity'");
            }

            String line;
            int rowNum = 2;
            Set<String> seenBarcodes = new HashSet<>();
            
            while ((line = reader.readLine()) != null) {
                String[] cols = line.split("\t");
                if (cols.length != 2) {
                    errors.add("Row " + rowNum + ": Wrong format. Expected 2 columns: barcode, quantity");
                    rowNum++;
                    continue;
                }
                if (cols[0].trim().isEmpty()) {
                    errors.add("Row " + rowNum + ": Barcode cannot be empty");
                    rowNum++;
                    continue;
                }
                try {
                    String barcode = cols[0].trim().toLowerCase();
                    
                    // Check for duplicates within the file
                    if (seenBarcodes.contains(barcode)) {
                        errors.add("Row " + rowNum + ": Duplicate barcode '" + barcode + "' found in file");
                        rowNum++;
                        continue;
                    }
                    
                    Integer quantity = Integer.parseInt(cols[1].trim());
                    if (quantity < 0) {
                        errors.add("Row " + rowNum + ": Quantity cannot be negative: " + quantity);
                        rowNum++;
                        continue;
                    }
                    
                    InventoryForm form = new InventoryForm();
                    form.setBarcode(barcode);
                    form.setQuantity(quantity);
                    inventoryForms.add(form);
                    seenBarcodes.add(barcode);
                } catch (NumberFormatException e) {
                    errors.add("Row " + rowNum + ": Invalid quantity value '" + cols[1].trim() + "'. Must be a valid integer");
                } catch (Exception e) {
                    e.printStackTrace();
                    errors.add("Row " + rowNum + ": " + e.getMessage());
                }
                rowNum++;
            }
        }
        if (!errors.isEmpty()) {
            throw new ApiException("TSV validation errors: " + String.join(", ", errors));
        }
        return inventoryForms;
    }

    /**
     * Parse TSV file with duplicate detection and detailed error reporting
     * @param inputStream The input stream containing TSV data
     * @return TsvUploadResult containing parsed forms and any errors/warnings
     */
    public static TsvUploadResult parseWithDuplicateDetection(InputStream inputStream) {
        TsvUploadResult result = new TsvUploadResult();
        List<InventoryForm> inventoryForms = new ArrayList<>();
        Set<String> seenBarcodes = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String header = reader.readLine(); // Expecting: barcode<TAB>quantity
            if (header == null || !header.toLowerCase().contains("barcode") || !header.toLowerCase().contains("quantity")) {
                result.addError("Missing or invalid header: Expected 'barcode<TAB>quantity'");
                return result;
            }

            String line;
            int rowNum = 2;
            
            while ((line = reader.readLine()) != null) {
                result.setTotalRows(result.getTotalRows() + 1);
                
                String[] cols = line.split("\t");
                if (cols.length != 2) {
                    result.addError("Row " + rowNum + ": Wrong format. Expected 2 columns: barcode, quantity");
                    result.incrementFailed();
                    rowNum++;
                    continue;
                }
                if (cols[0].trim().isEmpty()) {
                    result.addError("Row " + rowNum + ": Barcode cannot be empty");
                    result.incrementFailed();
                    rowNum++;
                    continue;
                }
                
                try {
                    String barcode = cols[0].trim().toLowerCase();
                    
                    // Check for duplicates within the file
                    if (seenBarcodes.contains(barcode)) {
                        result.addWarning("Row " + rowNum + ": Skipping duplicate barcode '" + barcode + "' found in file");
                        result.incrementFailed();
                        rowNum++;
                        continue;
                    }
                    
                    Integer quantity = Integer.parseInt(cols[1].trim());
                    if (quantity < 0) {
                        result.addError("Row " + rowNum + ": Quantity cannot be negative: " + quantity);
                        result.incrementFailed();
                        rowNum++;
                        continue;
                    }
                    
                    InventoryForm form = new InventoryForm();
                    form.setBarcode(barcode);
                    form.setQuantity(quantity);
                    inventoryForms.add(form);
                    seenBarcodes.add(barcode);
                    result.incrementSuccessful();
                    
                } catch (NumberFormatException e) {
                    result.addError("Row " + rowNum + ": Invalid quantity value '" + cols[1].trim() + "'. Must be a valid integer");
                    result.incrementFailed();
                } catch (Exception e) {
                    e.printStackTrace();
                    result.addError("Row " + rowNum + ": " + e.getMessage());
                    result.incrementFailed();
                }
                rowNum++;
            }
        } catch (Exception e) {
            result.addError("File reading error: " + e.getMessage());
        }
        
        // Store the parsed forms in the result for later use
        result.setParsedForms(inventoryForms);
        return result;
    }

    /**
     * Parse TSV file with complete validation including database checks
     * @param inputStream The input stream containing TSV data
     * @param productApi ProductApi instance to check for existing products
     * @return TsvUploadResult containing parsed forms and any errors/warnings
     */
    public static TsvUploadResult parseWithCompleteValidation(InputStream inputStream, org.example.api.ProductApi productApi) {

        TsvUploadResult result = new TsvUploadResult();
        List<InventoryForm> validInventoryForms = new ArrayList<>();
        Set<String> seenBarcodes = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String header = reader.readLine(); // Expecting: barcode<TAB>quantity
            if (header == null || !header.toLowerCase().contains("barcode") || !header.toLowerCase().contains("quantity")) {
                result.addError("Missing or invalid header: Expected 'barcode<TAB>quantity'");
                return result;
            }

            String line;
            int rowNum = 2;
            while ((line = reader.readLine()) != null) {
                result.setTotalRows(result.getTotalRows() + 1);
                
                String[] cols = line.split("\t");
                if (cols.length != 2) {
                    result.addError("Row " + rowNum + ": Wrong format. Expected 2 columns: barcode, quantity");
                    result.incrementFailed();
                    rowNum++;
                    continue;
                }
                if (cols[0].trim().isEmpty()) {
                    result.addError("Row " + rowNum + ": Barcode cannot be empty");
                    result.incrementFailed();
                    rowNum++;
                    continue;
                }
                
                try {
                    String barcode = cols[0].trim().toLowerCase();
                    
                    // Check for duplicates within the file
                    if (seenBarcodes.contains(barcode)) {
                        result.addError("Row " + rowNum + ": Duplicate barcode '" + barcode + "' found in file");
                        result.incrementFailed();
                        rowNum++;
                        continue;
                    }
                    
                    // Check if product exists
                    try {
                        org.example.pojo.ProductPojo existingProduct = productApi.getByBarcode(barcode);
                        if (existingProduct == null) {
                            result.addError("Row " + rowNum + ": Product with barcode '" + barcode + "' not found");
                            result.incrementFailed();
                            rowNum++;
                            continue;
                        }
                    } catch (Exception e) {
                        result.addError("Row " + rowNum + ": Error validating product '" + barcode + "': " + e.getMessage());
                        result.incrementFailed();
                        rowNum++;
                        continue;
                    }
                    
                    Integer quantity = Integer.parseInt(cols[1].trim());
                    if (quantity < 0) {
                        result.addError("Row " + rowNum + ": Quantity cannot be negative: " + quantity);
                        result.incrementFailed();
                        rowNum++;
                        continue;
                    }
                    
                    InventoryForm form = new InventoryForm();
                    form.setBarcode(barcode);
                    form.setQuantity(quantity);
                    validInventoryForms.add(form);
                    seenBarcodes.add(barcode);
                    result.incrementSuccessful();
                    
                } catch (NumberFormatException e) {
                    result.addError("Row " + rowNum + ": Invalid quantity value '" + cols[1].trim() + "'. Must be a valid integer");
                    result.incrementFailed();
                } catch (Exception e) {
                    result.addError("Row " + rowNum + ": " + e.getMessage());
                    result.incrementFailed();
                }
                rowNum++;
            }
        } catch (Exception e) {
            result.addError("File reading error: " + e.getMessage());
        }
        
        // Store the valid forms in the result for later use
        result.setParsedForms(validInventoryForms);

        return result;
    }
}
