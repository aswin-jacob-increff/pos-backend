package org.example.controller;

import org.example.dto.InvoiceDto;
import org.example.model.InvoiceData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoice")
public class InvoiceController {

    @Autowired
    private InvoiceDto invoiceDto;

    // POST /api/invoice/{orderId}
    @PostMapping("/{orderId}")
    public InvoiceData generateInvoice(@PathVariable Integer orderId) {
        return invoiceDto.generateInvoice(orderId);
    }

    // GET /api/invoice/{id}
    @GetMapping("/{id}")
    public InvoiceData getInvoice(@PathVariable Integer id) {
        return invoiceDto.getInvoice(id);
    }

    // GET /api/invoice
    @GetMapping
    public List<InvoiceData> getAllInvoices() {
        return invoiceDto.getAllInvoices();
    }

    // GET /api/invoice/{orderId}/pdf
    @GetMapping("/{orderId}/pdf")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Integer orderId) {
        try {
            byte[] pdfBytes = invoiceDto.generateInvoicePdf(orderId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("invoice-" + orderId + ".pdf")
                    .build());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
