package org.example.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class InvoiceData {

    private Integer id;
    private List<InvoiceItemData> invoiceItemPojoList;
    private Double total;
    private Integer orderId;
    private LocalDateTime dateTime; // Always IST for frontend
    private Integer totalQuantity;
}
