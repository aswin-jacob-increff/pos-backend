package org.example.api;

import org.example.dao.OrderItemDao;
import org.example.exception.ApiException;
import org.example.model.enums.OrderStatus;
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
    private OrderItemDao orderItemDao;

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
        orderPojo.setStatus(OrderStatus.CREATED);
        // Insert order first to get the ID
        dao.insert(orderPojo);
        
        // Note: Order items are now managed separately by the calling code
        // The order is created first, then items are added with orderId reference
    }

    public void cancelOrder(Integer orderId) {
        if (Objects.isNull(orderId)) {
            throw new ApiException("Order ID cannot be null");
        }
        OrderPojo order = dao.select(orderId);
        if (order == null) {
            throw new ApiException("Order not found");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new ApiException("Order is already cancelled");
        }
        // Restore inventory quantities for all order items
        List<OrderItemPojo> orderItems = orderItemDao.selectByOrderId(orderId);
        for (OrderItemPojo orderItem : orderItems) {
            Integer productId = orderItem.getProductId();
            Integer quantityToRestore = orderItem.getQuantity();
            // Add the quantity back to inventory
            inventoryApi.addStock(productId, quantityToRestore);
        }
        // Update order status to CANCELLED instead of deleting
        order.setStatus(OrderStatus.CANCELLED);
        dao.update(orderId, order);
    }

    public String generateInvoice(Integer orderId) throws Exception {
        // This method is now handled by OrderDto.downloadInvoice()
        throw new ApiException("Use OrderDto.downloadInvoice() instead");
    }

    // Note: Order items are now managed separately with denormalized structure

    public void updateStatus(Integer id, OrderStatus status) {
        if (Objects.isNull(id)) {
            throw new ApiException("Order ID cannot be null");
        }
        if (Objects.isNull(status)) {
            throw new ApiException("Order status cannot be null");
        }
        OrderPojo order = dao.select(id);
        if (order == null) throw new ApiException("Order not found");
        order.setStatus(status);
        dao.update(id, order);
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
        return ((org.example.dao.OrderDao) dao).findOrdersByDateRange(startDate, endDate);
    }

    public List<OrderPojo> findByUserId(String userId) {
        return ((org.example.dao.OrderDao) dao).findByUserId(userId);
    }

    // ========== SUBSTRING SEARCH METHODS ==========

    /**
     * Find orders by ID substring matching.
     * This allows finding orders where the search term appears exactly as a substring in the order ID.
     * 
     * @param searchId The ID substring to search for
     * @param maxResults Maximum number of results to return
     * @return List of orders where the search term appears as a substring in the ID
     */
    public List<OrderPojo> findOrdersBySubstringId(String searchId, int maxResults) {
        if (searchId == null || searchId.trim().isEmpty()) {
            throw new ApiException("Search ID cannot be null or empty");
        }
        if (maxResults <= 0) {
            throw new ApiException("Max results must be positive");
        }
        return ((org.example.dao.OrderDao) dao).findOrdersBySubstringId(searchId, maxResults);
    }

    /**
     * Find orders by ID substring with pagination support.
     * 
     * @param searchId The ID substring to search for
     * @param request Pagination request
     * @return Paginated response with orders containing the substring
     */
    public org.example.model.data.PaginationResponse<OrderPojo> findOrdersBySubstringIdPaginated(
            String searchId, 
            org.example.model.form.PaginationRequest request) {
        if (searchId == null || searchId.trim().isEmpty()) {
            throw new ApiException("Search ID cannot be null or empty");
        }
        if (request == null) {
            request = new org.example.model.form.PaginationRequest();
        }
        return ((org.example.dao.OrderDao) dao).findOrdersBySubstringIdPaginated(searchId, request);
    }

    /**
     * Count orders by ID substring.
     * 
     * @param searchId The ID substring to search for
     * @return Number of orders containing the substring
     */
    public long countOrdersBySubstringId(String searchId) {
        if (searchId == null || searchId.trim().isEmpty()) {
            throw new ApiException("Search ID cannot be null or empty");
        }
        return ((org.example.dao.OrderDao) dao).countOrdersBySubstringId(searchId);
    }

    // ========== PAGINATION METHODS ==========

    /**
     * Get all orders with pagination support, ordered by date descending (most recent first).
     */
    public org.example.model.data.PaginationResponse<OrderPojo> getAllPaginated(org.example.model.form.PaginationRequest request) {
        return ((org.example.dao.OrderDao) dao).getAllPaginated(request);
    }

    /**
     * Get orders by user ID with pagination support, ordered by date descending (most recent first).
     */
    public org.example.model.data.PaginationResponse<OrderPojo> getByUserIdPaginated(String userId, org.example.model.form.PaginationRequest request) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new ApiException("User ID cannot be null or empty");
        }
        return ((org.example.dao.OrderDao) dao).getByUserIdPaginated(userId, request);
    }

    /**
     * Get orders by date range with pagination support, ordered by date descending (most recent first).
     */
    public org.example.model.data.PaginationResponse<OrderPojo> getByDateRangePaginated(java.time.LocalDate startDate, java.time.LocalDate endDate, org.example.model.form.PaginationRequest request) {
        if (startDate == null || endDate == null) {
            throw new ApiException("Start date and end date cannot be null");
        }
        if (endDate.isBefore(startDate)) {
            throw new ApiException("End date cannot be before start date");
        }
        return ((org.example.dao.OrderDao) dao).getByDateRangePaginated(startDate, endDate, request);
    }
} 