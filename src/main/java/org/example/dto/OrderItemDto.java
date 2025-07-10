package org.example.dto;

import org.example.model.OrderItemForm;
import org.example.model.OrderItemData;
import org.example.pojo.OrderItemPojo;
import org.example.flow.OrderItemFlow;
import org.example.api.OrderApi;
import org.example.api.ProductApi;
import org.example.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;
import java.util.Objects;
import jakarta.validation.Valid;
import org.example.util.TimeUtil;

@Component
public class OrderItemDto extends AbstractDto<OrderItemPojo, OrderItemForm, OrderItemData> {

    @Autowired
    private OrderItemFlow orderItemFlow;

    @Autowired
    private OrderApi orderApi;

    @Autowired
    private ProductApi productApi;

    @Override
    protected String getEntityName() {
        return "OrderItem";
    }

    @Override
    protected void preprocess(OrderItemForm orderItemForm) {
        // Cross-field/entity logic: orderId lookup, productId/productName/barcode lookup, base64 image validation
        if (Objects.isNull(orderItemForm.getOrderId())) {
            throw new ApiException("Order ID cannot be null");
        }
        // No need to setDateTime here; handled in convert
        if (Objects.isNull(orderItemForm.getProductId())) {
            if (orderItemForm.getBarcode() == null || orderItemForm.getBarcode().trim().isEmpty()) {
                if (orderItemForm.getProductName() == null || orderItemForm.getProductName().trim().isEmpty()) {
                    throw new ApiException("One of the three (product id, name, barcode) must be present");
                } else {
                    orderItemForm.setProductId(productApi.getByName(orderItemForm.getProductName()).getId());
                    orderItemForm.setBarcode(productApi.getByName(orderItemForm.getProductName()).getBarcode());
                }
            } else {
                orderItemForm.setProductId(productApi.getByBarcode(orderItemForm.getBarcode()).getId());
                orderItemForm.setProductName(productApi.getByBarcode(orderItemForm.getBarcode()).getName());
            }
        } else {
            orderItemForm.setBarcode(productApi.get(orderItemForm.getProductId()).getBarcode());
            orderItemForm.setProductName(productApi.get(orderItemForm.getProductId()).getName());
        }
        // Validate base64 image if provided
        if (orderItemForm.getImage() != null && !orderItemForm.getImage().trim().isEmpty()) {
            if (!isValidBase64(orderItemForm.getImage())) {
                throw new ApiException("Image must be a valid base64 string");
            }
        }
    }

    @Override
    protected OrderItemPojo convertFormToEntity(OrderItemForm orderItemForm) {
        OrderItemPojo orderItemPojo = new OrderItemPojo();
        orderItemPojo.setOrderId(orderItemForm.getOrderId());
        
        // Get product details and set denormalized fields
        var product = productApi.get(orderItemForm.getProductId());
        orderItemPojo.setProductBarcode(product.getBarcode());
        orderItemPojo.setProductName(product.getName());
        orderItemPojo.setClientName(product.getClientName());
        orderItemPojo.setProductMrp(product.getMrp());
        orderItemPojo.setProductImageUrl(product.getImageUrl());
        
        orderItemPojo.setQuantity(orderItemForm.getQuantity());
        orderItemPojo.setSellingPrice(product.getMrp());
        orderItemPojo.setAmount(product.getMrp() * orderItemForm.getQuantity());
        
        // Handle base64 image if provided (this would update the product's image)
        if (orderItemForm.getImage() != null && !orderItemForm.getImage().trim().isEmpty()) {
            // For now, we'll just validate the image
            // In a real implementation, you might want to update the product's image
        }
        return orderItemPojo;
    }

    @Override
    protected OrderItemData convertEntityToData(OrderItemPojo orderItemPojo) {
        OrderItemData orderItemData = new OrderItemData();
        orderItemData.setId(orderItemPojo.getId());
        // Since we don't have productId in OrderItemPojo anymore, we need to look it up by barcode
        try {
            var product = productApi.getByBarcode(orderItemPojo.getProductBarcode());
            orderItemData.setProductId(product.getId());
        } catch (Exception e) {
            // If product not found, set to null
            orderItemData.setProductId(null);
        }
        orderItemData.setBarcode(orderItemPojo.getProductBarcode());
        orderItemData.setProductName(orderItemPojo.getProductName());
        orderItemData.setQuantity(orderItemPojo.getQuantity());
        orderItemData.setSellingPrice(orderItemPojo.getSellingPrice());
        orderItemData.setAmount(orderItemPojo.getAmount());
        
        // Get order date from the order
        try {
            var order = orderApi.get(orderItemPojo.getOrderId());
            orderItemData.setDateTime(TimeUtil.toIST(order.getDate()));
        } catch (Exception e) {
            // If order not found, set to null
            orderItemData.setDateTime(null);
        }
        
        // Set imageUrl as reference to product image endpoint
        if (orderItemPojo.getProductImageUrl() != null && !orderItemPojo.getProductImageUrl().trim().isEmpty()) {
            try {
                var product = productApi.getByBarcode(orderItemPojo.getProductBarcode());
                orderItemData.setImageUrl("/api/products/" + product.getId() + "/image");
            } catch (Exception e) {
                orderItemData.setImageUrl(orderItemPojo.getProductImageUrl());
            }
        }
        return orderItemData;
    }

    public List<OrderItemData> getByOrderId(Integer orderId) {
        if (Objects.isNull(orderId)) {
            throw new ApiException("Order ID cannot be null");
        }
        System.out.println("OrderItemDto: Getting order items for order ID: " + orderId);
        List<OrderItemPojo> orderItemPojoList = orderItemFlow.getByOrderId(orderId);
        System.out.println("OrderItemDto: Found " + orderItemPojoList.size() + " order item POJOs for order " + orderId);
        List<OrderItemData> orderItemDataList = new ArrayList<>();
        for (OrderItemPojo orderItemPojo : orderItemPojoList) {
            System.out.println("OrderItemDto: Converting order item POJO ID: " + orderItemPojo.getId());
            orderItemDataList.add(convertEntityToData(orderItemPojo));
        }
        System.out.println("OrderItemDto: Returning " + orderItemDataList.size() + " order item data objects");
        return orderItemDataList;
    }
    
    private boolean isValidBase64(String str) {
        try {
            Base64.getDecoder().decode(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public OrderItemData update(Integer id, @Valid OrderItemForm form) {
        return super.update(id, form);
    }
}