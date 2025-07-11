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

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderDto orderDto;

    @PostMapping("/add")
    @org.springframework.transaction.annotation.Transactional
    public OrderData add(@RequestBody OrderForm form) {
        return orderDto.add(form);
    }

    @GetMapping("/{id}")
    public OrderData get(@PathVariable Integer id) {
        return orderDto.get(id);
    }

    @GetMapping
    public List<OrderData> getAll(org.springframework.security.core.Authentication authentication, 
                                 HttpServletRequest request) {
        String email;
        boolean isSupervisor = false;
        if (authentication != null) {
            email = authentication.getName();
            isSupervisor = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPERVISOR"));
        } else {
            HttpSession session = request.getSession(false);
            if (session != null) {
                Object userEmail = session.getAttribute("userEmail");
                Object userRole = session.getAttribute("userRole");
                if (userEmail != null) {
                    email = userEmail.toString();
                    isSupervisor = "ROLE_SUPERVISOR".equals(userRole);
                } else {
                    throw new ApiException("User not authenticated");
                }
            } else {
                throw new ApiException("User not authenticated");
            }
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
        return orderDto.update(id, form);
    }

    @DeleteMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional
    public void delete(@PathVariable Integer id) {
        orderDto.delete(id);
    }
    
    @DeleteMapping("/{id}/cancel")
    @org.springframework.transaction.annotation.Transactional
    public void cancelOrder(@PathVariable Integer id) {
        orderDto.cancelOrder(id);
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
        if (authentication != null) {
            email = authentication.getName();
            isSupervisor = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPERVISOR"));
        } else {
            HttpSession session = request.getSession(false);
            if (session != null) {
                Object userEmail = session.getAttribute("userEmail");
                Object userRole = session.getAttribute("userRole");
                if (userEmail != null) {
                    email = userEmail.toString();
                    isSupervisor = "ROLE_SUPERVISOR".equals(userRole);
                } else {
                    throw new ApiException("User not authenticated");
                }
            } else {
                throw new ApiException("User not authenticated");
            }
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
        return orderDto.getOrdersByUserId(userId);
    }

}
