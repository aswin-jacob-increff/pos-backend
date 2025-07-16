package org.example.controller;

import org.example.dto.OrderDto;
import org.example.dto.UserDto;
import org.example.dto.ProductDto;
import org.example.dto.InventoryDto;
import org.example.exception.ApiException;
import org.example.model.OrderData;
import org.example.model.OrderForm;
import org.example.model.UserData;
import org.example.model.UserForm;
import org.example.model.ProductData;
import org.example.model.InventoryData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private OrderDto orderDto;

    @Autowired
    private UserDto userDto;

    @Autowired
    private ProductDto productDto;

    @Autowired
    private InventoryDto inventoryDto;

    @GetMapping("/orders")
    public List<OrderData> getMyOrders(Authentication authentication) {
        System.out.println("=== USER ORDERS ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            if (authentication != null && authentication.isAuthenticated()) {
                String userEmail = authentication.getName();
                return orderDto.getOrdersByUserId(userEmail);
            } else {
                throw new ApiException("User not authenticated");
            }
        } catch (Exception e) {
            throw new ApiException("Failed to get user orders: " + e.getMessage());
        }
    }

    @GetMapping("/orders/by-date-range")
    public List<OrderData> getMyOrdersByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate,
            Authentication authentication) {
        try {
            if (authentication != null && authentication.isAuthenticated()) {
                String userEmail = authentication.getName();
                java.time.LocalDate start = java.time.LocalDate.parse(startDate);
                java.time.LocalDate end = java.time.LocalDate.parse(endDate);
                return orderDto.getOrdersByUserIdAndDateRange(userEmail, start, end);
            } else {
                throw new ApiException("User not authenticated");
            }
        } catch (java.time.format.DateTimeParseException e) {
            throw new ApiException("Invalid date format. Please use yyyy-MM-dd format (e.g., 2024-01-15)");
        } catch (Exception e) {
            throw new ApiException("Failed to get orders by date range: " + e.getMessage());
        }
    }

    @PostMapping("/orders")
    @org.springframework.transaction.annotation.Transactional
    public OrderData createOrder(@RequestBody OrderForm form, Authentication authentication) {
        System.out.println("=== USER ORDER CREATE ENDPOINT ===");
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
            throw new ApiException("Failed to create order: " + e.getMessage());
        }
    }

    @GetMapping("/orders/{id}")
    public OrderData getMyOrder(@PathVariable Integer id, Authentication authentication) {
        System.out.println("=== USER ORDER GET ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            if (authentication != null && authentication.isAuthenticated()) {
                String userEmail = authentication.getName();
                OrderData order = orderDto.get(id);
                
                // Check if the order belongs to the authenticated user
                if (!userEmail.equals(order.getUserId())) {
                    throw new ApiException("Access denied. You can only view your own orders.");
                }
                
                return order;
            } else {
                throw new ApiException("User not authenticated");
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get order: " + e.getMessage());
        }
    }

    @GetMapping("/orders/{id}/download-invoice")
    public ResponseEntity<org.springframework.core.io.Resource> downloadMyOrderInvoice(@PathVariable Integer id, Authentication authentication) {
        System.out.println("=== USER ORDER DOWNLOAD INVOICE ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
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
        System.out.println("=== USER PRODUCT GET BY BARCODE ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            if (authentication != null && authentication.isAuthenticated()) {
                return productDto.getByBarcode(barcode);
            } else {
                throw new ApiException("User not authenticated");
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get product by barcode: " + e.getMessage());
        }
    }

    @GetMapping("/inventory/barcode/{barcode}")
    public InventoryData getInventoryByBarcode(@PathVariable String barcode, Authentication authentication) {
        System.out.println("=== USER INVENTORY GET BY BARCODE ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            if (authentication != null && authentication.isAuthenticated()) {
                return inventoryDto.getByProductBarcode(barcode);
            } else {
                throw new ApiException("User not authenticated");
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get inventory by barcode: " + e.getMessage());
        }
    }

    @GetMapping("/search/barcode/{barcode}")
    public ResponseEntity<Object> searchByBarcode(@PathVariable String barcode, Authentication authentication) {
        System.out.println("=== USER SEARCH BY BARCODE ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            if (authentication != null && authentication.isAuthenticated()) {
                // Try to get product first
                ProductData product = null;
                InventoryData inventory = null;
                
                try {
                    product = productDto.getByBarcode(barcode);
                } catch (Exception e) {
                    // Product not found, continue to inventory
                }
                
                try {
                    inventory = inventoryDto.getByProductBarcode(barcode);
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
            } else {
                throw new ApiException("User not authenticated");
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to search by barcode: " + e.getMessage());
        }
    }


} 