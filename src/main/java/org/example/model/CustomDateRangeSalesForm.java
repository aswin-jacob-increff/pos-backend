package org.example.model;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class CustomDateRangeSalesForm {
    private LocalDate startDate;
    private LocalDate endDate;
    private String brand; // Optional client name filter
    private String category; // Optional product name filter
} 