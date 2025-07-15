package org.example.controller;

import org.example.dto.OrderDto;
import org.example.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.example.model.OrderData;
import org.example.model.OrderForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderDto orderDto;

    @PostMapping("/add")
    @org.springframework.transaction.annotation.Transactional
    public OrderData add(@RequestBody OrderForm form) {
        try {
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
    public List<OrderData> getAll(org.springframework.security.core.Authentication authentication, 
                                 HttpServletRequest request) {
        String email;
        boolean isSupervisor = false;
        
        // If not authenticated in current context, try to restore from session
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getName())) {
            
            HttpSession session = request.getSession(false);
            if (session != null) {
                // Try to get authentication from session
                org.springframework.security.core.Authentication sessionAuth = 
                    (org.springframework.security.core.Authentication) session.getAttribute("AUTHENTICATION");
                if (sessionAuth != null && sessionAuth.isAuthenticated()) {
                    // Restore the authentication in the security context
                    org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(sessionAuth);
                    authentication = sessionAuth;
                }
            }
        }
        
        if (authentication != null && authentication.isAuthenticated()) {
            email = authentication.getName();
            isSupervisor = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPERVISOR"));
        } else {
            throw new ApiException("User not authenticated");
        }
        
        if (isSupervisor) {
            return orderDto.getAll();
        } else {
            return orderDto.getOrdersByUserId(email);
        }
    }

    @PutMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional
    public OrderData update(@PathVariable Integer id, @RequestBody OrderForm form) {
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
    public void cancelOrder(@PathVariable Integer id) {
        try {
            orderDto.cancelOrder(id);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to cancel order: " + e.getMessage());
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
            @RequestParam String endDate,
            org.springframework.security.core.Authentication authentication,
            HttpServletRequest request) {
        String email = null;
        boolean isSupervisor = false;
        
        // If not authenticated in current context, try to restore from session
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getName())) {
            
            HttpSession session = request.getSession(false);
            if (session != null) {
                // Try to get authentication from session
                org.springframework.security.core.Authentication sessionAuth = 
                    (org.springframework.security.core.Authentication) session.getAttribute("AUTHENTICATION");
                if (sessionAuth != null && sessionAuth.isAuthenticated()) {
                    // Restore the authentication in the security context
                    org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(sessionAuth);
                    authentication = sessionAuth;
                }
            }
        }
        
        if (authentication != null && authentication.isAuthenticated()) {
            email = authentication.getName();
            isSupervisor = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPERVISOR"));
        } else {
            throw new ApiException("User not authenticated");
        }
        
        try {
            java.time.LocalDate start = java.time.LocalDate.parse(startDate);
            java.time.LocalDate end = java.time.LocalDate.parse(endDate);
            if (isSupervisor) {
                return orderDto.getOrdersByDateRange(start, end);
            } else {
                return orderDto.getOrdersByUserIdAndDateRange(email, start, end);
            }
        } catch (java.time.format.DateTimeParseException e) {
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

}
