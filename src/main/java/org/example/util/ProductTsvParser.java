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
        List<String> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String header = reader.readLine(); // Expecting: barcode, clientname, productname, mrp, imageurl (optional)
            if (header == null || !header.toLowerCase().contains("barcode") || !header.toLowerCase().contains("clientname") || !header.toLowerCase().contains("productname") || !header.toLowerCase().contains("mrp")) {
                throw new IllegalArgumentException("Missing or invalid header: Expected 'barcode<TAB>clientname<TAB>productname<TAB>mrp<TAB>imageurl' (imageurl is optional)");
            }

            String line;
            int rowNum = 2; // 1-based, header is row 1
            while ((line = reader.readLine()) != null) {
                String[] cols = line.split("\t");
                if (cols.length < 4 || cols.length > 5) {
                    errors.add("Row " + rowNum + ": Wrong format. Expected 4-5 columns: barcode, clientname, productname, mrp, imageurl(optional)");
                    rowNum++;
                    continue;
                }
                if (cols[0].trim().isEmpty()) {
                    rowNum++;
                    continue;
                }
                try {
                    ProductForm form = new ProductForm();
                    form.setBarcode(cols[0].trim().toLowerCase());
                    form.setClientName(cols[1].trim().toLowerCase());
                    form.setName(cols[2].trim().toLowerCase());
                    form.setMrp(Double.parseDouble(cols[3].trim()));
                    // Set image URL if provided (5th column)
                    if (cols.length == 5 && cols[4] != null && !cols[4].trim().isEmpty()) {
                        form.setImage(cols[4].trim());
                    }
                    products.add(form);
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
}
