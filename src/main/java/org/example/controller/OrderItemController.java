package org.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.example.model.OrderItemForm;
import org.example.model.OrderItemData;
import org.example.dto.OrderItemDto;
import org.example.exception.ApiException;

@RestController
@RequestMapping("/api/order-items")
public class OrderItemController {

    @Autowired
    private OrderItemDto orderItemDto;

    @PostMapping
    @org.springframework.transaction.annotation.Transactional
    public OrderItemData add(@RequestBody OrderItemForm form) {
        try {
            return orderItemDto.add(form);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to add order item: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public OrderItemData get(@PathVariable Integer id) {
        try {
            if (id == null) {
                throw new ApiException("Order Item ID cannot be null");
            }
            return orderItemDto.get(id);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get order item: " + e.getMessage());
        }
    }

    @GetMapping
    public List<OrderItemData> getAll() {
        try {
            return orderItemDto.getAll();
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get all order items: " + e.getMessage());
        }
    }

    @GetMapping("/orders/{orderId}")
    public List<OrderItemData> getByOrderId(@PathVariable Integer orderId) {
        try {
            if (orderId == null) {
                throw new ApiException("Order ID cannot be null");
            }
            return orderItemDto.getByOrderId(orderId);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get order items by order ID: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional
    public void update(@PathVariable Integer id, @RequestBody OrderItemForm form) {
        try {
            if (id == null) {
                throw new ApiException("Order Item ID cannot be null");
            }
            orderItemDto.update(id, form);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to update order item: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional
    public void delete(@PathVariable Integer id) {
        try {
            if (id == null) {
                throw new ApiException("Order Item ID cannot be null");
            }
            orderItemDto.delete(id);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to delete order item: " + e.getMessage());
        }
    }
}
