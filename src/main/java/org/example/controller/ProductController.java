package org.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.example.model.ProductData;
import org.example.model.ProductForm;
import org.example.dto.ProductDto;
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

}
