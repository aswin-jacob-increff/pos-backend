package org.example.controller;

import org.example.dto.OrderDto;
import org.example.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.example.model.OrderData;
import org.example.model.OrderForm;
import org.example.model.InvoiceData;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderDto orderDto;

    @PostMapping("/add")
    public OrderData add(@RequestBody OrderForm form) {
        return orderDto.add(form);
    }

    @GetMapping("/{id}")
    public OrderData get(@PathVariable Integer id) {
        if (id == null) {
            throw new ApiException("Order ID cannot be null");
        }
        return orderDto.get(id);
    }

    @GetMapping
    public List<OrderData> getAll() {
        return orderDto.getAll();
    }

    @PutMapping("/{id}")
    public OrderData update(@PathVariable Integer id, @RequestBody OrderForm form) {
        if (id == null) {
            throw new ApiException("Order ID cannot be null");
        }
        return orderDto.update(id, form);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        if (id == null) {
            throw new ApiException("Order ID cannot be null");
        }
        orderDto.delete(id);
    }
    
    @PutMapping("/{id}/cancel")
    public OrderData cancelOrder(@PathVariable Integer id) {
        if (id == null) {
            throw new ApiException("Order ID cannot be null");
        }
        return orderDto.cancelOrder(id);
    }

    @PostMapping("/{id}/generate-invoice")
    public ResponseEntity<String> generateInvoice(@PathVariable Integer id) {
        try {
            String filePath = orderDto.generateInvoice(id); // e.g., "src/main/resources/invoice/order-5.pdf"

            String fileName = new java.io.File(filePath).getName(); // gets "order-5.pdf"
            String downloadUrl = "/files/invoice/" + fileName;

            return ResponseEntity.ok(downloadUrl);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid input: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to generate invoice: " + e.getMessage());
        }
    }

}
