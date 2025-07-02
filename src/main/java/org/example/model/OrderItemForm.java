package org.example.model;

import lombok.Getter;
import lombok.Setter;
import org.example.pojo.OrderPojo;
import org.example.pojo.ProductPojo;

import java.time.Instant;

@Setter
@Getter
public class OrderItemForm {

    private Integer orderId;
    private Instant dateTime;
    private Integer productId;
    private String productName;
    private String productBarcode;
    private Integer clientId;
    private String clientName;
    private Integer quantity;
    private Double sellingPrice;
}

