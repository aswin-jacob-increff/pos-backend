package org.example.controller;

import org.example.dto.OrderDto;
import org.example.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.example.model.OrderData;
import org.example.model.OrderForm;

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
    
    @DeleteMapping("/{id}/cancel")
    public void cancelOrder(@PathVariable Integer id) {
        if (id == null) {
            throw new ApiException("Order ID cannot be null");
        }
        orderDto.cancelOrder(id);
    }

    @GetMapping("/{id}/generate-and-download-invoice")
    public ResponseEntity<org.springframework.core.io.Resource> generateAndDownloadInvoice(@PathVariable Integer id) {
        try {
            if (id == null) {
                throw new ApiException("Order ID cannot be null");
            }
            
            // Generate the PDF and get it as a resource
            org.springframework.core.io.Resource pdfResource = orderDto.generateAndGetInvoiceResource(id);
            String fileName = "order-" + id + ".pdf";
            
            return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdfResource);
        } catch (Exception e) {
            throw new ApiException("Failed to generate and download invoice: " + e.getMessage());
        }
    }

}
