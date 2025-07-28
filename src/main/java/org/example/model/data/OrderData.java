package org.example.model.data;

import lombok.Getter;
import lombok.Setter;
import org.example.util.TimeUtil;

import java.time.LocalDateTime;
import java.util.List;
import org.example.model.enums.OrderStatus;

@Setter
@Getter
public class OrderData {

    private Integer id;
    private LocalDateTime date; // Always IST for frontend
    private List<OrderItemData> orderItemDataList;
    private Double total;
    private OrderStatus status; // Order status
    private String invoiceBase64; // Base64 encoded invoice PDF
    private String invoiceUrl; // URL to download invoice
    private String userId;

    public void setTotal(Double total) {
        this.total = total == null ? null : TimeUtil.round2(total);
    }
    public Double getTotal() {
        return total == null ? null : TimeUtil.round2(total);
    }
}
