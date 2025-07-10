package org.example.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "pos_day_sales")
@Getter
@Setter
public class DaySalesPojo {
    @Id
    private LocalDate date;

    @Column(nullable = false)
    private int invoicedOrdersCount;

    @Column(nullable = false)
    private int invoicedItemsCount;

    @Column(nullable = false)
    private double totalRevenue;
    
    // Orders are now denormalized and stored separately
    // No direct reference needed - orders are queried by date when needed
} 