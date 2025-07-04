package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesReportResponse {
    private String brand;
    private String category;
    private Integer quantity;
    private Double revenue;
} 