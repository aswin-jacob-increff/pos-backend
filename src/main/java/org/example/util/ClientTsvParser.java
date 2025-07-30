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
        TsvUploadResult result = new TsvUploadResult();
        List<ClientForm> clients = new ArrayList<>();
        Set<String> seenClientNames = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String header = reader.readLine(); // Expecting: clientName
            if (header == null || !header.toLowerCase().contains("clientname")) {
                result.addError("Missing or invalid header: Expected 'clientName'");
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
                    String clientName = cols[0].trim();
                    
                    // Check for duplicates within the file
                    if (seenClientNames.contains(clientName)) {
                        result.addWarning("Row " + rowNum + ": Skipping duplicate client name '" + clientName + "' found in file");
                        result.incrementFailed();
                        rowNum++;
                        continue;
                    }
                    
                    ClientForm form = new ClientForm();
                    form.setClientName(clientName);
                    form.setStatus(true);
                    clients.add(form);
                    seenClientNames.add(clientName);
                    result.incrementSuccessful();
                    
                } catch (Exception e) {
                    result.addError("Row " + rowNum + ": " + e.getMessage());
                    result.incrementFailed();
                }
                rowNum++;
            }
        } catch (Exception e) {
            result.addError("File reading error: " + e.getMessage());
        }
        
        result.setParsedForms(clients);
        return result;
    }

    /**
     * Parse TSV file with complete validation including database checks
     * @param inputStream InputStream containing TSV data
     * @param clientApi ClientApi instance to check for existing clients
     * @return TsvUploadResult containing parsed forms and any errors/warnings
     */
    public static TsvUploadResult parseWithCompleteValidation(InputStream inputStream, org.example.api.ClientApi clientApi) {
        TsvUploadResult result = new TsvUploadResult();
        List<ClientForm> validClientForms = new ArrayList<>();
        Set<String> seenClientNames = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String header = reader.readLine(); // Expecting: clientName
            if (header == null || !header.toLowerCase().contains("clientname")) {
                result.addError("Missing or invalid header: Expected 'clientName'");
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
                    String clientName = cols[0].trim();
                    
                    // Check for duplicates within the file
                    if (seenClientNames.contains(clientName)) {
                        result.addError("Row " + rowNum + ": Duplicate client name '" + clientName + "' found in file");
                        result.incrementFailed();
                        rowNum++;
                        continue;
                    }
                    
                    // Check if client already exists in database
                    try {
                        org.example.pojo.ClientPojo existingClient = clientApi.getByName(clientName);
                        if (existingClient != null) {
                            result.addError("Row " + rowNum + ": Client '" + clientName + "' already exists in database");
                            result.incrementFailed();
                            rowNum++;
                            continue;
                        }
                    } catch (Exception e) {
                        result.addError("Row " + rowNum + ": Error checking database for client '" + clientName + "': " + e.getMessage());
                        result.incrementFailed();
                        rowNum++;
                        continue;
                    }
                    
                    ClientForm form = new ClientForm();
                    form.setClientName(clientName);
                    form.setStatus(true);
                    validClientForms.add(form);
                    seenClientNames.add(clientName);
                    result.incrementSuccessful();
                    
                } catch (Exception e) {
                    result.addError("Row " + rowNum + ": " + e.getMessage());
                    result.incrementFailed();
                }
                rowNum++;
            }
        } catch (Exception e) {
            result.addError("File reading error: " + e.getMessage());
        }
        
        result.setParsedForms(validClientForms);
        return result;
    }
}
