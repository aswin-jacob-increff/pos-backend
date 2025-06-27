package org.example.model;

import lombok.Getter;
import lombok.Setter;
import org.example.pojo.ProductPojo;

@Setter
@Getter
public class InventoryForm {

    private Integer productId;
    private String productName;
    private String productBarcode;
    private Integer clientId;
    private String clientName;
    private Integer quantity;

}
