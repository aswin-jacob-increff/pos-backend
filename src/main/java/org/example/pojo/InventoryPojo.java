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

    @ManyToOne (fetch = FetchType.EAGER)
    @JoinColumn (name = "product_id", unique = true)
    private ProductPojo product;

    private Integer quantity;

}