package org.example.controller;

import org.example.dto.OrderItemDto;
import org.example.exception.ApiException;
import org.example.model.data.OrderItemData;
import org.example.model.form.OrderItemForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supervisor/order-items")
public class OrderItemController {

    @Autowired
    private OrderItemDto orderItemDto;

    @PostMapping("/add")
    @org.springframework.transaction.annotation.Transactional
    public OrderItemData add(@RequestBody OrderItemForm form, Authentication authentication) {
        System.out.println("=== SUPERVISOR ORDER ITEM ADD ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            return orderItemDto.add(form);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to add order item: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public OrderItemData get(@PathVariable Integer id, Authentication authentication) {
        System.out.println("=== SUPERVISOR ORDER ITEM GET ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            return orderItemDto.get(id);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get order item: " + e.getMessage());
        }
    }

    @GetMapping
    public List<OrderItemData> getAll(Authentication authentication) {
        System.out.println("=== SUPERVISOR ORDER ITEM GET ALL ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            return orderItemDto.getAll();
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get all order items: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional
    public OrderItemData update(@PathVariable Integer id, @RequestBody OrderItemForm form, Authentication authentication) {
        System.out.println("=== SUPERVISOR ORDER ITEM UPDATE ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            return orderItemDto.update(id, form);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to update order item: " + e.getMessage());
        }
    }

    @GetMapping("/by-order/{orderId}")
    public List<OrderItemData> getOrderItemsByOrderId(@PathVariable Integer orderId, Authentication authentication) {
        System.out.println("=== SUPERVISOR ORDER ITEM GET BY ORDER ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            return orderItemDto.getByOrderId(orderId);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get order items by order ID: " + e.getMessage());
        }
    }

}
