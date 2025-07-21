package org.example.controller;

import org.example.dto.OrderDto;
import org.example.exception.ApiException;
import org.example.model.constants.ApiEndpoints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.example.model.data.OrderData;
import org.example.model.form.OrderForm;

@RestController
@RequestMapping(ApiEndpoints.Supervisor.ORDERS)
public class OrderController {

    @Autowired
    private OrderDto orderDto;

    @PostMapping("/add")
    @org.springframework.transaction.annotation.Transactional
    public OrderData add(@RequestBody OrderForm form, Authentication authentication) {
        System.out.println("=== SUPERVISOR ORDER ADD ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            if (authentication != null && authentication.isAuthenticated()) {
                // Set the user ID from the authenticated user
                String userEmail = authentication.getName();
                form.setUserId(userEmail);
                
                return orderDto.add(form);
            } else {
                throw new ApiException("User not authenticated");
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to add order: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public OrderData get(@PathVariable Integer id, Authentication authentication) {
        System.out.println("=== SUPERVISOR ORDER GET ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            return orderDto.get(id);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get order: " + e.getMessage());
        }
    }

    @GetMapping
    public List<OrderData> getAll(Authentication authentication) {
        System.out.println("=== SUPERVISOR ORDER GET ALL ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            return orderDto.getAll();
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get all orders: " + e.getMessage());
        }
    }

    @GetMapping("/paginated")
    public ResponseEntity<org.example.model.data.PaginationResponse<OrderData>> getAllOrdersPaginated(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            Authentication authentication) {
        System.out.println("=== SUPERVISOR ORDER GET ALL PAGINATED ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        System.out.println("Page: " + page + ", Size: " + size + ", SortBy: " + sortBy + ", SortDirection: " + sortDirection);
        
        try {
            org.example.model.form.PaginationRequest request = new org.example.model.form.PaginationRequest(page, size, sortBy, sortDirection);
            org.example.model.data.PaginationResponse<OrderData> response = orderDto.getAllPaginated(request);
            return ResponseEntity.ok(response);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get all orders: " + e.getMessage());
        }
    }

    @GetMapping("/user/{userId}/paginated")
    public ResponseEntity<org.example.model.data.PaginationResponse<OrderData>> getOrdersByUserIdPaginated(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            Authentication authentication) {
        System.out.println("=== SUPERVISOR ORDER GET BY USER ID PAGINATED ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        System.out.println("UserId: " + userId + ", Page: " + page + ", Size: " + size);
        
        try {
            org.example.model.form.PaginationRequest request = new org.example.model.form.PaginationRequest(page, size, sortBy, sortDirection);
            org.example.model.data.PaginationResponse<OrderData> response = orderDto.getOrdersByUserIdPaginated(userId, request);
            return ResponseEntity.ok(response);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get orders by user ID: " + e.getMessage());
        }
    }

    @GetMapping("/date-range/paginated")
    public ResponseEntity<org.example.model.data.PaginationResponse<OrderData>> getOrdersByDateRangePaginated(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            Authentication authentication) {
        System.out.println("=== SUPERVISOR ORDER GET BY DATE RANGE PAGINATED ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        System.out.println("StartDate: " + startDate + ", EndDate: " + endDate + ", Page: " + page + ", Size: " + size);
        
        try {
            java.time.LocalDate start = java.time.LocalDate.parse(startDate);
            java.time.LocalDate end = java.time.LocalDate.parse(endDate);
            
            org.example.model.form.PaginationRequest request = new org.example.model.form.PaginationRequest(page, size, sortBy, sortDirection);
            org.example.model.data.PaginationResponse<OrderData> response = orderDto.getOrdersByDateRangePaginated(start, end, request);
            return ResponseEntity.ok(response);
        } catch (java.time.format.DateTimeParseException e) {
            throw new ApiException("Invalid date format. Use YYYY-MM-DD format");
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get orders by date range: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional
    public OrderData update(@PathVariable Integer id, @RequestBody OrderForm form, Authentication authentication) {
        System.out.println("=== SUPERVISOR ORDER UPDATE ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            return orderDto.update(id, form);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to update order: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}/cancel")
    @org.springframework.transaction.annotation.Transactional
    public void cancelOrder(@PathVariable Integer id, Authentication authentication) {
        System.out.println("=== SUPERVISOR ORDER CANCEL ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            orderDto.cancelOrder(id);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to cancel order: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/download-invoice")
    public ResponseEntity<org.springframework.core.io.Resource> downloadInvoice(@PathVariable Integer id, Authentication authentication) {
        System.out.println("=== SUPERVISOR ORDER DOWNLOAD INVOICE ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            org.springframework.core.io.Resource pdfResource = orderDto.downloadInvoice(id);
            String fileName = "order-" + id + ".pdf";
            
            return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdfResource);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException("Failed to download invoice: " + e.getMessage());
        }
    }

    /**
     * Get orders within a date range
     * @param startDate Start date in yyyy-MM-dd format (inclusive)
     * @param endDate End date in yyyy-MM-dd format (inclusive)
     * @return List of orders within the date range
     */
    @GetMapping("/by-date-range")
    public List<OrderData> getOrdersByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate,
            Authentication authentication) {
        System.out.println("=== SUPERVISOR ORDER BY DATE RANGE ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            java.time.LocalDate start = java.time.LocalDate.parse(startDate);
            java.time.LocalDate end = java.time.LocalDate.parse(endDate);
            return orderDto.getOrdersByDateRange(start, end);
        } catch (java.time.format.DateTimeParseException e) {
            throw new ApiException("Invalid date format. Please use yyyy-MM-dd format (e.g., 2024-01-15)");
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException("Failed to get orders by date range: " + e.getMessage());
        }
    }

    @GetMapping("/by-user")
    public List<OrderData> getOrdersByUserId(@RequestParam String userId, Authentication authentication) {
        System.out.println("=== SUPERVISOR ORDER BY USER ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            return orderDto.getOrdersByUserId(userId);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get orders by user ID: " + e.getMessage());
        }
    }

    // ========== SUBSTRING SEARCH ENDPOINTS ==========

    @GetMapping("/substring-id/{searchId}")
    public List<OrderData> findOrdersBySubstringId(
            @PathVariable String searchId,
            @RequestParam(defaultValue = "10") Integer maxResults,
            Authentication authentication) {
        System.out.println("=== SUPERVISOR ORDER SUBSTRING ID SEARCH ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        System.out.println("SearchId: " + searchId + ", MaxResults: " + maxResults);
        
        try {
            return orderDto.findOrdersBySubstringId(searchId, maxResults);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to find orders by substring ID: " + e.getMessage());
        }
    }

    @GetMapping("/substring-id/{searchId}/paginated")
    public ResponseEntity<org.example.model.data.PaginationResponse<OrderData>> findOrdersBySubstringIdPaginated(
            @PathVariable String searchId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            Authentication authentication) {
        System.out.println("=== SUPERVISOR ORDER SUBSTRING ID SEARCH PAGINATED ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        System.out.println("SearchId: " + searchId + ", Page: " + page + ", Size: " + size);
        
        try {
            org.example.model.form.PaginationRequest request = new org.example.model.form.PaginationRequest(page, size, sortBy, sortDirection);
            org.example.model.data.PaginationResponse<OrderData> response = orderDto.findOrdersBySubstringIdPaginated(searchId, request);
            return ResponseEntity.ok(response);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to find orders by substring ID: " + e.getMessage());
        }
    }

}
