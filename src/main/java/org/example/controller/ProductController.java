package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.example.model.ClientForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.example.model.ProductData;
import org.example.model.ProductForm;
import org.example.dto.ProductDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductDto productDto;

    @PostMapping
    public ProductData add(@RequestBody ProductForm form) {
        return productDto.add(form);
    }

    @GetMapping("/id/{id}")
    public ProductData get(@PathVariable Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        return productDto.get(id);
    }

    @GetMapping
    public List<ProductData> getAll() {
        return productDto.getAll();
    }

    @GetMapping("/barcode/{barcode}")
    public ProductData getByBarcode(@PathVariable String barcode) {
        if (barcode.trim().isEmpty()) {
            throw new IllegalArgumentException("Product Barcode cannot be null");
        }
        return productDto.getByBarcode(barcode);
    }

    @PutMapping("/{id}")
    public ProductData update(@PathVariable Integer id, @RequestBody ProductForm form) {
        if (id == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        return productDto.update(id, form);
    }

    @DeleteMapping
    public void deleteProduct(@RequestParam(required = false) Integer id,
                              @RequestParam(required = false) String name,
                              @RequestParam(required = false) String barcode) {
        if (id != null) {
            productDto.delete(id);
        } else if (name != null) {
            productDto.deleteByName(name);
        } else if (barcode != null) {
            productDto.deleteByBarcode(barcode);
        } else {
            throw new IllegalArgumentException("You must provide either 'id', 'name', or 'barcode' to delete a product.");
        }
    }

    @Operation(summary = "Upload products via TSV file")
    @PostMapping(value = "/upload-tsv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadProductsFromTsv(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty() || !file.getOriginalFilename().endsWith(".tsv")) {
            return ResponseEntity.badRequest().body("Please upload a valid non-empty .tsv file.");
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String header = reader.readLine(); // expecting: barcode, clientName, name, mrp, imageUrl
            if (header == null || !header.toLowerCase().contains("barcode")) {
                return ResponseEntity.badRequest().body("Missing or invalid header row. Expected 'barcode<TAB>clientName<TAB>name<TAB>mrp<TAB>imageUrl'");
            }

            int count = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                String[] cols = line.split("\t");
                if (cols.length < 4) continue;

                String barcode = cols[0].trim().toLowerCase();
                String clientName = cols[1].trim().toLowerCase();
                String name = cols[2].trim();
                String mrpStr = cols[3].trim();
                String imageUrl = cols.length >= 5 ? cols[4].trim() : "";

                if (barcode.isEmpty() || clientName.isEmpty() || name.isEmpty() || mrpStr.isEmpty()) continue;

                double mrp;
                try {
                    mrp = Double.parseDouble(mrpStr);
                } catch (NumberFormatException e) {
                    continue; // skip invalid lines
                }

                ProductForm form = new ProductForm();
                form.setBarcode(barcode);
                form.setClientName(clientName);
                form.setName(name);
                form.setMrp(mrp);
                form.setImageUrl(imageUrl);

                productDto.add(form); // assumes productDto resolves clientId internally
                count++;
            }

            return ResponseEntity.ok("Successfully uploaded " + count + " products.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error while processing file: " + e.getMessage());
        }
    }

}
