package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesReportData {
    private String brand;
    private String category;
    private Integer quantity;
    private Double revenue;
} 