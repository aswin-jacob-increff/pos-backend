package org.example.controller;

import org.example.exception.ApiException;
import org.example.model.constants.ApiEndpoints;
import org.example.util.InventoryTsvParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.ByteArrayResource;

import java.util.List;

import org.example.model.data.InventoryData;
import org.example.model.form.InventoryForm;
import org.example.dto.InventoryDto;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(ApiEndpoints.Supervisor.INVENTORY)
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

    @GetMapping("/paginated")
    public ResponseEntity<org.example.model.data.PaginationResponse<InventoryData>> getAllInventoryPaginated(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            Authentication authentication) {
        System.out.println("=== SUPERVISOR INVENTORY GET ALL PAGINATED ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        System.out.println("Page: " + page + ", Size: " + size + ", SortBy: " + sortBy + ", SortDirection: " + sortDirection);
        
        try {
            org.example.model.form.PaginationRequest request = new org.example.model.form.PaginationRequest(page, size, sortBy, sortDirection);
            org.example.model.data.PaginationResponse<InventoryData> response = inventoryDto.getAllPaginated(request);
            return ResponseEntity.ok(response);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get all inventory: " + e.getMessage());
        }
    }

    @GetMapping("/product/name/{productName}/paginated")
    public ResponseEntity<org.example.model.data.PaginationResponse<InventoryData>> getByProductNamePaginated(
            @PathVariable String productName,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            Authentication authentication) {
        System.out.println("=== SUPERVISOR INVENTORY GET BY PRODUCT NAME PAGINATED ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        System.out.println("ProductName: " + productName + ", Page: " + page + ", Size: " + size);
        
        try {
            org.example.model.form.PaginationRequest request = new org.example.model.form.PaginationRequest(page, size, sortBy, sortDirection);
            org.example.model.data.PaginationResponse<InventoryData> response = inventoryDto.getByProductNamePaginated(productName, request);
            return ResponseEntity.ok(response);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get inventory by product name: " + e.getMessage());
        }
    }

    @GetMapping("/product/barcode/{barcode}/paginated")
    public ResponseEntity<org.example.model.data.PaginationResponse<InventoryData>> getByProductBarcodePaginated(
            @PathVariable String barcode,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            Authentication authentication) {
        System.out.println("=== SUPERVISOR INVENTORY GET BY PRODUCT BARCODE PAGINATED ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        System.out.println("Barcode: " + barcode + ", Page: " + page + ", Size: " + size);
        
        try {
            org.example.model.form.PaginationRequest request = new org.example.model.form.PaginationRequest(page, size, sortBy, sortDirection);
            org.example.model.data.PaginationResponse<InventoryData> response = inventoryDto.getByProductBarcodePaginated(barcode, request);
            return ResponseEntity.ok(response);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get inventory by product barcode: " + e.getMessage());
        }
    }

    @GetMapping("/byProduct")
    public List<InventoryData> getInventoryByAny(
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) String barcode,
            @RequestParam(required = false) String name,
            Authentication authentication
    ) {
        System.out.println("=== SUPERVISOR INVENTORY GET BY PRODUCT ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        List<InventoryData> inventoryDataList;

        if (id != null) {
            // For now, we'll require barcode instead of product ID
            throw new ApiException("Please use product barcode instead of product ID for inventory lookup");
        } else if (barcode != null && !barcode.trim().isEmpty()) {
            inventoryDataList = inventoryDto.getByProductBarcodeLike(barcode.trim());
        } else if (name != null && !name.trim().isEmpty()) {
            inventoryDataList = inventoryDto.getByProductNameLike(name.trim());
        } else {
            throw new ApiException("Requires either of product name, product id or product barcode");
        }

        if (inventoryDataList == null || inventoryDataList.isEmpty()) {
            throw new ApiException("No inventory found for the provided input.");
        }

        return inventoryDataList;
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

    @GetMapping("/product/barcode/search/{productBarcode}")
    public List<InventoryData> searchByProductBarcode(@PathVariable String productBarcode, Authentication authentication) {
        System.out.println("=== SUPERVISOR INVENTORY SEARCH BY PRODUCT BARCODE ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            return inventoryDto.getByProductBarcodeLike(productBarcode);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to search inventory by product barcode: " + e.getMessage());
        }
    }

    @GetMapping("/product/name/search/{productName}")
    public List<InventoryData> searchByProductName(@PathVariable String productName, Authentication authentication) {
        System.out.println("=== SUPERVISOR INVENTORY SEARCH BY PRODUCT NAME ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            return inventoryDto.getByProductNameLike(productName);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to search inventory by product name: " + e.getMessage());
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
    public ResponseEntity<org.example.model.data.TsvUploadResult> uploadInventoryFromTsv(@RequestParam("file") MultipartFile file, Authentication authentication) {
        System.out.println("=== SUPERVISOR INVENTORY UPLOAD TSV ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        System.out.println("File received: " + (file != null ? "YES" : "NO"));
        if (file != null) {
            System.out.println("File name: " + file.getOriginalFilename());
            System.out.println("File size: " + file.getSize());
            System.out.println("File content type: " + file.getContentType());
            System.out.println("File is empty: " + file.isEmpty());
        }
        
        try {
            org.example.model.data.TsvUploadResult result = inventoryDto.uploadInventoryFromTsv(file);
            System.out.println("Upload result summary: " + result.getSummary());
            System.out.println("Successful rows: " + result.getSuccessfulRows());
            System.out.println("Failed rows: " + result.getFailedRows());
            System.out.println("Errors: " + result.getErrors());
            System.out.println("Warnings: " + result.getWarnings());
            
            // Return appropriate status based on the result
            if (result.hasErrors()) {
                // If there are validation errors, return 400 Bad Request
                return ResponseEntity.badRequest().body(result);
            } else if (result.getSuccessfulRows() == 0) {
                // If no successful rows, return 400 Bad Request
                return ResponseEntity.badRequest().body(result);
            } else {
                // If there are successful rows, return 200 OK
                return ResponseEntity.ok(result);
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
            org.example.model.data.TsvUploadResult errorResult = new org.example.model.data.TsvUploadResult();
            errorResult.addError("Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
        }
    }



}
