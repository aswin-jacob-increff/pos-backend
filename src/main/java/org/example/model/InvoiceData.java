package org.example.model;

import lombok.Getter;
import lombok.Setter;
import org.example.pojo.InvoiceItemPojo;
import org.example.pojo.OrderItemPojo;
import org.example.pojo.OrderPojo;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class InvoiceData {

    private Integer id;
    private List<InvoiceItemData> invoiceItemPojoList;
    private Double total;
    private Integer orderId;
    private Instant dateTime;
    private Integer totalQuantity;
}
