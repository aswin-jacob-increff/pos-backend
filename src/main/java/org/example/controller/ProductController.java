package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.example.model.ClientForm;
import org.example.util.ProductTsvParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.example.model.ProductData;
import org.example.model.ProductForm;
import org.example.dto.ProductDto;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Base64;
import org.example.exception.ApiException;

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

    @GetMapping("/{id}/image")
    public ResponseEntity<ByteArrayResource> getProductImage(@PathVariable Integer id) {
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            ProductData product = productDto.get(id);
            if (product.getImageUrl() == null || product.getImageUrl().trim().isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Convert base64 to byte array
            byte[] imageBytes = Base64.getDecoder().decode(product.getImageUrl());
            ByteArrayResource resource = new ByteArrayResource(imageBytes);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG); // Adjust based on actual image type
            headers.setContentLength(imageBytes.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
                    
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Upload products via TSV file")
    @PostMapping(value = "/upload-tsv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadProductsFromTsv(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty() || !file.getOriginalFilename().endsWith(".tsv")) {
            throw new ApiException("Please upload a valid non-empty .tsv file.");
        }
        try {
            List<ProductForm> productForms = ProductTsvParser.parse(file.getInputStream());
            if (productForms.size() > 5000) {
                throw new ApiException("File upload limit exceeded: Maximum 5000 rows allowed.");
            }
            int count = 0;
            for (ProductForm form : productForms) {
                productDto.add(form); // assumes clientDto resolves clientName → clientId
                count++;
            }
            return ResponseEntity.ok("Successfully uploaded " + count + " products.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid TSV: " + e.getMessage());
        } catch (ApiException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error while processing file: " + e.getMessage());
        }
    }
}
