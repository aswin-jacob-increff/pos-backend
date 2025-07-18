package org.example.util;

import org.example.exception.ApiException;
import org.example.model.form.ClientForm;
import org.example.model.data.TsvUploadResult;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class ClientTsvParser {

    public static List<ClientForm> parse(InputStream inputStream) throws Exception {
        List<ClientForm> clients = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String header = reader.readLine(); // Expecting: clientName
            if (header == null || !header.toLowerCase().contains("clientname")) {
                throw new IllegalArgumentException("Missing or invalid header: 'clientName'");
            }

            String line;
            int rowNum = 2;
            Set<String> seenClientNames = new HashSet<>();
            
            while ((line = reader.readLine()) != null) {
                String[] cols = line.split("\t");
                if (cols.length != 1) {
                    errors.add("Row " + rowNum + ": Wrong format. Expected 1 column: clientName");
                    rowNum++;
                    continue;
                }
                if (cols[0].trim().isEmpty()) {
                    errors.add("Row " + rowNum + ": Client name cannot be empty");
                    rowNum++;
                    continue;
                }
                try {
                    String clientName = cols[0].trim().toLowerCase();
                    
                    // Check for duplicates within the file
                    if (seenClientNames.contains(clientName)) {
                        errors.add("Row " + rowNum + ": Duplicate client name '" + clientName + "' found in file");
                        rowNum++;
                        continue;
                    }
                    
                    ClientForm form = new ClientForm();
                    form.setClientName(clientName);
                    clients.add(form);
                    seenClientNames.add(clientName);
                    
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
        return clients;
    }

    /**
     * Parse TSV file with duplicate detection and detailed error reporting
     * @param inputStream The input stream containing TSV data
     * @return TsvUploadResult containing parsed forms and any errors/warnings
     */
    public static TsvUploadResult parseWithDuplicateDetection(InputStream inputStream) {
        System.out.println("ClientTsvParser.parseWithDuplicateDetection - Starting");
        TsvUploadResult result = new TsvUploadResult();
        List<ClientForm> clients = new ArrayList<>();
        Set<String> seenClientNames = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String header = reader.readLine(); // Expecting: clientName
            System.out.println("ClientTsvParser.parseWithDuplicateDetection - Header: " + header);
            if (header == null || !header.toLowerCase().contains("clientname")) {
                System.out.println("ClientTsvParser.parseWithDuplicateDetection - Invalid header");
                result.addError("Missing or invalid header: 'clientName'");
                return result;
            }

            String line;
            int rowNum = 2;
            
            while ((line = reader.readLine()) != null) {
                System.out.println("ClientTsvParser.parseWithDuplicateDetection - Processing row " + rowNum + ": " + line);
                result.setTotalRows(result.getTotalRows() + 1);
                
                String[] cols = line.split("\t");
                if (cols.length != 1) {
                    System.out.println("ClientTsvParser.parseWithDuplicateDetection - Row " + rowNum + " has wrong format");
                    result.addError("Row " + rowNum + ": Wrong format. Expected 1 column: clientName");
                    result.incrementFailed();
                    rowNum++;
                    continue;
                }
                if (cols[0].trim().isEmpty()) {
                    System.out.println("ClientTsvParser.parseWithDuplicateDetection - Row " + rowNum + " has empty client name");
                    result.addError("Row " + rowNum + ": Client name cannot be empty");
                    result.incrementFailed();
                    rowNum++;
                    continue;
                }
                
                try {
                    String clientName = cols[0].trim().toLowerCase();
                    System.out.println("ClientTsvParser.parseWithDuplicateDetection - Processing client name: " + clientName);
                    
                    // Check for duplicates within the file
                    if (seenClientNames.contains(clientName)) {
                        System.out.println("ClientTsvParser.parseWithDuplicateDetection - Duplicate client name found: " + clientName);
                        result.addWarning("Row " + rowNum + ": Skipping duplicate client name '" + clientName + "' found in file");
                        result.incrementFailed();
                        rowNum++;
                        continue;
                    }
                    
                    ClientForm form = new ClientForm();
                    form.setClientName(clientName);
                    clients.add(form);
                    seenClientNames.add(clientName);
                    result.incrementSuccessful();
                    System.out.println("ClientTsvParser.parseWithDuplicateDetection - Successfully added client: " + clientName);
                    
                } catch (Exception e) {
                    System.out.println("ClientTsvParser.parseWithDuplicateDetection - Error processing row " + rowNum + ": " + e.getMessage());
                    e.printStackTrace();
                    result.addError("Row " + rowNum + ": " + e.getMessage());
                    result.incrementFailed();
                }
                rowNum++;
            }
        } catch (Exception e) {
            System.out.println("ClientTsvParser.parseWithDuplicateDetection - File reading error: " + e.getMessage());
            result.addError("File reading error: " + e.getMessage());
        }
        
        // Store the parsed forms in the result for later use
        result.setParsedForms(clients);
        System.out.println("ClientTsvParser.parseWithDuplicateDetection - Completed. Total: " + result.getTotalRows() + ", Successful: " + result.getSuccessfulRows() + ", Failed: " + result.getFailedRows());
        return result;
    }

    /**
     * Parse TSV file with complete validation including database checks
     * @param inputStream The input stream containing TSV data
     * @param clientApi ClientApi instance to check for existing clients
     * @return TsvUploadResult containing parsed forms and any errors/warnings
     */
    public static TsvUploadResult parseWithCompleteValidation(InputStream inputStream, org.example.api.ClientApi clientApi) {
        System.out.println("ClientTsvParser.parseWithCompleteValidation - Starting");
        TsvUploadResult result = new TsvUploadResult();
        List<ClientForm> validClients = new ArrayList<>();
        Set<String> seenClientNames = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String header = reader.readLine(); // Expecting: clientName
            System.out.println("ClientTsvParser.parseWithCompleteValidation - Header: " + header);
            if (header == null || !header.toLowerCase().contains("clientname")) {
                System.out.println("ClientTsvParser.parseWithCompleteValidation - Invalid header");
                result.addError("Missing or invalid header: 'clientName'");
                return result;
            }

            String line;
            int rowNum = 2;
            while ((line = reader.readLine()) != null) {
                result.setTotalRows(result.getTotalRows() + 1);
                
                String[] cols = line.split("\t");
                if (cols.length != 1) {
                    result.addError("Row " + rowNum + ": Wrong format. Expected 1 column: clientName");
                    result.incrementFailed();
                    rowNum++;
                    continue;
                }
                if (cols[0].trim().isEmpty()) {
                    result.addError("Row " + rowNum + ": Client name cannot be empty");
                    result.incrementFailed();
                    rowNum++;
                    continue;
                }
                
                try {
                    String clientName = cols[0].trim().toLowerCase();
                    System.out.println("ClientTsvParser.parseWithCompleteValidation - Processing client name: " + clientName);
                    
                    // Check for duplicates within the file
                    if (seenClientNames.contains(clientName)) {
                        System.out.println("ClientTsvParser.parseWithCompleteValidation - Duplicate client name found in file: " + clientName);
                        result.addError("Row " + rowNum + ": Duplicate client name '" + clientName + "' found in file");
                        result.incrementFailed();
                        rowNum++;
                        continue;
                    }
                    
                    // Check for duplicates in database
                    try {
                        org.example.pojo.ClientPojo existingClient = clientApi.getByName(clientName);
                        if (existingClient != null) {
                            System.out.println("ClientTsvParser.parseWithCompleteValidation - Client already exists in database: " + clientName);
                            result.addError("Row " + rowNum + ": Client '" + clientName + "' already exists");
                            result.incrementFailed();
                            rowNum++;
                            continue;
                        }
                    } catch (Exception e) {
                        System.out.println("ClientTsvParser.parseWithCompleteValidation - Error checking database for client '" + clientName + "': " + e.getMessage());
                        // If there's an error checking the database, treat it as a valid client
                    }
                    
                    ClientForm form = new ClientForm();
                    form.setClientName(clientName);
                    validClients.add(form);
                    seenClientNames.add(clientName);
                    result.incrementSuccessful();
                    System.out.println("ClientTsvParser.parseWithCompleteValidation - Successfully validated client: " + clientName);
                    
                } catch (Exception e) {
                    System.out.println("ClientTsvParser.parseWithCompleteValidation - Error processing row " + rowNum + ": " + e.getMessage());
                    e.printStackTrace();
                    result.addError("Row " + rowNum + ": " + e.getMessage());
                    result.incrementFailed();
                }
                rowNum++;
            }
        } catch (Exception e) {
            System.out.println("ClientTsvParser.parseWithCompleteValidation - File reading error: " + e.getMessage());
            result.addError("File reading error: " + e.getMessage());
        }
        
        // Store the valid forms in the result for later use
        result.setParsedForms(validClients);
        System.out.println("ClientTsvParser.parseWithCompleteValidation - Completed. Total: " + result.getTotalRows() + ", Successful: " + result.getSuccessfulRows() + ", Failed: " + result.getFailedRows());
        return result;
    }
}
