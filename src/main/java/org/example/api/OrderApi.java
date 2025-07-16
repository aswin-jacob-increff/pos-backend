package org.example.api;

import org.example.dao.OrderDao;
import org.example.exception.ApiException;
import org.example.pojo.OrderItemPojo;
import org.example.pojo.OrderPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
public class OrderApi extends AbstractApi<OrderPojo> {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderItemApi orderItemApi;

    @Autowired
    private InventoryApi inventoryApi;

    @Autowired
    private InvoiceApi invoiceApi;

    @Override
    protected String getEntityName() {
        return "Order";
    }

    // Unique add logic for orders (with order items)
    @Override
    public void add(OrderPojo orderPojo) {
        if (Objects.isNull(orderPojo)) {
            throw new ApiException("Order cannot be null");
        }
        // Set default date to current UTC time if not provided
        orderPojo.setDate(Objects.nonNull(orderPojo.getDate()) ? orderPojo.getDate() : Instant.now());
        // Set status to CREATED when order is created
        orderPojo.setStatus(org.example.pojo.OrderStatus.CREATED);
        // Insert order first to get the ID
        orderDao.insert(orderPojo);
        
        // Note: Order items are now managed separately by the calling code
        // The order is created first, then items are added with orderId reference
    }

    public OrderPojo get(Integer id) {
        if (id == null) {
            throw new ApiException("Order ID cannot be null");
        }
        OrderPojo order = orderDao.select(id);
        if (order == null) {
            throw new ApiException("Order with ID " + id + " not found");
        }
        return order;
    }

    @Override
    public void update(Integer id, OrderPojo orderPojo) {
        if (Objects.isNull(id)) {
            throw new ApiException("Order ID cannot be null");
        }
        if (Objects.isNull(orderPojo)) {
            throw new ApiException("Order cannot be null");
        }
        super.update(id, orderPojo);
    }

    public void cancelOrder(Integer orderId) {
        if (Objects.isNull(orderId)) {
            throw new ApiException("Order ID cannot be null");
        }
        OrderPojo order = orderDao.select(orderId);
        if (Objects.isNull(order)) {
            throw new ApiException("Order with ID " + orderId + " not found");
        }
        // Restore inventory quantities for all order items
        List<OrderItemPojo> orderItems = orderItemApi.getByOrderId(orderId);
        for (OrderItemPojo orderItem : orderItems) {
            String productBarcode = orderItem.getProductBarcode();
            Integer quantityToRestore = orderItem.getQuantity();
            // Add the quantity back to inventory
            inventoryApi.addStock(productBarcode, quantityToRestore);
        }
        // Update order status to CANCELLED instead of deleting
        order.setStatus(org.example.pojo.OrderStatus.CANCELLED);
        orderDao.update(orderId, order);
    }

    public String generateInvoice(Integer orderId) throws Exception {
        // This method is now handled by OrderDto.downloadInvoice()
        throw new ApiException("Use OrderDto.downloadInvoice() instead");
    }

    // Note: Order items are now managed separately with denormalized structure

    public void updateStatus(Integer id, org.example.pojo.OrderStatus status) {
        if (Objects.isNull(id)) {
            throw new ApiException("Order ID cannot be null");
        }
        if (Objects.isNull(status)) {
            throw new ApiException("Order status cannot be null");
        }
        OrderPojo order = orderDao.select(id);
        if (order == null) throw new ApiException("Order not found");
        order.setStatus(status);
        orderDao.update(id, order);
    }

    /**
     * Get orders within a date range
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of orders within the date range
     */
    public List<OrderPojo> getOrdersByDateRange(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new ApiException("Start date and end date cannot be null");
        }
        if (endDate.isBefore(startDate)) {
            throw new ApiException("End date cannot be before start date");
        }
        return orderDao.findOrdersByDateRange(startDate, endDate);
    }

    public List<OrderPojo> findByUserId(String userId) {
        return orderDao.findByUserId(userId);
    }
} 