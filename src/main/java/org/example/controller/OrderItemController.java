package org.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.example.model.OrderItemForm;
import org.example.model.OrderItemData;
import org.example.dto.OrderItemDto;

@RestController
@RequestMapping("/api/order-items")
public class OrderItemController {

    @Autowired
    private OrderItemDto orderItemDto;

    @PostMapping
    @org.springframework.transaction.annotation.Transactional
    public OrderItemData add(@RequestBody OrderItemForm form) {
        return orderItemDto.add(form);
    }

    @GetMapping("/{id}")
    public OrderItemData get(@PathVariable Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Order Item ID cannot be null");
        }
        return orderItemDto.get(id);
    }

    @GetMapping
    public List<OrderItemData> getAll() {
        return orderItemDto.getAll();
    }

    @GetMapping("/orders/{orderId}")
    public List<OrderItemData> getByOrderId(@PathVariable Integer orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        return orderItemDto.getByOrderId(orderId);
    }

    @PutMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional
    public void update(@PathVariable Integer id, @RequestBody OrderItemForm form) {
        if (id == null) {
            throw new IllegalArgumentException("Order Item ID cannot be null");
        }
        orderItemDto.update(id, form);
    }

    @DeleteMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional
    public void delete(@PathVariable Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Order Item ID cannot be null");
        }
        orderItemDto.delete(id);
    }
}
