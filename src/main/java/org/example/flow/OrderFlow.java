package org.example.flow;

import org.example.model.enums.OrderStatus;
import org.example.pojo.OrderPojo;
import org.example.api.OrderApi;
import org.example.api.OrderItemApi;
import org.example.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class OrderFlow extends AbstractFlow<OrderPojo> {

    @Autowired
    private OrderApi api;

    @Autowired
    private OrderItemApi orderItemApi;

    public OrderFlow() {
        super(OrderPojo.class);
    }

    @Override
    protected Integer getEntityId(OrderPojo entity) {
        return entity.getId();
    }

    @Override
    protected String getEntityName() {
        return "Order";
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public OrderPojo add(OrderPojo orderPojo) {
        if (Objects.isNull(orderPojo)) {
            throw new ApiException("Order cannot be null");
        }
        // Order items are now managed separately in OrderApi
        // The order itself is created first, then items are added
        api.add(orderPojo);
        return orderPojo;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void update(Integer id, OrderPojo entity) {
        if (id == null) {
            throw new ApiException("ID cannot be null");
        }
        if (entity == null) {
            throw new ApiException("Entity cannot be null");
        }
        api.update(id, entity);
    }

    public OrderPojo get(Integer id) {
        if (id == null) {
            throw new ApiException("Order ID cannot be null");
        }
        return api.get(id);
    }

    public List<OrderPojo> getAll() {
        return api.getAll();
    }

    public void cancelOrder(Integer id) {
        if (Objects.isNull(id)) {
            throw new ApiException("Order ID cannot be null");
        }
        api.cancelOrder(id);
    }

    public String generateInvoice(Integer orderId) throws Exception {
        if (Objects.isNull(orderId)) {
            throw new ApiException("Order ID cannot be null");
        }
        // This method is now handled by OrderDto.downloadInvoice()
        throw new ApiException("Use OrderDto.downloadInvoice() instead");
    }

    public org.springframework.core.io.Resource getInvoiceFile(Integer orderId) {
        String fileName = "order-" + orderId + ".pdf";
        java.nio.file.Path filePath = java.nio.file.Paths.get("src/main/resources/invoice/", fileName);
        if (!java.nio.file.Files.exists(filePath)) {
            throw new ApiException("Invoice PDF not found for order ID: " + orderId);
        }
        try {
            return new org.springframework.core.io.UrlResource(filePath.toUri());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException("Failed to load invoice PDF: " + e.getMessage());
        }
    }

    public void updateStatus(Integer id, OrderStatus status) {
        api.updateStatus(id, status);
    }

    /**
     * Get orders within a date range
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of orders within the date range
     */
    public List<OrderPojo> getOrdersByDateRange(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        return api.getOrdersByDateRange(startDate, endDate);
    }

    public List<OrderPojo> getOrdersByUserId(String userId) {
        return api.findByUserId(userId);
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
        return api.findOrdersBySubstringId(searchId, maxResults);
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
        return api.findOrdersBySubstringIdPaginated(searchId, request);
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
        return api.countOrdersBySubstringId(searchId);
    }

    // ========== PAGINATION METHODS ==========

    /**
     * Get all orders with pagination support, ordered by date descending (most recent first).
     */
    public org.example.model.data.PaginationResponse<OrderPojo> getAllPaginated(org.example.model.form.PaginationRequest request) {
        return api.getAllPaginated(request);
    }

    /**
     * Get orders by user ID with pagination support, ordered by date descending (most recent first).
     */
    public org.example.model.data.PaginationResponse<OrderPojo> getByUserIdPaginated(String userId, org.example.model.form.PaginationRequest request) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new ApiException("User ID cannot be null or empty");
        }
        return api.getByUserIdPaginated(userId, request);
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
        return api.getByDateRangePaginated(startDate, endDate, request);
    }
}
