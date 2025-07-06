package org.example.api;

import org.example.pojo.InventoryPojo;
import org.example.pojo.OrderItemPojo;
import org.example.dao.OrderItemDao;
import org.example.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Objects;
import org.example.api.ProductApi;

@Service
public class OrderItemApi {

    @Autowired
    private OrderItemDao orderItemDao;

    @Autowired
    private InventoryApi inventoryApi;

    @Autowired
    private ProductApi productApi;

    public OrderItemPojo add(OrderItemPojo orderItemPojo) {
        Integer productId = orderItemPojo.getProduct().getId();
        InventoryPojo inventoryPojo = inventoryApi.getByProductId(productId);

        if (Objects.isNull(inventoryPojo)) {
            throw new ApiException("No inventory found for product ID: " + productId);
        }

        if (orderItemPojo.getQuantity() > inventoryPojo.getQuantity()) {
            throw new ApiException("Quantity must be less than available stock in inventory. Available: " + inventoryPojo.getQuantity() + ", Requested: " + orderItemPojo.getQuantity());
        }

        // ✅ Calculate amount
        double amount = orderItemPojo.getSellingPrice() * orderItemPojo.getQuantity();
        orderItemPojo.setAmount(amount);

        // ✅ Update inventory
        inventoryApi.removeStock(productId, orderItemPojo.getQuantity());

        orderItemDao.insert(orderItemPojo);
        return orderItemPojo;
    }

    public OrderItemPojo get(Integer id) {
        OrderItemPojo orderItem = orderItemDao.select(id);
        if (Objects.isNull(orderItem)) {
            throw new ApiException("Order item with ID " + id + " not found");
        }
        return orderItem;
    }

    public List<OrderItemPojo> getAll() {
        return orderItemDao.selectAll();
    }

    public List<OrderItemPojo> getByOrderId(Integer orderId) {
        return orderItemDao.selectByOrderId(orderId);
    }

    public List<OrderItemPojo> getByProductId(Integer productId) {
        return orderItemDao.selectByProductId(productId);
    }

    public OrderItemPojo update(Integer id, OrderItemPojo updatedOrderItem) {
        OrderItemPojo existingOrderItem = orderItemDao.select(id);
        if (Objects.isNull(existingOrderItem)) {
            throw new ApiException("Order item with ID " + id + " not found");
        }

        InventoryPojo inventoryPojo = inventoryApi.getByProductId(existingOrderItem.getProduct().getId());
        if (Objects.isNull(inventoryPojo)) {
            throw new ApiException("No inventory found for product ID: " + existingOrderItem.getProduct().getId());
        }

        // ✅ Revert old quantity to inventory before check
        int oldQty = existingOrderItem.getQuantity();
        int newQty = updatedOrderItem.getQuantity();
        int available = inventoryPojo.getQuantity() + oldQty;

        if (newQty > available) {
            throw new ApiException("Quantity must be less than available stock in inventory. Available: " + available + ", Requested: " + newQty);
        }

        // ✅ Update inventory - first restore old quantity, then remove new quantity
        inventoryApi.addStock(existingOrderItem.getProduct().getId(), oldQty);
        inventoryApi.removeStock(existingOrderItem.getProduct().getId(), newQty);

        // ✅ Update item and recalculate amount
        existingOrderItem.setOrder(updatedOrderItem.getOrder());
        existingOrderItem.setProduct(updatedOrderItem.getProduct());
        existingOrderItem.setQuantity(newQty);
        existingOrderItem.setSellingPrice(updatedOrderItem.getSellingPrice());

        double amount = updatedOrderItem.getSellingPrice() * newQty;
        existingOrderItem.setAmount(amount);

        orderItemDao.update(id, existingOrderItem);
        return orderItemDao.select(id);
    }

    public void delete(Integer id) {
        OrderItemPojo orderItem = get(id);
        InventoryPojo inventoryPojo = inventoryApi.getByProductId(orderItem.getProduct().getId());

        // ✅ Restore inventory
        inventoryApi.addStock(orderItem.getProduct().getId(), orderItem.getQuantity());

        orderItemDao.delete(id);
    }
} 