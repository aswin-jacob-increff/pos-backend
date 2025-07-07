package org.example.util;

import org.example.exception.ApiException;
import org.example.model.ProductForm;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ProductTsvParser {
    public static List<ProductForm> parse(InputStream inputStream) throws Exception {
        List<ProductForm> products = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String header = reader.readLine(); // Expecting: barcode, clientId, productname, mrp, imageUrl (optional)
            if (header == null || !header.toLowerCase().contains("productname")) {
                throw new IllegalArgumentException("Missing or invalid header: Expected 'barcode<TAB>clientId<TAB>productname<TAB>mrp<TAB>imageUrl' (imageUrl is optional)");
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String[] cols = line.split("\t");
                if (cols.length < 4 || cols.length > 5) {
                    throw new ApiException("Wrong format for Product file. Expected 4-5 columns: barcode, clientId, productname, mrp, imageUrl(optional)");
                }
                if (cols[0].trim().isEmpty()) continue;

                ProductForm form = new ProductForm();
                form.setBarcode(cols[0].trim().toLowerCase());
                form.setClientId(Integer.parseInt(cols[1].trim()));
                form.setName(cols[2].trim().toLowerCase());
                form.setMrp(Double.parseDouble(cols[3].trim()));
                
                // Set image URL if provided (5th column)
                if (cols.length == 5 && cols[4] != null && !cols[4].trim().isEmpty()) {
                    form.setImage(cols[4].trim());
                }
                
                products.add(form);
            }
        }

        return products;
    }
}
