package org.example.flow;

import org.example.exception.ApiException;
import org.example.pojo.OrderItemPojo;
import org.example.pojo.OrderPojo;
import org.example.service.OrderItemService;
import org.example.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional
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

        // Create the order with all items - OrderService handles the complete creation
        return orderService.add(orderPojo);
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

    public OrderPojo cancelOrder(Integer id) {
        orderService.updateStatusToCancelled(id);
        return orderService.get(id);
    }

    public String generateInvoice(Integer orderId) throws Exception {
        return orderService.generateInvoice(orderId);
    }
}
