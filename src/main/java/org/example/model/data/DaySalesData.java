package org.example.model.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DaySalesData {
    private String date; // Formatted date string (YYYY-MM-DD)
    private Integer invoicedOrdersCount;
    private Integer invoicedItemsCount;
    private Double totalRevenue;
} 