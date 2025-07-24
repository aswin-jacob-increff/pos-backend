package org.example.api;

import org.example.dao.OrderItemDao;
import org.example.exception.ApiException;
import org.example.pojo.OrderItemPojo;
import org.example.pojo.InventoryPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;

@Service
public class OrderItemApi extends AbstractApi<OrderItemPojo> {

    @Autowired
    private OrderItemDao orderItemDao;

    @Autowired
    private InventoryApi inventoryApi;

    @Autowired
    private ProductApi productApi;

    @Override
    protected String getEntityName() {
        return "OrderItem";
    }

    // Unique add logic with inventory management
    @Override
    public void add(OrderItemPojo orderItemPojo) {
        if (Objects.isNull(orderItemPojo)) {
            throw new ApiException("Order item cannot be null");
        }
        if (Objects.isNull(orderItemPojo.getOrderId())) {
            throw new ApiException("Order ID cannot be null");
        }
        if (Objects.isNull(orderItemPojo.getProductId())) {
            throw new ApiException("Product ID cannot be null");
        }
        
        // Get product from productId
        var product = productApi.get(orderItemPojo.getProductId());
        if (product == null) {
            throw new ApiException("Product with ID " + orderItemPojo.getProductId() + " not found");
        }
        
        InventoryPojo inventoryPojo = inventoryApi.getByProductId(product.getId());
        if (Objects.isNull(inventoryPojo)) {
            throw new ApiException("No inventory found for product ID: " + orderItemPojo.getProductId());
        }
        if (orderItemPojo.getQuantity() > inventoryPojo.getQuantity()) {
            throw new ApiException("Insufficient stock. Available: " + inventoryPojo.getQuantity() + ", Requested: " + orderItemPojo.getQuantity());
        }
        // Calculate amount
        double amount = orderItemPojo.getSellingPrice() * orderItemPojo.getQuantity();
        orderItemPojo.setAmount(amount);
        // Update inventory
        inventoryApi.removeStock(product.getId(), orderItemPojo.getQuantity());
        orderItemDao.insert(orderItemPojo);
    }

    public OrderItemPojo get(Integer id) {
        if (id == null) {
            throw new ApiException("Order Item ID cannot be null");
        }
        OrderItemPojo orderItem = orderItemDao.select(id);
        if (orderItem == null) {
            throw new ApiException("Order Item with ID " + id + " not found");
        }
        return orderItem;
    }

    // Unique update logic with inventory management
    @Override
    public void update(Integer id, OrderItemPojo updatedOrderItem) {
        if (Objects.isNull(id)) {
            throw new ApiException("Order item ID cannot be null");
        }
        if (Objects.isNull(updatedOrderItem)) {
            throw new ApiException("Order item cannot be null");
        }
        OrderItemPojo existingOrderItem = orderItemDao.select(id);
        if (Objects.isNull(existingOrderItem)) {
            throw new ApiException("Order item with ID " + id + " not found");
        }
        
        // Get product from existing productId
        var existingProduct = productApi.get(existingOrderItem.getProductId());
        if (existingProduct == null) {
            throw new ApiException("Product with ID " + existingOrderItem.getProductId() + " not found");
        }
        
        InventoryPojo inventoryPojo = inventoryApi.getByProductId(existingProduct.getId());
        if (Objects.isNull(inventoryPojo)) {
            throw new ApiException("No inventory found for product ID: " + existingOrderItem.getProductId());
        }
        // Revert old quantity to inventory before check
        int oldQty = existingOrderItem.getQuantity();
        int newQty = updatedOrderItem.getQuantity();
        int available = inventoryPojo.getQuantity() + oldQty;
        if (newQty > available) {
            throw new ApiException("Insufficient stock. Available: " + available + ", Requested: " + newQty);
        }
        // Update inventory - first restore old quantity, then remove new quantity
        inventoryApi.addStock(existingProduct.getId(), oldQty);
        inventoryApi.removeStock(existingProduct.getId(), newQty);
        // Update item and recalculate amount
        existingOrderItem.setOrderId(updatedOrderItem.getOrderId());
        existingOrderItem.setProductId(updatedOrderItem.getProductId());
        existingOrderItem.setQuantity(newQty);
        existingOrderItem.setSellingPrice(updatedOrderItem.getSellingPrice());
        double amount = updatedOrderItem.getSellingPrice() * newQty;
        existingOrderItem.setAmount(amount);
        
        orderItemDao.update(id, existingOrderItem);
    }

    public List<OrderItemPojo> getByOrderId(Integer orderId) {
        if (Objects.isNull(orderId)) {
            throw new ApiException("Order ID cannot be null");
        }
        System.out.println("OrderItemApi: Getting order items for order ID: " + orderId);
        List<OrderItemPojo> result = orderItemDao.selectByOrderId(orderId);
        System.out.println("OrderItemApi: Found " + result.size() + " order items from DAO for order " + orderId);
        return result;
    }

    public List<OrderItemPojo> getByProductId(Integer productId) {
        if (Objects.isNull(productId)) {
            throw new ApiException("Product ID cannot be null");
        }
        return orderItemDao.selectByProductId(productId);
    }
} 