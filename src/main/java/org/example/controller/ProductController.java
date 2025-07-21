package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.example.model.constants.ApiEndpoints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.example.model.data.ProductData;
import org.example.model.form.ProductForm;
import org.example.dto.ProductDto;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import org.example.exception.ApiException;
import jakarta.validation.Valid;

@RestController
@RequestMapping(ApiEndpoints.Supervisor.PRODUCTS)
public class ProductController {

    @Autowired
    private ProductDto productDto;

    @PostMapping
    @org.springframework.transaction.annotation.Transactional
    public ProductData add(@RequestBody ProductForm form, Authentication authentication) {
        System.out.println("=== SUPERVISOR PRODUCT ADD ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            return productDto.add(form);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to add product: " + e.getMessage());
        }
    }

    @GetMapping("/id/{id}")
    public ProductData get(@PathVariable Integer id, Authentication authentication) {
        System.out.println("=== SUPERVISOR PRODUCT GET BY ID ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            return productDto.get(id);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get product: " + e.getMessage());
        }
    }

    @GetMapping("/barcode/{barcode}")
    public ProductData getByBarcode(@PathVariable String barcode, Authentication authentication) {
        System.out.println("=== SUPERVISOR PRODUCT GET BY BARCODE ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            return productDto.getByBarcode(barcode);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get product by barcode: " + e.getMessage());
        }
    }

    @GetMapping("/barcode/search/{barcode}")
    public List<ProductData> searchByBarcode(@PathVariable String barcode, Authentication authentication) {
        System.out.println("=== SUPERVISOR PRODUCT SEARCH BY BARCODE ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            return productDto.getByBarcodeLike(barcode);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to search products by barcode: " + e.getMessage());
        }
    }

    @GetMapping("/name/search/{name}")
    public List<ProductData> searchByName(@PathVariable String name, Authentication authentication) {
        System.out.println("=== SUPERVISOR PRODUCT SEARCH BY NAME ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            return productDto.getByNameLike(name);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to search products by name: " + e.getMessage());
        }
    }

    @GetMapping("/client/{clientId}")
    public List<ProductData> getByClientId(@PathVariable Integer clientId, Authentication authentication) {
        System.out.println("=== SUPERVISOR PRODUCT GET BY CLIENT ID ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            return productDto.getByClientId(clientId);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get products by client ID: " + e.getMessage());
        }
    }

    @GetMapping("/client/name/{clientName}")
    public List<ProductData> getByClientName(@PathVariable String clientName, Authentication authentication) {
        System.out.println("=== SUPERVISOR PRODUCT GET BY CLIENT NAME ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            return productDto.getByClientName(clientName);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get products by client name: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<ProductData>> getAllProducts(Authentication authentication) {
        System.out.println("=== SUPERVISOR PRODUCT GET ALL ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        List<ProductData> products = productDto.getAll();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/paginated")
    public ResponseEntity<org.example.model.data.PaginationResponse<ProductData>> getAllProductsPaginated(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            Authentication authentication) {
        System.out.println("=== SUPERVISOR PRODUCT GET ALL PAGINATED ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        System.out.println("Page: " + page + ", Size: " + size + ", SortBy: " + sortBy + ", SortDirection: " + sortDirection);
        
        org.example.model.form.PaginationRequest request = new org.example.model.form.PaginationRequest(page, size, sortBy, sortDirection);
        org.example.model.data.PaginationResponse<ProductData> response = productDto.getAllPaginated(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/name/search/{name}/paginated")
    public ResponseEntity<org.example.model.data.PaginationResponse<ProductData>> searchByNamePaginated(
            @PathVariable String name,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            Authentication authentication) {
        System.out.println("=== SUPERVISOR PRODUCT SEARCH BY NAME PAGINATED ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        System.out.println("Name: " + name + ", Page: " + page + ", Size: " + size);
        
        org.example.model.form.PaginationRequest request = new org.example.model.form.PaginationRequest(page, size, sortBy, sortDirection);
        org.example.model.data.PaginationResponse<ProductData> response = productDto.getByNameLikePaginated(name, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/client/{clientId}/paginated")
    public ResponseEntity<org.example.model.data.PaginationResponse<ProductData>> getByClientIdPaginated(
            @PathVariable Integer clientId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            Authentication authentication) {
        System.out.println("=== SUPERVISOR PRODUCT GET BY CLIENT ID PAGINATED ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        System.out.println("ClientId: " + clientId + ", Page: " + page + ", Size: " + size);
        
        org.example.model.form.PaginationRequest request = new org.example.model.form.PaginationRequest(page, size, sortBy, sortDirection);
        org.example.model.data.PaginationResponse<ProductData> response = productDto.getByClientIdPaginated(clientId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/client/name/{clientName}/paginated")
    public ResponseEntity<org.example.model.data.PaginationResponse<ProductData>> getByClientNamePaginated(
            @PathVariable String clientName,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            Authentication authentication) {
        System.out.println("=== SUPERVISOR PRODUCT GET BY CLIENT NAME PAGINATED ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        System.out.println("ClientName: " + clientName + ", Page: " + page + ", Size: " + size);
        
        org.example.model.form.PaginationRequest request = new org.example.model.form.PaginationRequest(page, size, sortBy, sortDirection);
        org.example.model.data.PaginationResponse<ProductData> response = productDto.getByClientNamePaginated(clientName, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<ProductData> updateProduct(@PathVariable Integer id, @Valid @RequestBody ProductForm form, Authentication authentication) {
        System.out.println("=== SUPERVISOR PRODUCT UPDATE ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        ProductData updatedProduct = productDto.update(id, form);
        return ResponseEntity.ok(updatedProduct);
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<Void> getProductImage(@PathVariable Integer id, Authentication authentication) {
        System.out.println("=== SUPERVISOR PRODUCT GET IMAGE ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
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
    public ResponseEntity<org.example.model.data.TsvUploadResult> uploadProductsFromTsv(@RequestParam("file") MultipartFile file, Authentication authentication) {
        System.out.println("=== SUPERVISOR PRODUCT UPLOAD TSV ENDPOINT ===");
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
            org.example.model.data.TsvUploadResult result = productDto.uploadProductsFromTsv(file);
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
