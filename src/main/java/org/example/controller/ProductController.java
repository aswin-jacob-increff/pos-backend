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

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductDto productDto;

    @PostMapping
    @org.springframework.transaction.annotation.Transactional
    public ProductData add(@RequestBody ProductForm form) {
        return productDto.add(form);
    }

    @GetMapping("/id/{id}")
    public ProductData get(@PathVariable Integer id) {
        return productDto.get(id);
    }

    @GetMapping
    public List<ProductData> getAll() {
        return productDto.getAll();
    }

    @GetMapping("/barcode/{barcode}")
    public ProductData getByBarcode(@PathVariable String barcode) {
        return productDto.getByBarcode(barcode);
    }

    @PutMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional
    public ProductData update(@PathVariable Integer id, @RequestBody ProductForm form) {
        return productDto.update(id, form);
    }

    @DeleteMapping
    @org.springframework.transaction.annotation.Transactional
    public void deleteProduct(@RequestParam(required = false) Integer id,
                              @RequestParam(required = false) String name,
                              @RequestParam(required = false) String barcode) {
        productDto.deleteProduct(id, name, barcode);
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
