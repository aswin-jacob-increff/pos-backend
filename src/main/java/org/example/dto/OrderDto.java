package org.example.dto;

import jakarta.persistence.criteria.CriteriaBuilder;
import org.example.model.InvoiceData;
import org.example.model.OrderForm;
import org.example.model.OrderData;
import org.example.pojo.InvoicePojo;
import org.example.pojo.OrderPojo;
import org.example.flow.OrderFlow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrderDto {

    @Autowired
    private OrderFlow orderFlow;

    public OrderData add(OrderForm orderForm) {
        OrderPojo orderPojo = orderFlow.add(convert(orderForm));
        return convert(orderPojo);
    }

    public OrderData get(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        return convert(orderFlow.get(id));
    }

    public List<OrderData> getAll() {
        List<OrderPojo> orderPojoList = orderFlow.getAll();
        List<OrderData> orderDataList = new ArrayList<>();
        for(OrderPojo orderPojo : orderPojoList) {
            orderDataList.add(convert(orderPojo));
        }
        return orderDataList;
    }

    public OrderData update(Integer id, OrderForm orderForm) {
        if (id == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        return convert(orderFlow.update(id, convert(orderForm)));
    }

    public void delete(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        orderFlow.delete(id);
    }

    OrderPojo convert(OrderForm orderForm) {
        OrderPojo orderPojo = new OrderPojo();
        orderPojo.setDateTime(orderForm.getDate());
        return orderPojo;
    }

    OrderData convert(OrderPojo orderPojo) {
        OrderData orderData = new OrderData();
        orderData.setId(orderPojo.getId());
        orderData.setDate(orderPojo.getDate());
        return orderData;
    }
}
