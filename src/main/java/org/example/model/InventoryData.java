package org.example.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class InventoryData {

    private Integer id;
    private Integer productId;
    private String productName;
    private String barcode;
    private Integer quantity;
    private Double mrp;
    private String imageUrl; // Reference to image endpoint: /api/products/{productId}/image

    public void setMrp(Double mrp) {
        this.mrp = mrp == null ? null : org.example.util.TimeUtil.round2(mrp);
    }
    public Double getMrp() {
        return mrp == null ? null : org.example.util.TimeUtil.round2(mrp);
    }
}
