package org.example.service;

import jakarta.transaction.Transactional;
import org.example.pojo.InventoryPojo;
import org.example.pojo.OrderItemPojo;
import org.example.dao.OrderItemDao;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
@Transactional
public class OrderItemService {

    @Autowired
    private OrderItemDao orderItemDao;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ProductService productService;

    public OrderItemPojo add(OrderItemPojo orderItemPojo) {
        Integer productId = orderItemPojo.getProduct().getId();
        InventoryPojo inventoryPojo = inventoryService.getByProductId(productId);

        if (orderItemPojo.getQuantity() > inventoryPojo.getQuantity()) {
            throw new RuntimeException("Quantity must be less than available stock in inventory");
        }

        // ✅ Calculate amount
        double amount = orderItemPojo.getSellingPrice() * orderItemPojo.getQuantity();
        orderItemPojo.setAmount(amount);

        // ✅ Update inventory
        inventoryPojo.setQuantity(inventoryPojo.getQuantity() - orderItemPojo.getQuantity());
        inventoryService.update(inventoryPojo.getId(), inventoryPojo);

        orderItemDao.insert(orderItemPojo);
        return orderItemPojo;
    }

    public OrderItemPojo get(Integer id) {
        return orderItemDao.select(id);
    }

    public List<OrderItemPojo> getAll() {
        return orderItemDao.selectAll();
    }

    public List<OrderItemPojo> getByOrderId(Integer orderId) {
        return orderItemDao.selectByOrderId(orderId);
    }

    public OrderItemPojo update(Integer id, OrderItemPojo updatedOrderItem) {
        OrderItemPojo existingOrderItem = orderItemDao.select(id);

        InventoryPojo inventoryPojo = inventoryService.getByProductId(existingOrderItem.getProduct().getId());

        // ✅ Revert old quantity to inventory before check
        int oldQty = existingOrderItem.getQuantity();
        int newQty = updatedOrderItem.getQuantity();
        int available = inventoryPojo.getQuantity() + oldQty;

        if (newQty > available) {
            throw new RuntimeException("Quantity must be less than available stock in inventory");
        }

        // ✅ Update inventory
        inventoryPojo.setQuantity(available - newQty);
        inventoryService.update(inventoryPojo.getId(), inventoryPojo);

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
        InventoryPojo inventoryPojo = inventoryService.getByProductId(orderItem.getProduct().getId());

        // ✅ Restore inventory
        inventoryPojo.setQuantity(inventoryPojo.getQuantity() + orderItem.getQuantity());
        inventoryService.update(inventoryPojo.getId(), inventoryPojo);

        orderItemDao.delete(id);
    }
}
