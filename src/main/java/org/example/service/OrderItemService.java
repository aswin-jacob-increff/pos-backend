package org.example.service;

import jakarta.transaction.Transactional;
import org.example.pojo.InventoryPojo;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import org.example.dao.OrderItemDao;
import org.example.pojo.OrderItemPojo;

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
        if (orderItemPojo.getQuantity() > inventoryService.getByProductId(orderItemPojo.getProduct().getId()).getQuantity()) {
            throw new RuntimeException("Quantity must be less than available stock in inventory");
        }
        InventoryPojo inventoryPojo = inventoryService.getByProductId(orderItemPojo.getProduct().getId());
        Integer inventoryQuantity = inventoryPojo.getQuantity();
        Integer orderItemQuantity = orderItemPojo.getQuantity();
        inventoryQuantity = inventoryQuantity - orderItemQuantity;
        inventoryPojo.setQuantity(inventoryQuantity);
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
        Integer inventoryQuantity = inventoryPojo.getQuantity();
        inventoryQuantity += existingOrderItem.getQuantity();
        if (updatedOrderItem.getQuantity() > inventoryQuantity) {
            throw new RuntimeException("Quantity must be less than available stock in inventory");
        }
        inventoryQuantity -= updatedOrderItem.getQuantity();
        inventoryPojo.setQuantity(inventoryQuantity);
        inventoryService.update(inventoryPojo.getId(), inventoryPojo);
        existingOrderItem.setOrder(updatedOrderItem.getOrder());
        existingOrderItem.setProduct(updatedOrderItem.getProduct());
        existingOrderItem.setQuantity(updatedOrderItem.getQuantity());
        existingOrderItem.setSellingPrice(updatedOrderItem.getSellingPrice());
        orderItemDao.update(id, existingOrderItem);
        return orderItemDao.select(id);
    }

    public void delete(Integer id) {
        InventoryPojo inventoryPojo = inventoryService.getByProductId(get(id).getProduct().getId());
        Integer inventoryQuantity = inventoryPojo.getQuantity();
        inventoryQuantity += get(id).getQuantity();
        inventoryPojo.setQuantity(inventoryQuantity);
        inventoryService.update(inventoryPojo.getId(), inventoryPojo);
        orderItemDao.delete(id);
    }
}
