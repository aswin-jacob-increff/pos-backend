package org.example.controller;

import org.example.exception.ApiException;
import org.example.util.InventoryTsvParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Base64;

import org.example.model.InventoryData;
import org.example.model.InventoryForm;
import org.example.dto.InventoryDto;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryDto inventoryDto;

    @PostMapping
    public InventoryData add(@RequestBody InventoryForm form) {
        return inventoryDto.add(form);
    }

    @GetMapping("/{id}")
    public InventoryData get(@PathVariable Integer id) {
        return inventoryDto.get(id);
    }

    @GetMapping
    public List<InventoryData> getAll() {
        return inventoryDto.getAll();
    }

    @GetMapping("/byProduct")
    public InventoryData getInventoryByAny(
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) String barcode,
            @RequestParam(required = false) String name
    ) {
        InventoryData inventoryData = new InventoryData();

        if (id != null) {
            // For now, we'll require barcode instead of product ID
            throw new ApiException("Please use product barcode instead of product ID for inventory lookup");
        } else if (barcode != null && !barcode.trim().isEmpty()) {
            inventoryData = inventoryDto.getByProductBarcode(barcode.trim());
        } else if (name != null && !name.trim().isEmpty()) {
            inventoryData = inventoryDto.getByProductName(name.trim());
        } else {
            throw new ApiException("Requires either of product name, product id or product barcode");
        }

        if (inventoryData == null) {
            throw new ApiException("Inventory not found for the provided input.");
        }

        return inventoryData;
    }

    @PutMapping("/{id}")
    public InventoryData update(@PathVariable Integer id, @RequestBody InventoryForm form) {
        return inventoryDto.update(id, form);
    }
    
    @PutMapping("/{productId}/addStock")
    public InventoryData addStock(
            @PathVariable String productId,
            @RequestParam Integer quantity
    ) {
        return inventoryDto.addStock(productId, quantity);
    }
    
    @PutMapping("/{productId}/removeStock")
    public InventoryData removeStock(
            @PathVariable String productId,
            @RequestParam Integer quantity
    ) {
        return inventoryDto.removeStock(productId, quantity);
    }
    
    @PutMapping("/{productId}/setStock")
    public InventoryData setStock(
            @PathVariable String productId,
            @RequestParam Integer quantity
    ) {
        return inventoryDto.setStock(productId, quantity);
    }
    
    @GetMapping("/{productId}/image")
    public ResponseEntity<ByteArrayResource> getProductImage(@PathVariable String productId) {
        
        try {
            InventoryData inventory = inventoryDto.getByProductBarcode(productId);
            if (inventory.getImageUrl() == null || inventory.getImageUrl().trim().isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Extract the actual base64 data from the product
            // We need to get the product's base64 data, not the URL
            // This is a bit complex since we're going through inventory
            // For now, we'll redirect to the product image endpoint
            return ResponseEntity.status(302)
                    .header("Location", "/api/products/" + productId + "/image")
                    .build();
                    
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping(value = "/upload-tsv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadInventoryFromTsv(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty() || !file.getOriginalFilename().endsWith(".tsv")) {
            throw new ApiException("Please upload a valid non-empty .tsv file.");
        }
        try {
            List<InventoryForm> forms = InventoryTsvParser.parse(file.getInputStream());
            if (forms.size() > 5000) {
                throw new ApiException("File upload limit exceeded: Maximum 5000 rows allowed.");
            }
            // Only add if all are valid
            int count = 0;
            for (InventoryForm form : forms) {
                inventoryDto.add(form); // Use add(form) which uses barcode-based logic
                count++;
            }
            return ResponseEntity.ok("Successfully uploaded " + count + " inventory records.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid TSV format: " + e.getMessage());
        } catch (ApiException e) {
            // Propagate parser validation errors
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process file: " + e.getMessage());
        }
    }

}
