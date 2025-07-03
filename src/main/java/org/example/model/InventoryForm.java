package org.example.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class InventoryForm {

    private Integer productId;
    private String productName;
    private String barcode;
    private Integer quantity;
    private Double mrp;
    private String image; // Base64 string from frontend

}
