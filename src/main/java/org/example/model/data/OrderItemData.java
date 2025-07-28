package org.example.model.data;

import lombok.Getter;
import lombok.Setter;
import org.example.util.TimeUtil;

import java.time.LocalDateTime;

@Setter
@Getter
public class OrderItemData {

    private Integer id;
    private Integer orderId;
    private LocalDateTime dateTime; // Always IST for frontend
    private Integer productId;
    private String productName;
    private String barcode;
    private Integer clientId;
    private String clientName;
    private Integer quantity;
    private Double sellingPrice;
    private Double amount;
    private String imageUrl; // Reference to image endpoint: /api/products/{productId}/image

    public void setSellingPrice(Double sellingPrice) {
        this.sellingPrice = sellingPrice == null ? null : TimeUtil.round2(sellingPrice);
    }
    public Double getSellingPrice() {
        return sellingPrice == null ? null : TimeUtil.round2(sellingPrice);
    }
    public void setAmount(Double amount) {
        this.amount = amount == null ? null : TimeUtil.round2(amount);
    }
    public Double getAmount() {
        return amount == null ? null : TimeUtil.round2(amount);
    }
}
