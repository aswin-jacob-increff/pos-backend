package org.example.dto;

import org.example.pojo.OrderStatus;
import org.example.flow.OrderFlow;
import org.example.flow.ProductFlow;
import org.example.model.OrderData;
import org.example.model.OrderForm;
import org.example.model.OrderItemData;
import org.example.model.OrderItemForm;
import org.example.pojo.OrderItemPojo;
import org.example.pojo.OrderPojo;
import org.example.pojo.ProductPojo;
import org.example.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrderDto {

    @Autowired
    private OrderFlow orderFlow;

    @Autowired
    private ProductFlow productFlow;

    public OrderData add(OrderForm orderForm) {
        validateOrderForm(orderForm);
        
        OrderPojo orderPojo = orderFlow.add(convert(orderForm));
        return convert(orderPojo);
    }

    public OrderData get(Integer id) {
        validateOrderId(id);
        return convert(orderFlow.get(id));
    }

    public List<OrderData> getAll() {
        List<OrderPojo> orderPojoList = orderFlow.getAll();
        List<OrderData> orderDataList = new ArrayList<>();
        for (OrderPojo orderPojo : orderPojoList) {
            orderDataList.add(convert(orderPojo));
        }
        return orderDataList;
    }

    public OrderData update(Integer id, OrderForm orderForm) {
        validateOrderId(id);
        validateOrderForm(orderForm);
        
        return convert(orderFlow.update(id, convert(orderForm)));
    }

    public void delete(Integer id) {
        validateOrderId(id);
        orderFlow.delete(id);
    }
    
    public OrderData cancelOrder(Integer id) {
        validateOrderId(id);
        return convert(orderFlow.cancelOrder(id));
    }
    
    private void validateOrderId(Integer id) {
        if (id == null) {
            throw new ApiException("Order ID cannot be null");
        }
        if (id <= 0) {
            throw new ApiException("Order ID must be positive");
        }
    }
    
    private void validateOrderForm(OrderForm orderForm) {
        if (orderForm == null) {
            throw new ApiException("Order form cannot be null");
        }
        
        if (orderForm.getOrderItemFormList() == null || orderForm.getOrderItemFormList().isEmpty()) {
            throw new ApiException("Order must contain at least one order item");
        }
        
        // Validate each order item
        for (OrderItemForm itemForm : orderForm.getOrderItemFormList()) {
            validateOrderItemForm(itemForm);
        }
    }
    
    private void validateOrderItemForm(OrderItemForm itemForm) {
        if (itemForm == null) {
            throw new ApiException("Order item form cannot be null");
        }
        
        if (itemForm.getProductId() == null) {
            throw new ApiException("Product ID cannot be null for order item");
        }
        if (itemForm.getProductId() <= 0) {
            throw new ApiException("Product ID must be positive");
        }
        
        if (itemForm.getQuantity() == null || itemForm.getQuantity() <= 0) {
            throw new ApiException("Quantity must be greater than 0 for product ID: " + itemForm.getProductId());
        }
        
        if (itemForm.getSellingPrice() == null || itemForm.getSellingPrice() <= 0) {
            throw new ApiException("Selling price must be greater than 0 for product ID: " + itemForm.getProductId());
        }
    }

    private OrderPojo convert(OrderForm orderForm) {
        OrderPojo orderPojo = new OrderPojo();
        orderPojo.setDate(orderForm.getDate());
        orderPojo.setStatus(orderForm.getStatus());
        orderPojo.setTotal(0.0);

        // Convert list of OrderItemForm to OrderItemPojo
        if (orderForm.getOrderItemFormList() != null) {
            List<OrderItemPojo> itemPojoList = new ArrayList<>();
            for (OrderItemForm itemForm : orderForm.getOrderItemFormList()) {
                OrderItemPojo itemPojo = new OrderItemPojo();
                itemPojo.setQuantity(itemForm.getQuantity());
                itemPojo.setSellingPrice(itemForm.getSellingPrice());
                itemPojo.setAmount(itemForm.getSellingPrice() * itemForm.getQuantity());

                // Validate and get the actual product from database
                ProductPojo product = productFlow.get(itemForm.getProductId());
                if (product == null) {
                    throw new ApiException("Product with ID " + itemForm.getProductId() + " not found");
                }
                
                itemPojo.setProduct(product);
                itemPojoList.add(itemPojo);
            }
            orderPojo.setOrderItems(itemPojoList);
        }

        return orderPojo;
    }

    private OrderData convert(OrderPojo orderPojo) {
        OrderData orderData = new OrderData();
        orderData.setId(orderPojo.getId());
        orderData.setDate(orderPojo.getDate());
        orderData.setTotal(orderPojo.getTotal());
        orderData.setStatus(orderPojo.getStatus());

        if (orderPojo.getOrderItems() != null) {
            List<OrderItemData> orderItemDataList = new ArrayList<>();
            for (OrderItemPojo itemPojo : orderPojo.getOrderItems()) {
                OrderItemData itemData = new OrderItemData();
                itemData.setId(itemPojo.getId());
                itemData.setQuantity(itemPojo.getQuantity());
                itemData.setSellingPrice(itemPojo.getSellingPrice());
                itemData.setAmount(itemPojo.getAmount());

                // This can be expanded using productDto if needed
                itemData.setProductId(itemPojo.getProduct().getId());
                itemData.setProductName(itemPojo.getProduct().getName());
                
                // Add product image to order item data
                if (itemPojo.getProduct().getImageUrl() != null && !itemPojo.getProduct().getImageUrl().trim().isEmpty()) {
                    itemData.setImageUrl(itemPojo.getProduct().getImageUrl());
                }

                orderItemDataList.add(itemData);
            }
            orderData.setOrderItemDataList(orderItemDataList);
        }

        return orderData;
    }

    public String generateInvoice(Integer orderId) throws Exception {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        return orderFlow.generateInvoice(orderId);
    }
}
