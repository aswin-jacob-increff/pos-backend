package org.example.model;

import lombok.Getter;
import lombok.Setter;
import org.example.pojo.OrderStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
public class OrderData {

    private Integer id;
    private Instant date;
    private List<OrderItemData> orderItemDataList;
    private OrderStatus status;
    private Double total;

}
