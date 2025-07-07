package org.example.api;

import org.example.dao.OrderDao;
import org.example.exception.ApiException;
import org.example.pojo.OrderItemPojo;
import org.example.pojo.OrderPojo;
import org.example.api.InvoiceApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;
import java.util.Objects;


import org.example.util.TimeUtil;

@Service
public class OrderApi {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderItemApi orderItemApi;

    @Autowired
    private InventoryApi inventoryApi;



    @Autowired
    private InvoiceApi invoiceApi;

    public OrderPojo add(OrderPojo orderPojo) {
        if (Objects.isNull(orderPojo)) {
            throw new ApiException("Order cannot be null");
        }
        if (Objects.isNull(orderPojo.getOrderItems()) || orderPojo.getOrderItems().isEmpty()) {
            throw new ApiException("Order must contain at least one item");
        }
        // Set default date to current UTC time if not provided
        orderPojo.setDate(Objects.nonNull(orderPojo.getDate()) ? orderPojo.getDate() : Instant.now());
        // Set status to CREATED when order is created
        orderPojo.setStatus(org.example.pojo.OrderStatus.CREATED);
        
        // Insert order first to get the ID
        orderDao.insert(orderPojo);
        
        // Combine order items with the same product ID
        List<OrderItemPojo> combinedItems = combineOrderItems(orderPojo.getOrderItems());
        
        double totalAmount = 0.0;
        for (OrderItemPojo item : combinedItems) {
            if (Objects.isNull(item)) {
                throw new ApiException("Order item cannot be null");
            }
            item.setOrder(orderPojo); // link order to item
            double amount = item.getSellingPrice() * item.getQuantity();
            item.setAmount(amount);
            orderItemApi.add(item);
            totalAmount += amount;
        }
        orderPojo.setTotal(totalAmount);
        orderDao.update(orderPojo.getId(), orderPojo);
        
        // Note: Invoice generation is now handled separately, not automatically on order creation
        // Order status remains CREATED until invoice is explicitly generated
        
        return orderPojo;
    }

    public OrderPojo get(Integer id) {
        OrderPojo order = orderDao.select(id);
        if (Objects.isNull(order)) {
            throw new ApiException("Order with ID " + id + " not found");
        }
        return order;
    }

    public List<OrderPojo> getAll() {
        return orderDao.selectAll();
    }

    public OrderPojo update(Integer id, OrderPojo updatedOrder) {
        OrderPojo existingOrder = orderDao.select(id);
        if (Objects.isNull(existingOrder)) {
            throw new ApiException("Order with ID " + id + " not found");
        }
        // Preserve date conversion - assume incoming date is already in UTC (from DTO conversion)
        existingOrder.setDate(updatedOrder.getDate());
        existingOrder.setTotal(updatedOrder.getTotal());
        existingOrder.setOrderItems(updatedOrder.getOrderItems());
        // Update status if provided
        if (updatedOrder.getStatus() != null) {
            existingOrder.setStatus(updatedOrder.getStatus());
        }

        orderDao.update(id, existingOrder);
        return orderDao.select(id);
    }

    public void delete(Integer id) {
        OrderPojo order = orderDao.select(id);
        if (Objects.isNull(order)) {
            throw new ApiException("Order with ID " + id + " not found");
        }
        orderDao.delete(id);
    }

    public void cancelOrder(Integer orderId) {
        OrderPojo order = orderDao.select(orderId);
        if (Objects.isNull(order)) {
            throw new ApiException("Order with ID " + orderId + " not found");
        }
        
        // Restore inventory quantities for all order items
        List<OrderItemPojo> orderItems = orderItemApi.getByOrderId(orderId);
        for (OrderItemPojo orderItem : orderItems) {
            Integer productId = orderItem.getProduct().getId();
            Integer quantityToRestore = orderItem.getQuantity();
            
            // Add the quantity back to inventory
            inventoryApi.addStock(productId, quantityToRestore);
        }
        
        // Delete the order (this will cascade to order items)
        orderDao.delete(orderId);
    }

    public String generateInvoice(Integer orderId) throws Exception {
        // This method is now handled by OrderDto.downloadInvoice()
        throw new ApiException("Use OrderDto.downloadInvoice() instead");
    }

    /**
     * Combines order items with the same product ID by adding their quantities
     * and using the selling price from the first occurrence
     */
    private List<OrderItemPojo> combineOrderItems(List<OrderItemPojo> orderItems) {
        java.util.Map<Integer, OrderItemPojo> combinedMap = new java.util.HashMap<>();
        
        for (OrderItemPojo item : orderItems) {
            Integer productId = item.getProduct().getId();
            
            if (combinedMap.containsKey(productId)) {
                // Product already exists, add quantities
                OrderItemPojo existingItem = combinedMap.get(productId);
                existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
            } else {
                // New product, add to map
                combinedMap.put(productId, item);
            }
        }
        
        return new java.util.ArrayList<>(combinedMap.values());
    }

    public void updateStatus(Integer id, org.example.pojo.OrderStatus status) {
        OrderPojo order = orderDao.select(id);
        if (order == null) throw new ApiException("Order not found");
        order.setStatus(status);
        orderDao.update(id, order);
    }
} 