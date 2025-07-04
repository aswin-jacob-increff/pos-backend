package org.example.model;

import lombok.Getter;
import lombok.Setter;
import org.example.pojo.ProductPojo;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDateTime;

@Setter
@Getter
public class OrderItemData {

    private Integer id;
    private Integer orderId;
    private LocalDateTime dateTime; // Always IST for frontend
    private Integer productId;
    private String productName;
    private String barcode;
    private Integer clientId;
    private String clientName;
    private Integer quantity;
    private Double sellingPrice;
    private Double amount;
    private String imageUrl; // Reference to image endpoint: /api/products/{productId}/image

}
