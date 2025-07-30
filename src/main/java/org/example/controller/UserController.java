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

        String userEmail = AuthHelper.getUserId();
        return orderDto.getOrdersByUserId(userEmail);
    }

    @GetMapping("/orders/paginated")
    public ResponseEntity<PaginationResponse<OrderData>> getMyOrdersPaginated(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        String userEmail = AuthHelper.getUserId();
        PaginationRequest request = new PaginationRequest(page, size, sortBy, sortDirection);
        PaginationResponse<OrderData> response = orderDto.getOrdersByUserIdPaginated(userEmail, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/orders/by-date-range")
    public List<OrderData> getMyOrdersByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {

        String userEmail = AuthHelper.getUserId();
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        return orderDto.getOrdersByDateRange(start, end);
    }

    @PostMapping("/orders")
    public OrderData createOrder(@RequestBody OrderForm form) {

        String userEmail = AuthHelper.getUserId();
        form.setUserId(userEmail);
        return orderDto.add(form);
    }

    @GetMapping("/orders/{id}")
    public OrderData getMyOrder(@PathVariable Integer id) {

        String userEmail = AuthHelper.getUserId();
        OrderData order = orderDto.get(id);
        // Check if the order belongs to the authenticated user
        if (!userEmail.equals(order.getUserId())) {
            throw new ApiException("Access denied. You can only view your own orders.");
        }
        return order;
    }

    @GetMapping("/orders/{id}/download-invoice")
    public ResponseEntity<org.springframework.core.io.Resource> downloadMyOrderInvoice(@PathVariable Integer id, Authentication authentication) {

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

        AuthHelper.getUserId(authentication); // Verify authentication
        return productDto.getByBarcode(barcode);
    }

    @GetMapping("/inventory/barcode/{barcode}")
    public InventoryData getInventoryByBarcode(@PathVariable String barcode, Authentication authentication) {

        AuthHelper.getUserId(authentication); // Verify authentication
        // Get product by barcode first, then get inventory by product ID
        var product = productApi.getByBarcode(barcode);
        if (product == null) {
            throw new ApiException("Product with barcode '" + barcode + "' not found");
        }
        return inventoryDto.getByProductId(product.getId());
    }

    @GetMapping("/search/barcode/{barcode}")
    public ResponseEntity<Object> searchByBarcode(@PathVariable String barcode, Authentication authentication) {

        AuthHelper.getUserId(authentication); // Verify authentication
        // Try to get product first
        ProductData product = null;
        InventoryData inventory = null;
        product = productDto.getByBarcode(barcode);
        var productPojo = productApi.getByBarcode(barcode);
        if (productPojo != null) {
            inventory = inventoryDto.getByProductId(productPojo.getId());
        }

        // Create response object
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("barcode", barcode);
        response.put("product", product);
        response.put("inventory", inventory);
        response.put("found", product != null || inventory != null);
        return ResponseEntity.ok(response);
    }
} 