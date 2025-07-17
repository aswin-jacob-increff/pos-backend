package org.example.util;

import org.example.exception.ApiException;
import org.example.model.form.InventoryForm;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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
            while ((line = reader.readLine()) != null) {
                String[] cols = line.split("\t");
                if (cols.length != 2) {
                    errors.add("Row " + rowNum + ": Wrong format. Expected 2 columns: barcode, quantity");
                    rowNum++;
                    continue;
                }
                try {
                    String barcode = cols[0].trim().toLowerCase();
                    Integer quantity = Integer.parseInt(cols[1].trim());
                    InventoryForm form = new InventoryForm();
                    form.setBarcode(barcode);
                    form.setQuantity(quantity);
                    inventoryForms.add(form);
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
}
