package org.example.api;

import jakarta.transaction.Transactional;
import org.example.dao.OrderItemDao;
import org.example.dao.OrderDao;
import org.example.exception.ApiException;
import org.example.model.enums.OrderStatus;
import org.example.pojo.OrderItemPojo;
import org.example.pojo.OrderPojo;
import org.example.model.data.PaginationResponse;
import org.example.model.form.PaginationRequest;
import org.example.model.form.PaginationQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;
import java.time.LocalDate;
import java.util.Objects;

@Service
@Transactional
public class OrderApi extends AbstractApi<OrderPojo> {

    @Autowired
    private OrderItemDao orderItemDao;

    @Autowired
    private InventoryApi inventoryApi;

    @Autowired
    private InvoiceApi invoiceApi;

    public OrderApi() {
        super(OrderPojo.class);
    }

    // Unique add logic for orders (with order items)
    @Override
    public void add(OrderPojo orderPojo) {
        if (Objects.isNull(orderPojo)) {
            throw new ApiException("Order cannot be null");
        }
        orderPojo.setDate(Objects.nonNull(orderPojo.getDate()) ? orderPojo.getDate() : Instant.now());
        orderPojo.setStatus(OrderStatus.CREATED);
        dao.insert(orderPojo);
    }

    public String generateInvoice(Integer orderId) throws Exception {
        // This method is now handled by OrderDto.downloadInvoice()
        throw new ApiException("Use OrderDto.downloadInvoice() instead");
    }

    // Note: Order items are now managed separately with denormalized structure

    public void updateStatus(Integer id, OrderStatus status) {
        validateId(id);
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
    public List<OrderPojo> getOrdersByDateRange(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        return ((OrderDao) dao).findOrdersByDateRange(startDate, endDate);
    }

    public List<OrderPojo> findByUserId(String userId) {
        return ((OrderDao) dao).findByUserId(userId);
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
        validateString(searchId, "Search ID");
        validatePositive(maxResults, "Max results");
        return ((OrderDao) dao).findOrdersBySubstringId(searchId, maxResults);
    }

    /**
     * Find orders by ID substring with pagination support.
     * 
     * @param searchId The ID substring to search for
     * @param request Pagination request
     * @return Paginated response with orders containing the substring
     */
    public PaginationResponse<OrderPojo> findOrdersBySubstringIdPaginated(
            String searchId, 
            PaginationRequest request) {
        validateString(searchId, "Search ID");
        if (request == null) {
            request = new PaginationRequest();
        }
        return ((OrderDao) dao).findOrdersBySubstringIdPaginated(searchId, request);
    }

    /**
     * Count orders by ID substring.
     * 
     * @param searchId The ID substring to search for
     * @return Number of orders containing the substring
     */
    public long countOrdersBySubstringId(String searchId) {
        validateString(searchId, "Search ID");
        return ((OrderDao) dao).countOrdersBySubstringId(searchId);
    }

    // ========== PAGINATION METHODS ==========

    /**
     * Get all orders with pagination support, ordered by date descending (most recent first).
     */
    public PaginationResponse<OrderPojo> getAllPaginated(PaginationRequest request) {
        return ((OrderDao) dao).getPaginated(PaginationQuery.all(request));
    }

    /**
     * Get orders by user ID with pagination support, ordered by date descending (most recent first).
     */
    public PaginationResponse<OrderPojo> getByUserIdPaginated(String userId, PaginationRequest request) {
        validateString(userId, "User ID");
        return ((OrderDao) dao).getByUserIdPaginated(userId, request);
    }

    /**
     * Get orders by date range with pagination support, ordered by date descending (most recent first).
     */
    public PaginationResponse<OrderPojo> getByDateRangePaginated(LocalDate startDate, LocalDate endDate, PaginationRequest request) {
        validateDateRange(startDate, endDate);
        return ((OrderDao) dao).getByDateRangePaginated(startDate, endDate, request);
    }
} 