package org.example.flow;

import org.example.pojo.OrderItemPojo;
import org.example.api.OrderItemApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OrderItemFlow extends AbstractFlow<OrderItemPojo> {

    @Autowired
    private OrderItemApi api;

    @Override
    protected Integer getEntityId(OrderItemPojo entity) {
        return entity.getId();
    }

    @Override
    protected String getEntityName() {
        return "OrderItem";
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public OrderItemPojo add(OrderItemPojo orderItemPojo) {
        calculateAmount(orderItemPojo);
        api.add(orderItemPojo);
        return orderItemPojo;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public OrderItemPojo update(Integer id, OrderItemPojo orderItemPojo) {
        calculateAmount(orderItemPojo);
        api.update(id, orderItemPojo);
        return api.get(id);
    }

    public List<OrderItemPojo> getByOrderId(Integer orderId) {
        return api.getByOrderId(orderId);
    }

    private void calculateAmount(OrderItemPojo item) {
        if (item.getSellingPrice() != null && item.getQuantity() != null) {
            item.setAmount(item.getSellingPrice() * item.getQuantity());
        } else {
            throw new RuntimeException("Selling price and quantity must not be null");
        }
    }
}
