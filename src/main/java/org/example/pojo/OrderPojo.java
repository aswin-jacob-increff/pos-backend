package org.example.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import org.example.pojo.OrderStatus;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class OrderPojo extends AbstractPojo {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Integer id;

    private Instant date;

    private double total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.CREATED;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemPojo> orderItems;
}
