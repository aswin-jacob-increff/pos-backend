package org.example.controller;

import org.example.exception.ApiException;
import org.example.model.constants.ApiEndpoints;
import org.example.model.data.ProductData;
import org.example.model.form.ProductForm;
import org.example.model.data.PaginationResponse;
import org.example.model.form.PaginationRequest;
import org.example.model.form.PaginationQuery;
import org.example.dto.ProductDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiEndpoints.Supervisor.PRODUCTS)
public class ProductController {

    @Autowired
    private ProductDto productDto;

    @PostMapping
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

    @GetMapping("/{id}")
    public ProductData get(@PathVariable Integer id, Authentication authentication) {
        System.out.println("=== SUPERVISOR PRODUCT GET ENDPOINT ===");
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

    @GetMapping
    public List<ProductData> getAll(Authentication authentication) {
        System.out.println("=== SUPERVISOR PRODUCT GET ALL ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            return productDto.getAll();
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get all products: " + e.getMessage());
        }
    }

    // ========== GENERALIZED PAGINATION ENDPOINTS ==========

    @GetMapping("/paginated")
    public ResponseEntity<PaginationResponse<ProductData>> getAllProductsPaginated(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            Authentication authentication) {
        
        PaginationRequest request = new PaginationRequest(page, size, sortBy, sortDirection);
        
        return PaginationControllerHelper.handlePaginatedRequest(
            "SUPERVISOR PRODUCT GET ALL PAGINATED ENDPOINT",
            authentication,
            request,
            (req) -> productDto.getPaginated(PaginationQuery.all(req))
        );
    }

    @GetMapping("/client/{clientId}/paginated")
    public ResponseEntity<PaginationResponse<ProductData>> getProductsByClientIdPaginated(
            @PathVariable Integer clientId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            Authentication authentication) {
        
        PaginationRequest request = new PaginationRequest(page, size, sortBy, sortDirection);
        
        return PaginationControllerHelper.handleFieldPaginatedRequest(
            "SUPERVISOR PRODUCT GET BY CLIENT ID PAGINATED ENDPOINT",
            "clientId",
            clientId,
            authentication,
            request,
            productDto::getPaginated
        );
    }

    @GetMapping("/name/search/{name}/paginated")
    public ResponseEntity<PaginationResponse<ProductData>> searchProductsByNamePaginated(
            @PathVariable String name,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            Authentication authentication) {
        
        PaginationRequest request = new PaginationRequest(page, size, sortBy, sortDirection);
        
        return PaginationControllerHelper.handleLikePaginatedRequest(
            "SUPERVISOR PRODUCT SEARCH BY NAME PAGINATED ENDPOINT",
            "name",
            name,
            authentication,
            request,
            productDto::getPaginated
        );
    }

    // ========== LEGACY PAGINATION ENDPOINTS (for backward compatibility) ==========

    @GetMapping("/paginated/legacy")
    public ResponseEntity<PaginationResponse<ProductData>> getAllProductsPaginatedLegacy(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            Authentication authentication) {
        System.out.println("=== SUPERVISOR PRODUCT GET ALL PAGINATED ENDPOINT (LEGACY) ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        System.out.println("Page: " + page + ", Size: " + size + ", SortBy: " + sortBy + ", SortDirection: " + sortDirection);
        
        try {
            PaginationRequest request = new PaginationRequest(page, size, sortBy, sortDirection);
            PaginationResponse<ProductData> response = productDto.getPaginated(PaginationQuery.all(request));
            return ResponseEntity.ok(response);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get all products: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ProductData update(@PathVariable Integer id, @RequestBody ProductForm form, Authentication authentication) {
        System.out.println("=== SUPERVISOR PRODUCT UPDATE ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            return productDto.update(id, form);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to update product: " + e.getMessage());
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
}
