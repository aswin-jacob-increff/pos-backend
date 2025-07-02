package org.example.model;

import lombok.Getter;
import lombok.Setter;
import org.example.pojo.OrderStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
public class OrderForm {

    private Instant date;
    private List<OrderItemForm> orderItemFormList;
    private OrderStatus status;
    private Double total;

}
