package org.example.util;

import org.example.exception.ApiException;
import org.example.model.ClientForm;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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
            while ((line = reader.readLine()) != null) {
                String[] cols = line.split("\t");
                if (cols.length != 1) {
                    errors.add("Row " + rowNum + ": Wrong format. Expected 1 column: clientName");
                    rowNum++;
                    continue;
                }
                if (cols[0].trim().isEmpty()) {
                    rowNum++;
                    continue;
                }
                try {
                    ClientForm form = new ClientForm();
                    form.setClientName(cols[0].trim().toLowerCase());
                    try {
                        clients.add(form);
                        
                    } catch (Exception e) {
                        errors.add("Row " + rowNum + ": " + e.getMessage());
                    }
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
}
