package org.example.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.util.TimeUtil;

import java.time.ZonedDateTime;
import org.example.model.enums.OrderStatus;

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

    private ZonedDateTime date;

    private double total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.CREATED;

    @Column(nullable = false)
    private String userId; // email of the user who created the order

    // Order items are now denormalized and stored separately
    // No direct reference needed - order items reference this order by order_id

    public void setTotal(double total) {
        this.total = TimeUtil.round2(total);
    }
    public double getTotal() {
        return TimeUtil.round2(this.total);
    }

}
