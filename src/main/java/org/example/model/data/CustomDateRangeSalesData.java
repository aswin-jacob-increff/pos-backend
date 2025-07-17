package org.example.model.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomDateRangeSalesData {
    private String brand;
    private String category; // product name/category
    private Double totalAmount;
    private Integer totalOrders;
    private Integer totalItems;
} 