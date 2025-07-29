package org.example.controller;

import org.example.dto.OrderDto;
import org.example.exception.ApiException;
import org.example.model.constants.ApiEndpoints;
import org.example.util.AuthHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.example.model.data.OrderData;
import org.example.model.data.OrderItemData;
import org.example.model.form.OrderForm;
import org.example.model.form.OrderItemForm;
import org.example.model.data.PaginationResponse;
import org.example.model.form.PaginationRequest;
import org.example.model.form.PaginationQuery;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping(ApiEndpoints.Supervisor.ORDERS)
public class OrderController {

    @Autowired
    private OrderDto orderDto;

    @PostMapping("/add")
    public OrderData add(@RequestBody OrderForm form) {

        
        try {
            // Set the user ID from the authenticated user
            String userEmail = AuthHelper.getUserId();
            form.setUserId(userEmail);
            
            return orderDto.add(form);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to add order: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public OrderData get(@PathVariable Integer id) {

        
        try {
            return orderDto.get(id);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get order: " + e.getMessage());
        }
    }

    @GetMapping
    public List<OrderData> getAll() {

        
        try {
            return orderDto.getAll();
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get all orders: " + e.getMessage());
        }
    }

    @GetMapping("/paginated")
    public ResponseEntity<PaginationResponse<OrderData>> getAllOrdersPaginated(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        
        try {
            PaginationRequest request = new PaginationRequest(page, size, sortBy, sortDirection);
            PaginationResponse<OrderData> response = orderDto.getPaginated(PaginationQuery.all(request));
            return ResponseEntity.ok(response);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get all orders: " + e.getMessage());
        }
    }

    @GetMapping("/user/{userId}/paginated")
    public ResponseEntity<PaginationResponse<OrderData>> getOrdersByUserIdPaginated(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        
        try {
            PaginationRequest request = new PaginationRequest(page, size, sortBy, sortDirection);
            PaginationResponse<OrderData> response = orderDto.getOrdersByUserIdPaginated(userId, request);
            return ResponseEntity.ok(response);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get orders by user ID: " + e.getMessage());
        }
    }

    @GetMapping("/date-range/paginated")
    public ResponseEntity<PaginationResponse<OrderData>> getOrdersByDateRangePaginated(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            
            PaginationRequest request = new PaginationRequest(page, size, sortBy, sortDirection);
            PaginationResponse<OrderData> response = orderDto.getOrdersByDateRangePaginated(start, end, request);
            return ResponseEntity.ok(response);
        } catch (DateTimeParseException e) {
            throw new ApiException("Invalid date format. Use YYYY-MM-DD format.");
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get orders by date range: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public OrderData update(@PathVariable Integer id, @RequestBody OrderForm form) {

        
        try {
            return orderDto.update(id, form);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to update order: " + e.getMessage());
        }
    }
    


    @GetMapping("/{id}/download-invoice")
    public ResponseEntity<org.springframework.core.io.Resource> downloadInvoice(@PathVariable Integer id) {

        
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
            @RequestParam String endDate) {

        
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            return orderDto.getOrdersByDateRange(start, end);
        } catch (DateTimeParseException e) {
            throw new ApiException("Invalid date format. Please use yyyy-MM-dd format (e.g., 2024-01-15)");
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException("Failed to get orders by date range: " + e.getMessage());
        }
    }

    @GetMapping("/by-user")
    public List<OrderData> getOrdersByUserId(@RequestParam String userId) {

        
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
            @RequestParam(defaultValue = "10") Integer maxResults) {

        
        try {
            return orderDto.findOrdersBySubstringId(searchId, maxResults);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to find orders by substring ID: " + e.getMessage());
        }
    }

    @GetMapping("/substring-id/{searchId}/paginated")
    public ResponseEntity<PaginationResponse<OrderData>> findOrdersBySubstringIdPaginated(
            @PathVariable String searchId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        
        try {
            PaginationRequest request = new PaginationRequest(page, size, sortBy, sortDirection);
            PaginationResponse<OrderData> response = orderDto.findOrdersBySubstringIdPaginated(searchId, request);
            return ResponseEntity.ok(response);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to find orders by substring ID: " + e.getMessage());
        }
    }

    // ========== ORDER ITEM ENDPOINTS ==========

    /**
     * Get order items for a specific order
     */
    @GetMapping("/{orderId}/items")
    public List<OrderItemData> getOrderItems(@PathVariable Integer orderId) {

        
        try {
            return orderDto.getOrderItemsByOrderId(orderId);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get order items: " + e.getMessage());
        }
    }

    /**
     * Add a new order item to an order
     */
    @PostMapping("/{orderId}/items")
    public OrderItemData addOrderItem(
            @PathVariable Integer orderId,
            @RequestBody OrderItemForm orderItemForm) {

        
        try {
            // Set the order ID from the path variable
            orderItemForm.setOrderId(orderId);
            return orderDto.addOrderItem(orderItemForm);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to add order item: " + e.getMessage());
        }
    }

    /**
     * Update an order item
     */
    @PutMapping("/items/{itemId}")
    public OrderItemData updateOrderItem(
            @PathVariable Integer itemId,
            @RequestBody OrderItemForm orderItemForm) {

        
        try {
            return orderDto.updateOrderItem(itemId, orderItemForm);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to update order item: " + e.getMessage());
        }
    }

    /**
     * Get a specific order item by ID
     */
    @GetMapping("/items/{itemId}")
    public OrderItemData getOrderItem(@PathVariable Integer itemId) {


        
        try {
            return orderDto.getOrderItem(itemId);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get order item: " + e.getMessage());
        }
    }

}
