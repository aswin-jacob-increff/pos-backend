package org.example.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "order_item")
public class OrderItemPojo extends AbstractPojo {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn (name = "order_id")
    private OrderPojo order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn (name = "product_id")
    private ProductPojo product;

    private Integer quantity;
    private Double sellingPrice;
    private Double amount;
}