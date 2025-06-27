package org.example.dto;

import org.example.flow.OrderFlow;
import org.example.model.OrderItemForm;
import org.example.model.OrderItemData;
import org.example.pojo.OrderItemPojo;
import org.example.flow.OrderItemFlow;

import org.example.service.OrderService;
import org.example.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
            throw new IllegalArgumentException("Order Item ID cannot be null");
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
            throw new IllegalArgumentException("Order ID cannot be null");
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
            throw new IllegalArgumentException("Order Item ID cannot be null");
        }
        validate(orderItemForm);
        return convert(orderItemFlow.update(convert(orderItemForm), id));
    }

    public void delete(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Order Item ID cannot be null");
        }
        orderItemFlow.delete(id);
    }

    public void validate(OrderItemForm orderItemForm) {
        if (orderItemForm.getOrderId() == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        orderItemForm.setDateTime(orderService.get(orderItemForm.getOrderId()).getDate());
        if (orderItemForm.getProductId() == null) {
            if (orderItemForm.getProductBarcode().trim().isEmpty()) {
                if (orderItemForm.getProductName().trim().isEmpty()) {
                    throw new IllegalArgumentException("One of the three (product id, name, barcode) must be present");
                } else {
                    orderItemForm.setProductId(productService.getByName(orderItemForm.getProductName()).getId());
                    orderItemForm.setProductBarcode(productService.getByName(orderItemForm.getProductName()).getBarcode());
                }
            } else {
                orderItemForm.setProductId(productService.getByBarcode(orderItemForm.getProductBarcode()).getId());
                orderItemForm.setProductName(productService.getByBarcode(orderItemForm.getProductBarcode()).getName());
            }
        } else {
            orderItemForm.setProductBarcode(productService.get(orderItemForm.getProductId()).getBarcode());
            orderItemForm.setProductName(productService.get(orderItemForm.getProductId()).getName());
        }
        orderItemForm.setClientId(productService.get(orderItemForm.getProductId()).getClient().getId());
        orderItemForm.setClientName(productService.get(orderItemForm.getProductId()).getClient().getClientName());
        if (orderItemForm.getQuantity() == null) {
            throw new IllegalArgumentException("Order Item quantity cannot be null");
        }
    }

    private OrderItemPojo convert(OrderItemForm orderItemForm) {
        OrderItemPojo orderItemPojo = new OrderItemPojo();
        orderItemPojo.setOrder(orderService.get(orderItemForm.getOrderId()));
        orderItemPojo.setProduct(productService.get(orderItemForm.getOrderId()));
        orderItemPojo.setQuantity(orderItemForm.getQuantity());
        orderItemPojo.setSellingPrice(productService.get(orderItemForm.getProductId()).getMrp());
        return orderItemPojo;
    }

    private OrderItemData convert(OrderItemPojo orderItemPojo) {
        OrderItemData orderItemData = new OrderItemData();
        orderItemData.setId(orderItemPojo.getId());
        orderItemData.setOrderId(orderItemPojo.getOrder().getId());
        orderItemData.setDateTime(orderItemPojo.getOrder().getDate());
        orderItemData.setProductId(orderItemPojo.getProduct().getId());
        orderItemData.setProductBarcode(orderItemPojo.getProduct().getBarcode());
        orderItemData.setProductName(orderItemPojo.getProduct().getName());
        orderItemData.setClientId(orderItemPojo.getProduct().getClient().getId());
        orderItemData.setClientName(orderItemPojo.getProduct().getClient().getClientName());
        orderItemData.setQuantity(orderItemPojo.getQuantity());
        orderItemData.setSellingPrice(orderItemPojo.getSellingPrice());
        return orderItemData;
    }
}