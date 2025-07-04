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

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String header = reader.readLine(); // Expecting: clientName
            if (header == null || !header.toLowerCase().contains("clientname")) {
                throw new IllegalArgumentException("Missing or invalid header: 'clientName'");
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String[] cols = line.split("\t");
                if (cols.length != 1) {
                    throw new ApiException("Client file doesnt match criteria for upload");
                }
                if (cols[0].trim().isEmpty()) continue;

                ClientForm form = new ClientForm();
                form.setClientName(cols[0].trim().toLowerCase());

                clients.add(form);
            }
        }

        return clients;
    }
}
