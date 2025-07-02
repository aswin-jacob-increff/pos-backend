package org.example.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table (name = "invoice_item")
public class InvoiceItemPojo {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "orderItem_id")
    private OrderItemPojo orderItem;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "invoice_id")
    private InvoicePojo invoice;

    private Double amount;
    private String name;
    private Double price;
    private Integer quantity;
}
