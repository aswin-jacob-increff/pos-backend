package org.example.model;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
public class OrderForm {

    private LocalDateTime date; // Always IST from frontend
    @NotNull(message = "Order must contain at least one item")
    @Size(min = 1, message = "Order must contain at least one item")
    private List<OrderItemForm> orderItemFormList;
    private Double total;
    private String userId;
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserId() { return userId; }

}
