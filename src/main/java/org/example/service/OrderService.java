package org.example.service;

import org.example.dao.InvoiceDao;
import org.example.dao.InvoiceItemDao;
import org.example.dao.OrderDao;
import org.example.exception.ApiException;
import org.example.pojo.InvoiceItemPojo;
import org.example.pojo.InvoicePojo;
import org.example.pojo.OrderItemPojo;
import org.example.pojo.OrderPojo;
import org.example.pojo.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private InvoiceDao invoiceDao;

    @Autowired
    private InvoiceItemDao invoiceItemDao;

    @Autowired
    private InventoryService inventoryService;

    public OrderPojo add(OrderPojo orderPojo) {
        orderPojo.setDate(Instant.now());
        
        // Set default status if not provided
        if (orderPojo.getStatus() == null) {
            orderPojo.setStatus(OrderStatus.CREATED);
        }

        // 1. Save order to generate ID
        orderDao.insert(orderPojo);

        // 2. Add each order item
        List<OrderItemPojo> orderItems = orderPojo.getOrderItems();
        double totalAmount = 0.0;

        for (OrderItemPojo item : orderItems) {
            item.setOrder(orderPojo); // link order to item

            // Calculate amount in backend
            double amount = item.getSellingPrice() * item.getQuantity();
            item.setAmount(amount);

            orderItemService.add(item);

            totalAmount += amount;
        }

        // 3. Set total and update order again
        orderPojo.setTotal(totalAmount);
        orderDao.update(orderPojo.getId(), orderPojo);

        return orderPojo;
    }

    public OrderPojo get(Integer id) {
        OrderPojo order = orderDao.select(id);
        if (order == null) {
            throw new ApiException("Order with ID " + id + " not found");
        }
        return order;
    }

    public List<OrderPojo> getAll() {
        return orderDao.selectAll();
    }

    public OrderPojo update(Integer id, OrderPojo updatedOrder) {
        OrderPojo existingOrder = orderDao.select(id);
        if (existingOrder == null) {
            throw new ApiException("Order with ID " + id + " not found");
        }
        
        existingOrder.setDate(updatedOrder.getDate());
        
        // Update status if provided and valid
        if (updatedOrder.getStatus() != null) {
            validateStatusTransition(existingOrder.getStatus(), updatedOrder.getStatus());
            existingOrder.setStatus(updatedOrder.getStatus());
        }

        orderDao.update(id, existingOrder);
        return orderDao.select(id);
    }

    public void delete(Integer id) {
        OrderPojo order = orderDao.select(id);
        if (order == null) {
            throw new ApiException("Order with ID " + id + " not found");
        }
        orderDao.delete(id);
    }
    
    /**
     * Update order status to INVOICED when invoice is generated
     */
    public void updateStatusToInvoiced(Integer orderId) {
        OrderPojo order = orderDao.select(orderId);
        if (order == null) {
            throw new ApiException("Order with ID " + orderId + " not found");
        }
        
        validateStatusTransition(order.getStatus(), OrderStatus.INVOICED);
        order.setStatus(OrderStatus.INVOICED);
        orderDao.update(orderId, order);
    }
    
    /**
     * Update order status to CANCELLED
     */
    public void updateStatusToCancelled(Integer orderId) {
        OrderPojo order = orderDao.select(orderId);
        if (order == null) {
            throw new ApiException("Order with ID " + orderId + " not found");
        }
        
        validateStatusTransition(order.getStatus(), OrderStatus.CANCELLED);
        
        // Restore inventory quantities for all order items
        List<OrderItemPojo> orderItems = orderItemService.getByOrderId(orderId);
        for (OrderItemPojo orderItem : orderItems) {
            Integer productId = orderItem.getProduct().getId();
            Integer quantityToRestore = orderItem.getQuantity();
            
            // Add the quantity back to inventory
            inventoryService.addStock(productId, quantityToRestore);
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        orderDao.update(orderId, order);
    }
    
    /**
     * Validate status transitions
     */
    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == null) {
            return; // Allow setting initial status
        }
        
        switch (currentStatus) {
            case CREATED:
                if (newStatus == OrderStatus.CREATED) {
                    throw new ApiException("Order is already in CREATED status");
                }
                break;
            case INVOICED:
                if (newStatus == OrderStatus.CREATED) {
                    throw new ApiException("Cannot revert INVOICED order to CREATED status");
                }
                break;
            case CANCELLED:
                throw new ApiException("Cannot update status of CANCELLED order");
            default:
                throw new ApiException("Unknown order status: " + currentStatus);
        }
    }
}
