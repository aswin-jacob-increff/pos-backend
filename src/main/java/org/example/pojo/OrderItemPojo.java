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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "order_id")
    private OrderPojo order;

    @Column(nullable = false)
    private String productBarcode;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private String clientName;

    private Double productMrp;
    private String productImageUrl;

    private Integer quantity;
    private Double sellingPrice;
    private Double amount;
}