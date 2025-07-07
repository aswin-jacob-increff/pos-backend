package org.example.model;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class InvoiceDetailsData {
    private Integer id;
    private Integer orderId;
    private String filePath;
    private String invoiceId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 