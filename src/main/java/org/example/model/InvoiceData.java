package org.example.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class InvoiceData {

    private Integer id;
    private List<InvoiceItemData> invoiceItemPojoList;
    private Double total;
    private Integer orderId;
    private Integer totalQuantity;
    // Add date field for invoice (as String in IST format)
    private String date;
}
