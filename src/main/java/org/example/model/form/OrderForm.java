package org.example.model.form;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.ZonedDateTime;
import java.util.List;

@Setter
@Getter
public class OrderForm {

    private ZonedDateTime date; // Always IST from frontend
    @NotNull(message = "Order must contain at least one item")
    @Size(min = 1, message = "Order must contain at least one item")
    private List<OrderItemForm> orderItemFormList;
    private String userId;

}