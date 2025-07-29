package org.example.model.data;

import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.ZonedDateTime;

@Getter
@Setter
public class InvoiceDetailsData {
    private Integer id;
    private Integer orderId;
    private String filePath;
    private String invoiceId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "Asia/Kolkata")
    private ZonedDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "Asia/Kolkata")
    private ZonedDateTime updatedAt;
} 