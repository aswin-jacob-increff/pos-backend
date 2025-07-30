package org.example.controller;

import org.example.exception.ApiException;
import org.example.model.constants.ApiEndpoints;
import org.example.model.data.ProductData;
import org.example.model.form.ProductForm;
import org.example.model.data.PaginationResponse;
import org.example.model.form.PaginationRequest;
import org.example.model.form.PaginationQuery;
import org.example.dto.ProductDto;
import org.example.util.AuthHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(ApiEndpoints.Supervisor.PRODUCTS)
public class ProductController {

    @Autowired
    private ProductDto productDto;

    @PostMapping
    public ProductData add(@RequestBody ProductForm form) {
        return productDto.add(form);
    }

    @GetMapping("/{id}")
    public ProductData get(@PathVariable Integer id) {
        return productDto.get(id);
    }

    @GetMapping
    public List<ProductData> getAll() {
        return productDto.getAll();
    }

    // ========== GENERALIZED PAGINATION ENDPOINTS ==========

    @GetMapping("/paginated")
    public ResponseEntity<PaginationResponse<ProductData>> getAllProductsPaginated(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        PaginationRequest request = new PaginationRequest(page, size, sortBy, sortDirection);
        
        return PaginationControllerHelper.handlePaginatedRequest(
            "SUPERVISOR PRODUCT GET ALL PAGINATED ENDPOINT",
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
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        PaginationRequest request = new PaginationRequest(page, size, sortBy, sortDirection);
        
        return PaginationControllerHelper.handleFieldPaginatedRequest(
            "SUPERVISOR PRODUCT GET BY CLIENT ID PAGINATED ENDPOINT",
            "clientId",
            clientId,
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
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        PaginationRequest request = new PaginationRequest(page, size, sortBy, sortDirection);
        
        return PaginationControllerHelper.handleLikePaginatedRequest(
            "SUPERVISOR PRODUCT SEARCH BY NAME PAGINATED ENDPOINT",
            "name",
            name,
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
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        PaginationRequest request = new PaginationRequest(page, size, sortBy, sortDirection);
        PaginationResponse<ProductData> response = productDto.getPaginated(PaginationQuery.all(request));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ProductData update(@PathVariable Integer id, @RequestBody ProductForm form) {
        return productDto.update(id, form);
    }

    @GetMapping("/barcode/{barcode}")
    public ProductData getByBarcode(@PathVariable String barcode) {
        return productDto.getByBarcode(barcode);
    }

    @GetMapping("/name/search/{name}")
    public List<ProductData> searchByName(@PathVariable String name) {
        return productDto.getByNameLike(name);
    }

    @GetMapping("/client/{clientId}")
    public List<ProductData> getByClientId(@PathVariable Integer clientId) {
        return productDto.getByClientId(clientId);
    }

    @PostMapping(value = "/upload-tsv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<org.example.model.data.TsvUploadResult> uploadProductsFromTsv(@RequestParam("file") MultipartFile file) {

        
        try {
            org.example.model.data.TsvUploadResult result = productDto.uploadProductsFromTsv(file);

            
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

            e.printStackTrace();
            org.example.model.data.TsvUploadResult errorResult = new org.example.model.data.TsvUploadResult();
            errorResult.addError("Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
        }
    }
}
