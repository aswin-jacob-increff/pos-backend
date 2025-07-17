package org.example.model.data;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProductData {

    private Integer id;
    private String barcode;
    private String clientName;
    private Integer clientId;
    private String name;
    private Double mrp;
    private String imageUrl; // Reference to image endpoint: /api/products/{id}/image

    public void setMrp(Double mrp) {
        this.mrp = mrp == null ? null : org.example.util.TimeUtil.round2(mrp);
    }
    public Double getMrp() {
        return mrp == null ? null : org.example.util.TimeUtil.round2(mrp);
    }
}

