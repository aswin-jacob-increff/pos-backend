package org.example.dto;

import org.example.model.form.OrderItemForm;
import org.example.model.data.OrderItemData;
import org.example.pojo.OrderItemPojo;
import org.example.flow.OrderItemFlow;
import org.example.api.OrderApi;
import org.example.api.ProductApi;
import org.example.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import jakarta.validation.Valid;
import org.example.util.TimeUtil;
import org.example.api.ClientApi;

@Component
public class OrderItemDto extends AbstractDto<OrderItemPojo, OrderItemForm, OrderItemData> {

    @Autowired
    private OrderItemFlow orderItemFlow;

    @Autowired
    private OrderApi orderApi;

    @Autowired
    private ProductApi productApi;

    @Autowired
    private ClientApi clientApi;

    @Override
    protected String getEntityName() {
        return "OrderItem";
    }

    @Override
    protected void preprocess(OrderItemForm orderItemForm) {
        // Cross-field/entity logic: orderId validation, productId validation
        if (Objects.isNull(orderItemForm.getOrderId())) {
            throw new ApiException("Order ID cannot be null");
        }
        
        // Validate productId is provided
        if (Objects.isNull(orderItemForm.getProductId())) {
            throw new ApiException("Product ID is required");
        }
        
        // Validate that the product exists
        try {
            var product = productApi.get(orderItemForm.getProductId());
            if (product == null) {
                throw new ApiException("Product with ID " + orderItemForm.getProductId() + " not found");
            }
        } catch (Exception e) {
            throw new ApiException("Product with ID " + orderItemForm.getProductId() + " not found");
        }
    }

    @Override
    protected OrderItemPojo convertFormToEntity(OrderItemForm orderItemForm) {
        OrderItemPojo orderItemPojo = new OrderItemPojo();
        orderItemPojo.setOrderId(orderItemForm.getOrderId());
        orderItemPojo.setProductId(orderItemForm.getProductId());
        orderItemPojo.setQuantity(orderItemForm.getQuantity());
        
        // Get product details for pricing
        var product = productApi.get(orderItemForm.getProductId());
        if (product == null) {
            throw new ApiException("Product with ID " + orderItemForm.getProductId() + " not found");
        }
        
        orderItemPojo.setSellingPrice(product.getMrp());
        orderItemPojo.setAmount(product.getMrp() * orderItemForm.getQuantity());
        
        return orderItemPojo;
    }

    @Override
    protected OrderItemData convertEntityToData(OrderItemPojo orderItemPojo) {
        OrderItemData orderItemData = new OrderItemData();
        orderItemData.setId(orderItemPojo.getId());
        orderItemData.setOrderId(orderItemPojo.getOrderId());
        orderItemData.setProductId(orderItemPojo.getProductId());
        orderItemData.setQuantity(orderItemPojo.getQuantity());
        orderItemData.setSellingPrice(orderItemPojo.getSellingPrice());
        orderItemData.setAmount(orderItemPojo.getAmount());
        
        // Fetch product information using productId
        org.example.pojo.ProductPojo product = null;
        try {
            product = productApi.get(orderItemPojo.getProductId());
        } catch (Exception e) {
            // Product not found, continue with null values
        }
        
        if (product != null) {
            orderItemData.setBarcode(product.getBarcode());
            orderItemData.setProductName(product.getName());
            orderItemData.setImageUrl(product.getImageUrl());
            orderItemData.setClientId(product.getClientId());
            
            // Fetch client information
            if (product.getClientId() != null && product.getClientId() > 0) {
                try {
                    org.example.pojo.ClientPojo client = clientApi.get(product.getClientId());
                    if (client != null) {
                        orderItemData.setClientName(client.getClientName());
                    }
                } catch (Exception e) {
                    // Client not found, continue with null
                }
            }
        }
        
        // Get order date from the order
        try {
            var order = orderApi.get(orderItemPojo.getOrderId());
            orderItemData.setDateTime(TimeUtil.toIST(order.getDate()));
        } catch (Exception e) {
            // If order not found, set to null
            orderItemData.setDateTime(null);
        }
        
        return orderItemData;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public OrderItemData add(@Valid OrderItemForm form) {
        if (Objects.isNull(form)) {
            throw new ApiException("Order item form cannot be null");
        }
        return super.add(form);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public OrderItemData update(Integer id, @Valid OrderItemForm form) {
        if (Objects.isNull(id)) {
            throw new ApiException("Order item ID cannot be null");
        }
        if (Objects.isNull(form)) {
            throw new ApiException("Order item form cannot be null");
        }
        return super.update(id, form);
    }

    @Override
    public OrderItemData get(Integer id) {
        if (Objects.isNull(id)) {
            throw new ApiException("Order item ID cannot be null");
        }
        return super.get(id);
    }

    public List<OrderItemData> getByOrderId(Integer orderId) {
        if (Objects.isNull(orderId)) {
            throw new ApiException("Order ID cannot be null");
        }
        if (orderId <= 0) {
            throw new ApiException("Order ID must be positive");
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
    

}