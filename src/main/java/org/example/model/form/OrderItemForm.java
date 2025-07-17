package org.example.model.form;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

@Setter
@Getter
public class OrderItemForm {

    private Integer orderId;
    private LocalDateTime dateTime; // Always IST from frontend
    @NotNull(message = "Product ID is required")
    private Integer productId;
    private String productName;
    private String barcode;
    private Integer clientId;
    private String clientName;
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
    @NotNull(message = "Selling price is required")
    @Positive(message = "Selling price must be positive")
    private Double sellingPrice;
    private String image;
}

