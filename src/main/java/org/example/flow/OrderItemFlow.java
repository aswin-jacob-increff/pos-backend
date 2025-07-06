package org.example.flow;

import org.example.pojo.OrderItemPojo;
import org.example.api.OrderItemApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OrderItemFlow {

    @Autowired
    private OrderItemApi api;

    public OrderItemPojo add(OrderItemPojo orderItemPojo) {
        calculateAmount(orderItemPojo);
        return api.add(orderItemPojo);
    }

    public OrderItemPojo get(Integer id) {
        return api.get(id);
    }

    public List<OrderItemPojo> getAll() {
        return api.getAll();
    }

    public List<OrderItemPojo> getByOrderId(Integer orderId) {
        return api.getByOrderId(orderId);
    }

    public OrderItemPojo update(OrderItemPojo orderItemPojo, Integer id) {
        calculateAmount(orderItemPojo);
        return api.update(id, orderItemPojo);
    }

    public void delete(Integer id) {
        api.delete(id);
    }

    private void calculateAmount(OrderItemPojo item) {
        if (item.getSellingPrice() != null && item.getQuantity() != null) {
            item.setAmount(item.getSellingPrice() * item.getQuantity());
        } else {
            throw new RuntimeException("Selling price and quantity must not be null");
        }
    }
}
