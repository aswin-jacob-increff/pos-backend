package org.example.model.data;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class InvoiceAppForm {

    private Integer id;
    private List<InvoiceAppFormItem> invoiceItemPojoList;
    private Double total;
    private Integer orderId;
    private Integer totalQuantity;
    private String date;
}
