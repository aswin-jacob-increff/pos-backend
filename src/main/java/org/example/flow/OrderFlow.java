package org.example.flow;

import org.example.model.enums.OrderStatus;
import org.example.pojo.OrderPojo;
import org.example.pojo.OrderItemPojo;
import org.example.api.OrderApi;
import org.example.api.InventoryApi;
import org.example.dao.OrderItemDao;
import org.example.model.data.PaginationResponse;
import org.example.model.form.PaginationRequest;
import org.example.model.form.PaginationQuery;

import org.example.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;
import java.time.LocalDate;

@Service
@Transactional
public class OrderFlow extends AbstractFlow<OrderPojo> {

    @Autowired
    private OrderApi api;

    @Autowired
    private InventoryApi inventoryApi;

    @Autowired
    private OrderItemDao orderItemDao;

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
    public List<OrderPojo> getOrdersByDateRange(LocalDate startDate, LocalDate endDate) {
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
    public PaginationResponse<OrderPojo> findOrdersBySubstringIdPaginated(
            String searchId, 
            PaginationRequest request) {
        if (searchId == null || searchId.trim().isEmpty()) {
            throw new ApiException("Search ID cannot be null or empty");
        }
        if (request == null) {
            request = new PaginationRequest();
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
    public PaginationResponse<OrderPojo> getAllPaginated(PaginationRequest request) {
        return api.getPaginated(PaginationQuery.all(request));
    }

    /**
     * Get orders by user ID with pagination support, ordered by date descending (most recent first).
     */
    public PaginationResponse<OrderPojo> getByUserIdPaginated(String userId, PaginationRequest request) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new ApiException("User ID cannot be null or empty");
        }
        return api.getPaginated(PaginationQuery.byField("userId", userId, request));
    }

    /**
     * Get orders by date range with pagination support, ordered by date descending (most recent first).
     */
    public PaginationResponse<OrderPojo> getByDateRangePaginated(LocalDate startDate, LocalDate endDate, PaginationRequest request) {
        if (startDate == null || endDate == null) {
            throw new ApiException("Start date and end date cannot be null");
        }
        if (endDate.isBefore(startDate)) {
            throw new ApiException("End date cannot be before start date");
        }
        return api.getPaginated(PaginationQuery.byFields(
            new String[]{"date", "date"}, 
            new Object[]{startDate, endDate}, 
            request
        ));
    }

    // ========== INVENTORY MANAGEMENT METHODS ==========

    /**
     * Reduce inventory when adding an order item
     */
    public void reduceInventoryForOrderItem(Integer productId, Integer quantity) {
        if (productId == null) {
            throw new ApiException("Product ID cannot be null");
        }
        if (quantity == null || quantity <= 0) {
            throw new ApiException("Quantity must be positive");
        }

        try {
            System.out.println("OrderFlow: Reducing inventory for product ID: " + productId + 
                              ", quantity: " + quantity);
            inventoryApi.removeStock(productId, quantity);
            System.out.println("OrderFlow: Successfully reduced inventory for product ID: " + productId);
        } catch (Exception e) {
            System.out.println("OrderFlow: Failed to reduce inventory for product ID: " + productId + 
                              ", error: " + e.getMessage());
            throw new ApiException("Failed to reduce inventory: " + e.getMessage());
        }
    }

    /**
     * Adjust inventory when updating an order item
     */
    public void adjustInventoryForOrderItemUpdate(Integer productId, Integer oldQuantity, Integer newQuantity) {
        if (productId == null) {
            throw new ApiException("Product ID cannot be null");
        }
        if (oldQuantity == null || newQuantity == null) {
            throw new ApiException("Old and new quantities cannot be null");
        }

        int quantityDifference = newQuantity - oldQuantity;
        
        if (quantityDifference != 0) {
            try {
                if (quantityDifference > 0) {
                    // Quantity increased, reduce inventory
                    System.out.println("OrderFlow: Updating order item - reducing inventory for product ID: " + 
                                      productId + ", additional quantity: " + quantityDifference);
                    inventoryApi.removeStock(productId, quantityDifference);
                } else {
                    // Quantity decreased, add back inventory
                    int quantityToAdd = Math.abs(quantityDifference);
                    System.out.println("OrderFlow: Updating order item - adding back inventory for product ID: " + 
                                      productId + ", quantity to add: " + quantityToAdd);
                    inventoryApi.addStock(productId, quantityToAdd);
                }
                System.out.println("OrderFlow: Successfully adjusted inventory for product ID: " + productId);
            } catch (Exception e) {
                System.out.println("OrderFlow: Failed to adjust inventory for product ID: " + productId + 
                                  ", error: " + e.getMessage());
                throw new ApiException("Failed to adjust inventory: " + e.getMessage());
            }
        }
    }

    /**
     * Get order item by ID for inventory calculations
     */
    public OrderItemPojo getOrderItem(Integer orderItemId) {
        if (orderItemId == null) {
            throw new ApiException("Order item ID cannot be null");
        }
        return orderItemDao.select(orderItemId);
    }
}
