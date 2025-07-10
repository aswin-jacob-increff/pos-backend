package org.example.flow;

import org.example.pojo.OrderPojo;
import org.example.pojo.OrderItemPojo;
import org.example.api.OrderApi;
import org.example.api.OrderItemApi;
import org.example.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class OrderFlow extends AbstractFlow<OrderPojo> {

    @Autowired
    private OrderApi api;

    @Autowired
    private OrderItemApi orderItemApi;

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
        // Order items are now managed separately in OrderApi
        // The order itself is created first, then items are added
        api.add(orderPojo);
        return orderPojo;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void delete(Integer id) {
        List<OrderItemPojo> orderItemPojoList = orderItemApi.getByOrderId(id);
        for (OrderItemPojo orderItemPojo : orderItemPojoList) {
            orderItemApi.delete(orderItemPojo.getId());
        }
        api.delete(id);
    }

    public void cancelOrder(Integer id) {
        api.cancelOrder(id);
    }

    public String generateInvoice(Integer orderId) throws Exception {
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

    public void updateStatus(Integer id, org.example.pojo.OrderStatus status) {
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
}
