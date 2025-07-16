package org.example.model;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Setter
@Getter
public class InventoryForm {

    private Integer productId; // No longer mandatory - will be populated by backend
    private String productName; // Will be populated by backend
    @NotNull(message = "Barcode is required")
    private String barcode;
    private String clientName; // Will be populated by backend
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
    private Double mrp; // Will be populated by backend
    private String imageUrl; // Will be populated by backend
    private String image; // Base64 string from frontend (optional)

}
