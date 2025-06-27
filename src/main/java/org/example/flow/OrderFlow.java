package org.example.flow;

import org.example.pojo.InvoicePojo;
import org.example.pojo.OrderPojo;
import org.example.service.OrderItemService;
import org.example.pojo.OrderItemPojo;
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

    public OrderPojo add(OrderPojo form) {
        return orderService.add(form);
    }

    public OrderPojo get(Integer id) {
        return orderService.get(id);
    }

    public List<OrderPojo> getAll() {
        return orderService.getAll();
    }

    public void delete(Integer id) {
        List<OrderItemPojo> orderItemPojoList = orderItemService.getByOrderId(id);
        for (OrderItemPojo orderItemPojo : orderItemPojoList) {
            orderItemService.delete(orderItemPojo.getId());
        }
        orderService.delete(id);
    }

    public OrderPojo update(Integer id, OrderPojo form) {
        orderService.update(id, form);
        return orderService.get(id);
    }

    public InvoicePojo generateInvoice(Integer orderId) {
        return orderService.generateInvoice(orderId);
    }
}

