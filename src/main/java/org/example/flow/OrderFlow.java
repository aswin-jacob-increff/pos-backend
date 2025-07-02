package org.example.flow;

import org.example.exception.ApiException;
import org.example.pojo.OrderItemPojo;
import org.example.pojo.OrderPojo;
import org.example.service.OrderItemService;
import org.example.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderFlow {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    public OrderPojo add(OrderPojo orderPojo) {
        List<OrderItemPojo> orderItems = orderPojo.getOrderItems();

        if (orderItems == null || orderItems.isEmpty()) {
            throw new ApiException("Order must contain at least one item");
        }

        // First, create the order
        OrderPojo createdOrder = orderService.add(orderPojo);

        // Then add each order item, linking it to the created order
        for (OrderItemPojo item : orderItems) {
            item.setOrder(createdOrder);
            orderItemService.add(item);
        }

        return createdOrder;
    }

    public OrderPojo get(Integer id) {
        return orderService.get(id);
    }

    public List<OrderPojo> getAll() {
        return orderService.getAll();
    }

    public OrderPojo update(Integer id, OrderPojo form) {
        return orderService.update(id, form);
    }

    public void delete(Integer id) {
        List<OrderItemPojo> orderItemPojoList = orderItemService.getByOrderId(id);
        for (OrderItemPojo orderItemPojo : orderItemPojoList) {
            orderItemService.delete(orderItemPojo.getId());
        }
        orderService.delete(id);
    }
}
