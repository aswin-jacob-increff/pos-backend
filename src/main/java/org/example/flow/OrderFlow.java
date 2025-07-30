package org.example.flow;

import org.example.model.enums.OrderStatus;
import org.example.model.form.OrderItemForm;
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
    @Transactional
    public OrderPojo add(OrderPojo orderPojo) {
        if (Objects.isNull(orderPojo)) {
            throw new ApiException("Order cannot be null");
        }

        api.add(orderPojo);
        return orderPojo;
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

    public List<OrderPojo> getOrdersByDateRange(LocalDate startDate, LocalDate endDate) {
        return api.getOrdersByDateRange(startDate, endDate);
    }

    public List<OrderPojo> getOrdersByUserId(String userId) {
        return api.findByUserId(userId);
    }

    // ========== SUBSTRING SEARCH METHODS ==========

    public List<OrderPojo> findOrdersBySubstringId(String searchId, int maxResults) {
        if (searchId == null || searchId.trim().isEmpty()) {
            throw new ApiException("Search ID cannot be null or empty");
        }
        if (maxResults <= 0) {
            throw new ApiException("Max results must be positive");
        }
        return api.findOrdersBySubstringId(searchId, maxResults);
    }

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

    // ========== PAGINATION METHODS ==========

    public PaginationResponse<OrderPojo> getByUserIdPaginated(String userId, PaginationRequest request) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new ApiException("User ID cannot be null or empty");
        }
        return api.getPaginated(PaginationQuery.byField("userId", userId, request));
    }

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

    public OrderItemPojo getOrderItem(Integer orderItemId) {
        if (orderItemId == null) {
            throw new ApiException("Order item ID cannot be null");
        }
        return orderItemDao.select(orderItemId);
    }

    @Transactional
    public OrderPojo createOrderWithItems(OrderPojo orderPojo, List<OrderItemPojo> orderItemPojoList) {
        // Step 1: Check for empty order
        if (Objects.isNull(orderPojo)) {
            throw new ApiException("Order cannot be null");
        }
        if (Objects.isNull(orderItemPojoList) || orderItemPojoList.isEmpty()) {
            throw new ApiException("Order must contain at least one item");
        }

        // Step 2: Check inventory constraints for all items before creating anything
        for (OrderItemPojo orderItemPojo : orderItemPojoList) {
            Integer productId = orderItemPojo.getProductId();
            Integer quantity = orderItemPojo.getQuantity();
            
            if (productId == null) {
                throw new ApiException("Product ID cannot be null");
            }
            if (quantity == null || quantity <= 0) {
                throw new ApiException("Quantity must be positive");
            }

            // Validate inventory availability without modifying it
            inventoryApi.checkInventoryAvailability(productId, quantity);
        }

        // Step 3: All checks passed, now create the order
        api.add(orderPojo);
        
        double totalAmount = 0.0;
        
        // Step 4: Create order items and reduce inventory
        for (OrderItemPojo orderItemPojo : orderItemPojoList) {
            // Set the order ID for each item
            orderItemPojo.setOrderId(orderPojo.getId());
            
            // Add order item through API
            api.addOrderItem(orderItemPojo);
            
            // Reduce inventory (this should succeed since we already validated)
            Integer productId = orderItemPojo.getProductId();
            Integer quantity = orderItemPojo.getQuantity();
            
            try {
                inventoryApi.removeStock(productId, quantity);
            } catch (Exception e) {
                throw new ApiException("Failed to reduce inventory: " + e.getMessage());
            }
            
            // Calculate total
            totalAmount += orderItemPojo.getAmount();
        }
        
        // Step 5: Update order total
        orderPojo.setTotal(totalAmount);
        api.update(orderPojo.getId(), orderPojo);
        
        return orderPojo;
    }


}
