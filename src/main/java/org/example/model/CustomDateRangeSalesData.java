package org.example.model;

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