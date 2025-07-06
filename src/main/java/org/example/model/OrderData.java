package org.example.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
public class OrderData {

    private Integer id;
    private LocalDateTime date; // Always IST for frontend
    private List<OrderItemData> orderItemDataList;
    private Double total;
    private String invoiceBase64; // Base64 encoded invoice PDF
    private String invoiceUrl; // URL to download invoice

}
