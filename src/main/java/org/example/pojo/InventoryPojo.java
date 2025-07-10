package org.example.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table (name = "inventory")
public class InventoryPojo extends AbstractPojo {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Integer id;

    @Column(unique = true)
    private String productBarcode;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private String clientName;

    private Double productMrp;
    private String productImageUrl;

    private Integer quantity;

}