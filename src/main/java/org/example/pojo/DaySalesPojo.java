package org.example.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.util.List;

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
    
    // Store all orders for this day to enable filtering by brand
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "day_date", referencedColumnName = "date")
    @JsonIgnore
    private List<OrderPojo> orders;
} 