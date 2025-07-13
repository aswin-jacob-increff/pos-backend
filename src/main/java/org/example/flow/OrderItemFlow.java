package org.example.flow;

import org.example.pojo.OrderItemPojo;
import org.example.api.OrderItemApi;
import org.example.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;

@Service
public class OrderItemFlow extends AbstractFlow<OrderItemPojo> {

    @Autowired
    private OrderItemApi api;

    public OrderItemFlow() {
        super(OrderItemPojo.class);
    }

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
        if (Objects.isNull(orderItemPojo)) {
            throw new ApiException("Order item cannot be null");
        }
        calculateAmount(orderItemPojo);
        api.add(orderItemPojo);
        return orderItemPojo;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void update(Integer id, OrderItemPojo entity) {
        if (id == null) {
            throw new ApiException("ID cannot be null");
        }
        if (entity == null) {
            throw new ApiException("Entity cannot be null");
        }
        api.update(id, entity);
    }

    public List<OrderItemPojo> getAll() {
        return api.getAll();
    }

    public List<OrderItemPojo> getByOrderId(Integer orderId) {
        if (Objects.isNull(orderId)) {
            throw new ApiException("Order ID cannot be null");
        }
        return api.getByOrderId(orderId);
    }

    public OrderItemPojo get(Integer id) {
        if (id == null) {
            throw new ApiException("Order Item ID cannot be null");
        }
        return api.get(id);
    }

    private void calculateAmount(OrderItemPojo item) {
        if (item.getSellingPrice() != null && item.getQuantity() != null) {
            item.setAmount(item.getSellingPrice() * item.getQuantity());
        } else {
            throw new ApiException("Selling price and quantity must not be null");
        }
    }
}
