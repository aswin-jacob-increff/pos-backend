package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.model.ProductData;
import org.example.model.ProductForm;
import org.example.dto.ProductDto;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import org.example.exception.ApiException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductDto productDto;

    @PostMapping
    @org.springframework.transaction.annotation.Transactional
    public ProductData add(@RequestBody ProductForm form) {
        try {
            return productDto.add(form);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to add product: " + e.getMessage());
        }
    }

    @GetMapping("/id/{id}")
    public ProductData get(@PathVariable Integer id) {
        try {
            return productDto.get(id);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get product: " + e.getMessage());
        }
    }

    @GetMapping("/barcode/{barcode}")
    public ProductData getByBarcode(@PathVariable String barcode) {
        try {
            return productDto.getByBarcode(barcode);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get product by barcode: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<ProductData>> getAllProducts() {
        List<ProductData> products = productDto.getAll();
        return ResponseEntity.ok(products);
    }

    @PutMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<ProductData> updateProduct(@PathVariable Integer id, @Valid @RequestBody ProductForm form) {
        ProductData updatedProduct = productDto.update(id, form);
        return ResponseEntity.ok(updatedProduct);
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<Void> getProductImage(@PathVariable Integer id) {
        try {
            String imageUrl = productDto.getProductImageUrl(id);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", imageUrl)
                    .build();
        } catch (ApiException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Upload products via TSV file")
    @PostMapping(value = "/upload-tsv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<String> uploadProductsFromTsv(@RequestParam("file") MultipartFile file) {
        try {
            String result = productDto.uploadProductsFromTsv(file);
            return ResponseEntity.ok(result);
        } catch (ApiException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error while processing file: " + e.getMessage());
        }
    }




}
