package org.example.model.form;

import lombok.Getter;
import lombok.Setter;
import java.time.ZonedDateTime;

@Getter
@Setter
public class DaySalesForm {
    private ZonedDateTime date;
    private Integer invoicedOrdersCount;
    private Integer invoicedItemsCount;
    private Double totalRevenue;
} 