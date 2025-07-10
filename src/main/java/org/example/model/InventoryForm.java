package org.example.model;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Setter
@Getter
public class InventoryForm {

    @NotNull(message = "Product ID is required")
    private Integer productId;
    private String productName;
    private String barcode;
    private String clientName;
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
    private Double mrp;
    private String imageUrl;
    private String image; // Base64 string from frontend

}
