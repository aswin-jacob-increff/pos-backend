package org.example.model;

import lombok.Getter;
import lombok.Setter;
import org.example.pojo.ClientPojo;

@Setter
@Getter
public class ProductData {

    private Integer id;
    private String barcode;
    private String clientName;
    private Integer clientId;
    private String name;
    private Double mrp;
    private String imageUrl;

}

