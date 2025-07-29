package org.example.controller;

import org.example.dto.OrderDto;
import org.example.dto.UserDto;
import org.example.dto.ProductDto;
import org.example.dto.InventoryDto;
import org.example.exception.ApiException;
import org.example.model.constants.ApiEndpoints;
import org.example.model.data.OrderData;
import org.example.model.data.OrderItemData;
import org.example.model.form.OrderForm;
import org.example.model.form.OrderItemForm;
import org.example.model.data.UserData;
import org.example.model.form.UserForm;
import org.example.model.data.ProductData;
import org.example.model.data.InventoryData;
import org.example.api.ProductApi;
import org.example.api.UserApi;
import org.example.pojo.UserPojo;
import org.example.model.data.PaginationResponse;
import org.example.model.form.PaginationRequest;
import org.example.util.AuthHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping(ApiEndpoints.User.BASE_PATH)
public class UserController {

    @Autowired
    private OrderDto orderDto;

    @Autowired
    private UserDto userDto;

    @Autowired
    private ProductDto productDto;

    @Autowired
    private InventoryDto inventoryDto;

    @Autowired
    private ProductApi productApi;

    @Autowired
    private UserApi userApi;

    @GetMapping("/orders")
    public List<OrderData> getMyOrders() {

        
        try {
            String userEmail = AuthHelper.getUserId();
            return orderDto.getOrdersByUserId(userEmail);
        } catch (Exception e) {
            throw new ApiException("Failed to get user orders: " + e.getMessage());
        }
    }

    @GetMapping("/orders/paginated")
    public ResponseEntity<PaginationResponse<OrderData>> getMyOrdersPaginated(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        
        try {
            String userEmail = AuthHelper.getUserId();
            PaginationRequest request = new PaginationRequest(page, size, sortBy, sortDirection);
            PaginationResponse<OrderData> response = orderDto.getOrdersByUserIdPaginated(userEmail, request);
            return ResponseEntity.ok(response);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get user orders: " + e.getMessage());
        }
    }

    @GetMapping("/orders/by-date-range")
    public List<OrderData> getMyOrdersByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            String userEmail = AuthHelper.getUserId();
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            return orderDto.getOrdersByDateRange(start, end);
        } catch (DateTimeParseException e) {
            throw new ApiException("Invalid date format. Use YYYY-MM-DD format.");
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get orders by date range: " + e.getMessage());
        }
    }

    @PostMapping("/orders")
    public OrderData createOrder(@RequestBody OrderForm form) {

        
        try {
            // Set the user ID from the authenticated user
            String userEmail = AuthHelper.getUserId();
            form.setUserId(userEmail);
            
            return orderDto.add(form);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to create order: " + e.getMessage());
        }
    }

    @GetMapping("/orders/{id}")
    public OrderData getMyOrder(@PathVariable Integer id) {
        try {
            String userEmail = AuthHelper.getUserId();
            OrderData order = orderDto.get(id);
            // Check if the order belongs to the authenticated user
            if (!userEmail.equals(order.getUserId())) {
                throw new ApiException("Access denied. You can only view your own orders.");
            }
            return order;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get order: " + e.getMessage());
        }
    }

    @GetMapping("/orders/{id}/download-invoice")
    public ResponseEntity<org.springframework.core.io.Resource> downloadMyOrderInvoice(@PathVariable Integer id, Authentication authentication) {

        
        try {
            if (authentication != null && authentication.isAuthenticated()) {
                String userEmail = authentication.getName();
                OrderData existingOrder = orderDto.get(id);
                
                // Check if the order belongs to the authenticated user
                if (!userEmail.equals(existingOrder.getUserId())) {
                    throw new ApiException("Access denied. You can only download invoices for your own orders.");
                }
                
                org.springframework.core.io.Resource pdfResource = orderDto.downloadInvoice(id);
                String fileName = "order-" + id + ".pdf";
                
                return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                    .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                    .body(pdfResource);
            } else {
                throw new ApiException("User not authenticated");
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to download invoice: " + e.getMessage());
        }
    }

    @GetMapping("/current-user")
    public ResponseEntity<UserData> getCurrentUser(Authentication authentication) {
        try {
            if (authentication != null && authentication.isAuthenticated()) {
                String userEmail = authentication.getName();
                UserData userData = userDto.getUserByEmail(userEmail);
                return ResponseEntity.ok(userData);
            } else {
                throw new ApiException("User not authenticated");
            }
        } catch (Exception e) {
            throw new ApiException("Failed to get current user: " + e.getMessage());
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody UserForm form) {
        try {
            // Validate input
            if (form.getEmail() == null || form.getEmail().trim().isEmpty()) {
                throw new ApiException("Email is required");
            }
            if (form.getPassword() == null || form.getPassword().trim().isEmpty()) {
                throw new ApiException("Password is required");
            }
            if (form.getPassword().length() < 6) {
                throw new ApiException("Password must be at least 6 characters long");
            }
            // Role validation removed - automatically set to USER
            
            userDto.signup(form);
            return ResponseEntity.ok("User registered successfully");
        } catch (ApiException e) {
            throw e;
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new ApiException("User with this email already exists");
        } catch (Exception e) {
            throw new ApiException("Signup failed: " + e.getMessage());
        }
    }





    @GetMapping("/products/barcode/{barcode}")
    public ProductData getProductByBarcode(@PathVariable String barcode, Authentication authentication) {

        
        try {
            AuthHelper.getUserId(authentication); // Verify authentication
            return productDto.getByBarcode(barcode);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get product by barcode: " + e.getMessage());
        }
    }

    @GetMapping("/inventory/barcode/{barcode}")
    public InventoryData getInventoryByBarcode(@PathVariable String barcode, Authentication authentication) {

        
        try {
            AuthHelper.getUserId(authentication); // Verify authentication
            // Get product by barcode first, then get inventory by product ID
            var product = productApi.getByBarcode(barcode);
            if (product == null) {
                throw new ApiException("Product with barcode '" + barcode + "' not found");
            }
            return inventoryDto.getByProductId(product.getId());
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get inventory by barcode: " + e.getMessage());
        }
    }

    @GetMapping("/search/barcode/{barcode}")
    public ResponseEntity<Object> searchByBarcode(@PathVariable String barcode, Authentication authentication) {

        
        try {
            AuthHelper.getUserId(authentication); // Verify authentication
            // Try to get product first
            ProductData product = null;
            InventoryData inventory = null;
                
                try {
                    product = productDto.getByBarcode(barcode);
                } catch (Exception e) {
                    // Product not found, continue to inventory
                }
                
                try {
                    // Get product by barcode first, then get inventory by product ID
                    var productPojo = productApi.getByBarcode(barcode);
                    if (productPojo != null) {
                        inventory = inventoryDto.getByProductId(productPojo.getId());
                    }
                } catch (Exception e) {
                    // Inventory not found
                }
                
                // Create response object
                java.util.Map<String, Object> response = new java.util.HashMap<>();
                response.put("barcode", barcode);
                response.put("product", product);
                response.put("inventory", inventory);
                response.put("found", product != null || inventory != null);
                
                return ResponseEntity.ok(response);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to search by barcode: " + e.getMessage());
        }
    }

    // ========== ORDER ITEM ENDPOINTS ==========

    /**
     * Get order items for a specific order (user access)
     */
    @GetMapping("/orders/{orderId}/items")
    public List<OrderItemData> getMyOrderItems(@PathVariable Integer orderId, Authentication authentication) {

        
        try {
            // Verify the order belongs to the authenticated user
            String userEmail = AuthHelper.getUserId(authentication);
            OrderData order = orderDto.get(orderId);
            if (!order.getUserId().equals(userEmail)) {
                throw new ApiException("Access denied: Order does not belong to user");
            }
            return orderDto.getOrderItemsByOrderId(orderId);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get order items: " + e.getMessage());
        }
    }

    /**
     * Add a new order item to an order (user access)
     */
    @PostMapping("/orders/{orderId}/items")
    public OrderItemData addMyOrderItem(
            @PathVariable Integer orderId,
            @RequestBody OrderItemForm orderItemForm,
            Authentication authentication) {

        
        try {
            // Verify the order belongs to the authenticated user
            String userEmail = AuthHelper.getUserId(authentication);
            OrderData order = orderDto.get(orderId);
            if (!order.getUserId().equals(userEmail)) {
                throw new ApiException("Access denied: Order does not belong to user");
            }
            
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
     * Update an order item (user access)
     */
    @PutMapping("/orders/items/{itemId}")
    public OrderItemData updateMyOrderItem(
            @PathVariable Integer itemId,
            @RequestBody OrderItemForm orderItemForm,
            Authentication authentication) {

        
        try {
            // Verify the order item belongs to the authenticated user
            String userEmail = AuthHelper.getUserId(authentication);
            OrderItemData orderItem = orderDto.getOrderItem(itemId);
            OrderData order = orderDto.get(orderItem.getOrderId());
            if (!order.getUserId().equals(userEmail)) {
                throw new ApiException("Access denied: Order item does not belong to user");
            }
            
            return orderDto.updateOrderItem(itemId, orderItemForm);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to update order item: " + e.getMessage());
        }
    }

    /**
     * Get a specific order item by ID (user access)
     */
    @GetMapping("/orders/items/{itemId}")
    public OrderItemData getMyOrderItem(@PathVariable Integer itemId, Authentication authentication) {

        
        try {
            // Verify the order item belongs to the authenticated user
            String userEmail = AuthHelper.getUserId(authentication);
            OrderItemData orderItem = orderDto.getOrderItem(itemId);
            OrderData order = orderDto.get(orderItem.getOrderId());
            if (!order.getUserId().equals(userEmail)) {
                throw new ApiException("Access denied: Order item does not belong to user");
            }
            
            return orderDto.getOrderItem(itemId);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get order item: " + e.getMessage());
        }
    }

} 