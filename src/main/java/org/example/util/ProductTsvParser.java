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
            String header = reader.readLine(); // Expecting: clientName
            if (header == null || !header.toLowerCase().contains("productname")) {
                throw new IllegalArgumentException("Missing or invalid header: 'clientName'");
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String[] cols = line.split("\t");
                if (cols.length != 5) {
                    throw new ApiException("Wrong format for Product file");
                }
                if (cols.length < 1 || cols[0].trim().isEmpty()) continue;

                ProductForm form = new ProductForm();
                form.setBarcode(cols[0].trim().toLowerCase());
                form.setClientId(Integer.getInteger(cols[1].trim()));
                form.setName(cols[2].trim().toLowerCase());
                form.setMrp(Double.parseDouble(cols[3].trim()));
                form.setImage(cols[4]);
                products.add(form);
            }
        }

        return products;
    }
}
