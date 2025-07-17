package org.example.model.form;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Setter
@Getter
public class ProductForm {

    @NotBlank(message = "Barcode is required")
    @Size(max = 255, message = "Barcode must be at most 255 characters")
    private String barcode;
//    @NotBlank(message = "Client name is required")
    @Size(max = 255, message = "Client name must be at most 255 characters")
    private String clientName;
    private Integer clientId;
    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must be at most 255 characters")
    private String name;
    @NotNull(message = "MRP is required")
    @Positive(message = "MRP must be positive")
    private Double mrp;
    private String image; // Image URL string from frontend

}
