package org.example.dto;

import org.example.flow.OrderFlow;
import org.example.model.OrderItemForm;
import org.example.model.OrderItemData;
import org.example.pojo.OrderItemPojo;
import org.example.flow.OrderItemFlow;
import org.example.service.OrderService;
import org.example.service.ProductService;
import org.example.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;

@Component
public class OrderItemDto {

    @Autowired
    private OrderItemFlow orderItemFlow;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    public OrderItemData add(OrderItemForm orderItemForm) {
        validate(orderItemForm);
        OrderItemPojo orderItemPojo = convert(orderItemForm);
        orderItemFlow.add(orderItemPojo);
        return convert(orderItemPojo);
    }

    public OrderItemData get(Integer id) {
        if (id == null) {
            throw new ApiException("Order Item ID cannot be null");
        }
        return convert(orderItemFlow.get(id));
    }

    public List<OrderItemData> getAll() {
        List<OrderItemPojo> orderItemPojoList = orderItemFlow.getAll();
        List<OrderItemData> orderItemDataList = new ArrayList<>();
        for (OrderItemPojo orderItemPojo : orderItemPojoList) {
            orderItemDataList.add(convert(orderItemPojo));
        }
        return orderItemDataList;
    }

    public List<OrderItemData> getByOrderId(Integer orderId) {
        if (orderId == null) {
            throw new ApiException("Order ID cannot be null");
        }
        List<OrderItemPojo> orderItemPojoList = orderItemFlow.getByOrderId(orderId);
        List<OrderItemData> orderItemDataList = new ArrayList<>();
        for (OrderItemPojo orderItemPojo : orderItemPojoList) {
            orderItemDataList.add(convert(orderItemPojo));
        }
        return orderItemDataList;
    }

    public OrderItemData update(OrderItemForm orderItemForm, Integer id) {
        if (id == null) {
            throw new ApiException("Order Item ID cannot be null");
        }
        validate(orderItemForm);
        return convert(orderItemFlow.update(convert(orderItemForm), id));
    }

    public void delete(Integer id) {
        if (id == null) {
            throw new ApiException("Order Item ID cannot be null");
        }
        orderItemFlow.delete(id);
    }

    public void validate(OrderItemForm orderItemForm) {
        if (orderItemForm.getOrderId() == null) {
            throw new ApiException("Order ID cannot be null");
        }
        orderItemForm.setDateTime(orderService.get(orderItemForm.getOrderId()).getDate());
        if (orderItemForm.getProductId() == null) {
            if (orderItemForm.getBarcode() == null || orderItemForm.getBarcode().trim().isEmpty()) {
                if (orderItemForm.getProductName() == null || orderItemForm.getProductName().trim().isEmpty()) {
                    throw new ApiException("One of the three (product id, name, barcode) must be present");
                } else {
                    orderItemForm.setProductId(productService.getByName(orderItemForm.getProductName()).getId());
                    orderItemForm.setBarcode(productService.getByName(orderItemForm.getProductName()).getBarcode());
                }
            } else {
                orderItemForm.setProductId(productService.getByBarcode(orderItemForm.getBarcode()).getId());
                orderItemForm.setProductName(productService.getByBarcode(orderItemForm.getBarcode()).getName());
            }
        } else {
            orderItemForm.setBarcode(productService.get(orderItemForm.getProductId()).getBarcode());
            orderItemForm.setProductName(productService.get(orderItemForm.getProductId()).getName());
        }
        
        if (orderItemForm.getQuantity() == null) {
            throw new ApiException("Order Item quantity cannot be null");
        }
        if (orderItemForm.getQuantity() <= 0) {
            throw new ApiException("Order Item quantity must be positive");
        }
        
        // Validate base64 image if provided
        if (orderItemForm.getImage() != null && !orderItemForm.getImage().trim().isEmpty()) {
            if (!isValidBase64(orderItemForm.getImage())) {
                throw new ApiException("Image must be a valid base64 string");
            }
        }
    }

    private OrderItemPojo convert(OrderItemForm orderItemForm) {
        OrderItemPojo orderItemPojo = new OrderItemPojo();
        orderItemPojo.setOrder(orderService.get(orderItemForm.getOrderId()));
        orderItemPojo.setProduct(productService.get(orderItemForm.getProductId()));
        orderItemPojo.setQuantity(orderItemForm.getQuantity());
        orderItemPojo.setSellingPrice(productService.get(orderItemForm.getProductId()).getMrp());
        
        // Handle base64 image if provided (this would update the product's image)
        if (orderItemForm.getImage() != null && !orderItemForm.getImage().trim().isEmpty()) {
            // For now, we'll just validate the image
            // In a real implementation, you might want to update the product's image
        }
        
        return orderItemPojo;
    }

    private OrderItemData convert(OrderItemPojo orderItemPojo) {
        OrderItemData orderItemData = new OrderItemData();
        orderItemData.setId(orderItemPojo.getId());
        orderItemData.setProductId(orderItemPojo.getProduct().getId());
        orderItemData.setBarcode(orderItemPojo.getProduct().getBarcode());
        orderItemData.setProductName(orderItemPojo.getProduct().getName());
        orderItemData.setQuantity(orderItemPojo.getQuantity());
        orderItemData.setSellingPrice(orderItemPojo.getSellingPrice());
        
        // Set imageUrl as reference to product image endpoint
        if (orderItemPojo.getProduct().getImageUrl() != null && !orderItemPojo.getProduct().getImageUrl().trim().isEmpty()) {
            orderItemData.setImageUrl("/api/products/" + orderItemPojo.getProduct().getId() + "/image");
        }
        
        return orderItemData;
    }
    
    private boolean isValidBase64(String str) {
        try {
            Base64.getDecoder().decode(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}