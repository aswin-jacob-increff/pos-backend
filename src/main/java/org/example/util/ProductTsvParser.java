package org.example.util;

import org.example.exception.ApiException;
import org.example.model.form.ProductForm;
import org.example.model.data.TsvUploadResult;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class ProductTsvParser {
    public static List<ProductForm> parse(InputStream inputStream) throws Exception {
        List<ProductForm> products = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String header = reader.readLine(); // Expecting: barcode, clientname, productname, mrp, imageurl (optional)
            if (header == null || !header.toLowerCase().contains("barcode") || !header.toLowerCase().contains("clientname") || !header.toLowerCase().contains("productname") || !header.toLowerCase().contains("mrp")) {
                throw new IllegalArgumentException("Missing or invalid header: Expected 'barcode<TAB>clientname<TAB>productname<TAB>mrp<TAB>imageurl' (imageurl is optional)");
            }

            String line;
            int rowNum = 2; // 1-based, header is row 1
            Set<String> seenBarcodes = new HashSet<>();
            
            while ((line = reader.readLine()) != null) {
                String[] cols = line.split("\t");
                if (cols.length < 4 || cols.length > 5) {
                    errors.add("Row " + rowNum + ": Wrong format. Expected 4-5 columns: barcode, clientname, productname, mrp, imageurl(optional)");
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
                    
                    ProductForm form = new ProductForm();
                    form.setBarcode(barcode);
                    form.setClientName(cols[1].trim().toLowerCase());
                    form.setName(cols[2].trim().toLowerCase());
                    form.setMrp(Double.parseDouble(cols[3].trim()));
                    // Set image URL if provided (5th column)
                    if (cols.length == 5 && cols[4] != null && !cols[4].trim().isEmpty()) {
                        form.setImage(cols[4].trim());
                    }
                    products.add(form);
                    seenBarcodes.add(barcode);
                } catch (NumberFormatException e) {
                    errors.add("Row " + rowNum + ": Invalid MRP value '" + cols[3].trim() + "'. Must be a valid number");
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
        return products;
    }

    /**
     * Parse TSV file with duplicate detection and detailed error reporting
     * @param inputStream The input stream containing TSV data
     * @return TsvUploadResult containing parsed forms and any errors/warnings
     */
    public static TsvUploadResult parseWithDuplicateDetection(InputStream inputStream) {
        TsvUploadResult result = new TsvUploadResult();
        List<ProductForm> products = new ArrayList<>();
        Set<String> seenBarcodes = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String header = reader.readLine(); // Expecting: barcode, clientname, productname, mrp, imageurl (optional)
            if (header == null || !header.toLowerCase().contains("barcode") || !header.toLowerCase().contains("clientname") || !header.toLowerCase().contains("productname") || !header.toLowerCase().contains("mrp")) {
                result.addError("Missing or invalid header: Expected 'barcode<TAB>clientname<TAB>productname<TAB>mrp<TAB>imageurl' (imageurl is optional)");
                return result;
            }

            String line;
            int rowNum = 2; // 1-based, header is row 1
            
            while ((line = reader.readLine()) != null) {
                result.setTotalRows(result.getTotalRows() + 1);
                
                String[] cols = line.split("\t");
                if (cols.length < 4 || cols.length > 5) {
                    result.addError("Row " + rowNum + ": Wrong format. Expected 4-5 columns: barcode, clientname, productname, mrp, imageurl(optional)");
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
                    
                    ProductForm form = new ProductForm();
                    form.setBarcode(barcode);
                    form.setClientName(cols[1].trim().toLowerCase());
                    form.setName(cols[2].trim().toLowerCase());
                    form.setMrp(Double.parseDouble(cols[3].trim()));
                    // Set image URL if provided (5th column)
                    if (cols.length == 5 && cols[4] != null && !cols[4].trim().isEmpty()) {
                        form.setImage(cols[4].trim());
                    }
                    products.add(form);
                    seenBarcodes.add(barcode);
                    result.incrementSuccessful();
                    
                } catch (NumberFormatException e) {
                    result.addError("Row " + rowNum + ": Invalid MRP value '" + cols[3].trim() + "'. Must be a valid number");
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
        result.setParsedForms(products);
        return result;
    }

    /**
     * Parse TSV file with complete validation including database checks
     * @param inputStream The input stream containing TSV data
     * @param productApi ProductApi instance to check for existing products
     * @param clientApi ClientApi instance to check for existing clients
     * @return TsvUploadResult containing parsed forms and any errors/warnings
     */
    public static TsvUploadResult parseWithCompleteValidation(InputStream inputStream, org.example.api.ProductApi productApi, org.example.api.ClientApi clientApi) {
        System.out.println("ProductTsvParser.parseWithCompleteValidation - Starting");
        TsvUploadResult result = new TsvUploadResult();
        List<ProductForm> validProducts = new ArrayList<>();
        Set<String> seenBarcodes = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String header = reader.readLine(); // Expecting: barcode, clientname, productname, mrp, imageurl (optional)
            System.out.println("ProductTsvParser.parseWithCompleteValidation - Header: " + header);
            if (header == null || !header.toLowerCase().contains("barcode") || !header.toLowerCase().contains("clientname") || !header.toLowerCase().contains("productname") || !header.toLowerCase().contains("mrp")) {
                System.out.println("ProductTsvParser.parseWithCompleteValidation - Invalid header");
                result.addError("Missing or invalid header: Expected 'barcode<TAB>clientname<TAB>productname<TAB>mrp<TAB>imageurl' (imageurl is optional)");
                return result;
            }

            String line;
            int rowNum = 2; // 1-based, header is row 1
            while ((line = reader.readLine()) != null) {
                result.setTotalRows(result.getTotalRows() + 1);
                
                String[] cols = line.split("\t");
                if (cols.length < 4 || cols.length > 5) {
                    result.addError("Row " + rowNum + ": Wrong format. Expected 4-5 columns: barcode, clientname, productname, mrp, imageurl(optional)");
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
                    String clientName = cols[1].trim().toLowerCase();
                    String productName = cols[2].trim().toLowerCase();
                    
                    System.out.println("ProductTsvParser.parseWithCompleteValidation - Processing barcode: " + barcode + ", client: " + clientName);
                    
                    // Check for duplicates within the file
                    if (seenBarcodes.contains(barcode)) {
                        System.out.println("ProductTsvParser.parseWithCompleteValidation - Duplicate barcode found in file: " + barcode);
                        result.addError("Row " + rowNum + ": Duplicate barcode '" + barcode + "' found in file");
                        result.incrementFailed();
                        rowNum++;
                        continue;
                    }
                    
                    // Check for duplicates in database
                    try {
                        org.example.pojo.ProductPojo existingProduct = productApi.getByBarcode(barcode);
                        if (existingProduct != null) {
                            System.out.println("ProductTsvParser.parseWithCompleteValidation - Product already exists in database: " + barcode);
                            result.addError("Row " + rowNum + ": Product with barcode '" + barcode + "' already exists");
                            result.incrementFailed();
                            rowNum++;
                            continue;
                        }
                    } catch (Exception e) {
                        // If product doesn't exist, that's fine - we can proceed
                        System.out.println("ProductTsvParser.parseWithCompleteValidation - Product not found in database (expected): " + barcode);
                    }
                    
                    // Check if client exists and is active
                    try {
                        org.example.pojo.ClientPojo existingClient = clientApi.getByName(clientName);
                        if (existingClient == null) {
                            System.out.println("ProductTsvParser.parseWithCompleteValidation - Client not found: " + clientName);
                            result.addError("Row " + rowNum + ": Client '" + clientName + "' not found");
                            result.incrementFailed();
                            rowNum++;
                            continue;
                        }
                        if (!existingClient.getStatus()) {
                            System.out.println("ProductTsvParser.parseWithCompleteValidation - Client is not active: " + clientName);
                            result.addError("Row " + rowNum + ": Client '" + clientName + "' is not active");
                            result.incrementFailed();
                            rowNum++;
                            continue;
                        }
                    } catch (Exception e) {
                        System.out.println("ProductTsvParser.parseWithCompleteValidation - Error checking client '" + clientName + "': " + e.getMessage());
                        result.addError("Row " + rowNum + ": Error validating client '" + clientName + "': " + e.getMessage());
                        result.incrementFailed();
                        rowNum++;
                        continue;
                    }
                    
                    ProductForm form = new ProductForm();
                    form.setBarcode(barcode);
                    form.setClientName(clientName);
                    form.setName(productName);
                    form.setMrp(Double.parseDouble(cols[3].trim()));
                    // Set image URL if provided (5th column)
                    if (cols.length == 5 && cols[4] != null && !cols[4].trim().isEmpty()) {
                        form.setImage(cols[4].trim());
                    }
                    validProducts.add(form);
                    seenBarcodes.add(barcode);
                    result.incrementSuccessful();
                    System.out.println("ProductTsvParser.parseWithCompleteValidation - Successfully validated product: " + barcode);
                    
                } catch (NumberFormatException e) {
                    System.out.println("ProductTsvParser.parseWithCompleteValidation - Invalid MRP value in row " + rowNum + ": " + cols[3].trim());
                    result.addError("Row " + rowNum + ": Invalid MRP value '" + cols[3].trim() + "'. Must be a valid number");
                    result.incrementFailed();
                } catch (Exception e) {
                    System.out.println("ProductTsvParser.parseWithCompleteValidation - Error processing row " + rowNum + ": " + e.getMessage());
                    e.printStackTrace();
                    result.addError("Row " + rowNum + ": " + e.getMessage());
                    result.incrementFailed();
                }
                rowNum++;
            }
        } catch (Exception e) {
            System.out.println("ProductTsvParser.parseWithCompleteValidation - File reading error: " + e.getMessage());
            result.addError("File reading error: " + e.getMessage());
        }
        
        // Store the valid forms in the result for later use
        result.setParsedForms(validProducts);
        System.out.println("ProductTsvParser.parseWithCompleteValidation - Completed. Total: " + result.getTotalRows() + ", Successful: " + result.getSuccessfulRows() + ", Failed: " + result.getFailedRows());
        return result;
    }
}
