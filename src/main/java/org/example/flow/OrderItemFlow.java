package org.example.flow;

import org.example.pojo.OrderItemPojo;
import org.example.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderItemFlow {

    @Autowired
    private OrderItemService orderItemService;

    public OrderItemPojo add(OrderItemPojo orderItemPojo) {
        return orderItemService.add(orderItemPojo);
    }

    public OrderItemPojo get(Integer id) {
        return orderItemService.get(id);
    }

    public List<OrderItemPojo> getAll() {
        return orderItemService.getAll();
    }

    public List<OrderItemPojo> getByOrderId(Integer orderId) {
        return orderItemService.getByOrderId(orderId);
    }

    public OrderItemPojo update(OrderItemPojo orderItemPojo, Integer id) {
        return orderItemService.update(id, orderItemPojo);
    }

    public void delete(Integer id) {
        orderItemService.delete(id);
    }
}
