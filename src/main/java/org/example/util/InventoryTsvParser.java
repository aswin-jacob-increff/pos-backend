package org.example.util;

import org.example.exception.ApiException;
import org.example.model.InventoryForm;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class InventoryTsvParser {

    public static List<InventoryForm> parse(InputStream inputStream) throws Exception {
        List<InventoryForm> inventoryForms = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String header = reader.readLine(); // Expecting: productId<TAB>quantity<TAB>mrp
            if (header == null || !header.toLowerCase().contains("productid")) {
                throw new IllegalArgumentException("Missing or invalid header: Expected 'productId<TAB>quantity<TAB>mrp'");
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String[] cols = line.split("\t");
                if (cols.length != 3) {
                    throw new ApiException("Wrong format for Inventory file");
                }

                try {
                    Integer productId = Integer.parseInt(cols[0].trim());
                    Integer quantity = Integer.parseInt(cols[1].trim());
                    Double mrp = Double.parseDouble(cols[2].trim());

                    InventoryForm form = new InventoryForm();
                    form.setProductId(productId);
                    form.setQuantity(quantity);
                    form.setMrp(mrp);

                    inventoryForms.add(form);
                } catch (NumberFormatException e) {
                    // Skip invalid lines silently
                    continue;
                }
            }
        }

        return inventoryForms;
    }
}
