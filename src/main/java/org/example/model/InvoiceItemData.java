package org.example.model;

import lombok.Getter;
import lombok.Setter;
import org.example.pojo.InvoicePojo;
import org.example.pojo.OrderItemPojo;

@Getter
@Setter
public class InvoiceItemData {

    private Integer id;
    private Integer productId;
    private String productName;
    private String productBarcode;
    private String clientName;
    private Integer clientId;
    private Double amount;
    private Integer quantity;
    private Double price;
}
