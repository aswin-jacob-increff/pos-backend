package org.example.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import org.example.pojo.OrderStatus;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class OrderPojo extends AbstractPojo {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "order_id_generator")
    @TableGenerator(
        name = "order_id_generator",
        table = "id_generators",
        pkColumnName = "gen_name",
        valueColumnName = "gen_val",
        pkColumnValue = "order_id",
        allocationSize = 1
    )
    private Integer id;

    private Instant date;

    private double total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.CREATED;

    @Column(nullable = false)
    private String userId; // email of the user who created the order

    // Order items are now denormalized and stored separately
    // No direct reference needed - order items reference this order by order_id

    public void setTotal(double total) {
        this.total = org.example.util.TimeUtil.round2(total);
    }
    public double getTotal() {
        return org.example.util.TimeUtil.round2(this.total);
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getUserId() {
        return userId;
    }
}
