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
        return orderDto.get(id);
    }

    @GetMapping
    public List<OrderData> getAll() {
        return orderDto.getAll();
    }

    @PutMapping("/{id}")
    public OrderData update(@PathVariable Integer id, @RequestBody OrderForm form) {
        return orderDto.update(id, form);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        orderDto.delete(id);
    }
    
    @DeleteMapping("/{id}/cancel")
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
            throw new ApiException("Failed to download invoice: " + e.getMessage());
        }
    }

}
