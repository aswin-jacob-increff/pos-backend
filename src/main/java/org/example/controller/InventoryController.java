package org.example.controller;

import org.example.exception.ApiException;
import org.example.util.InventoryTsvParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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
@RequestMapping("/api/supervisor/inventory")
public class InventoryController {

    @Autowired
    private InventoryDto inventoryDto;

    @PostMapping
    @org.springframework.transaction.annotation.Transactional
    public InventoryData add(@RequestBody InventoryForm form, Authentication authentication) {
        System.out.println("=== SUPERVISOR INVENTORY ADD ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            return inventoryDto.add(form);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to add inventory: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public InventoryData get(@PathVariable Integer id, Authentication authentication) {
        System.out.println("=== SUPERVISOR INVENTORY GET ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            return inventoryDto.get(id);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get inventory: " + e.getMessage());
        }
    }

    @GetMapping
    public List<InventoryData> getAll(Authentication authentication) {
        System.out.println("=== SUPERVISOR INVENTORY GET ALL ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            return inventoryDto.getAll();
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get all inventory: " + e.getMessage());
        }
    }

    @GetMapping("/byProduct")
    public InventoryData getInventoryByAny(
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) String barcode,
            @RequestParam(required = false) String name,
            Authentication authentication
    ) {
        System.out.println("=== SUPERVISOR INVENTORY GET BY PRODUCT ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
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
    @org.springframework.transaction.annotation.Transactional
    public InventoryData update(@PathVariable Integer id, @RequestBody InventoryForm form, Authentication authentication) {
        System.out.println("=== SUPERVISOR INVENTORY UPDATE ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            return inventoryDto.update(id, form);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to update inventory: " + e.getMessage());
        }
    }

    @GetMapping("/product/{productBarcode}")
    public InventoryData getByProductBarcode(@PathVariable String productBarcode, Authentication authentication) {
        System.out.println("=== SUPERVISOR INVENTORY GET BY BARCODE ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            return inventoryDto.getByProductBarcode(productBarcode);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get inventory by product barcode: " + e.getMessage());
        }
    }
    
    @PutMapping("/{productId}/addStock")
    @org.springframework.transaction.annotation.Transactional
    public InventoryData addStock(
            @PathVariable String productId,
            @RequestParam Integer quantity,
            Authentication authentication
    ) {
        System.out.println("=== SUPERVISOR INVENTORY ADD STOCK ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        // Add validation and logging
        if (productId == null || productId.trim().isEmpty() || "null".equalsIgnoreCase(productId)) {
            throw new ApiException("Product barcode cannot be null or empty. Received: '" + productId + "'");
        }
        
        if (quantity == null) {
            throw new ApiException("Quantity cannot be null");
        }
        
        System.out.println("Adding stock for barcode: '" + productId + "', quantity: " + quantity);
        
        return inventoryDto.addStock(productId.trim(), quantity);
    }
    
    @PutMapping("/{productId}/removeStock")
    @org.springframework.transaction.annotation.Transactional
    public InventoryData removeStock(
            @PathVariable String productId,
            @RequestParam Integer quantity,
            Authentication authentication
    ) {
        System.out.println("=== SUPERVISOR INVENTORY REMOVE STOCK ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        // Add validation and logging
        if (productId == null || productId.trim().isEmpty() || "null".equalsIgnoreCase(productId)) {
            throw new ApiException("Product barcode cannot be null or empty. Received: '" + productId + "'");
        }
        
        if (quantity == null) {
            throw new ApiException("Quantity cannot be null");
        }
        
        System.out.println("Removing stock for barcode: '" + productId + "', quantity: " + quantity);
        
        return inventoryDto.removeStock(productId.trim(), quantity);
    }
    
    @PutMapping("/{productId}/setStock")
    @org.springframework.transaction.annotation.Transactional
    public InventoryData setStock(
            @PathVariable String productId,
            @RequestParam Integer quantity,
            Authentication authentication
    ) {
        System.out.println("=== SUPERVISOR INVENTORY SET STOCK ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        // Add validation and logging
        if (productId == null || productId.trim().isEmpty() || "null".equalsIgnoreCase(productId)) {
            throw new ApiException("Product barcode cannot be null or empty. Received: '" + productId + "'");
        }
        
        if (quantity == null) {
            throw new ApiException("Quantity cannot be null");
        }
        
        System.out.println("Setting stock for barcode: '" + productId + "', quantity: " + quantity);
        
        return inventoryDto.setStock(productId.trim(), quantity);
    }
    
    @GetMapping("/{productId}/image")
    public ResponseEntity<ByteArrayResource> getProductImage(@PathVariable String productId, Authentication authentication) {
        System.out.println("=== SUPERVISOR INVENTORY GET PRODUCT IMAGE ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            InventoryData inventory = inventoryDto.getByProductBarcode(productId);
            if (inventory.getImageUrl() == null || inventory.getImageUrl().trim().isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(302)
                    .header("Location", "/api/supervisor/products/" + productId + "/image")
                    .build();
                    
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping(value = "/upload-tsv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<String> uploadInventoryFromTsv(@RequestParam("file") MultipartFile file, Authentication authentication) {
        System.out.println("=== SUPERVISOR INVENTORY UPLOAD TSV ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        if (file == null || file.isEmpty() || !file.getOriginalFilename().endsWith(".tsv")) {
            throw new ApiException("Please upload a valid non-empty .tsv file.");
        }
        try {
            List<InventoryForm> forms = InventoryTsvParser.parse(file.getInputStream());
            if (forms.size() > 5000) {
                throw new ApiException("File upload limit exceeded: Maximum 5000 rows allowed.");
            }
            int count = 0;
            for (InventoryForm form : forms) {
                inventoryDto.add(form); // Use add(form) which uses barcode-based logic
                count++;
            }
            return ResponseEntity.ok("Successfully uploaded " + count + " inventory records.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid TSV format: " + e.getMessage());
        } catch (ApiException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process file: " + e.getMessage());
        }
    }

}
