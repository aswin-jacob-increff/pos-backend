package org.example.flow;

import org.example.exception.ApiException;
import org.example.pojo.OrderItemPojo;
import org.example.pojo.OrderPojo;
import org.example.api.OrderItemApi;
import org.example.api.OrderApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class OrderFlow {

    @Autowired
    private OrderApi api;

    @Autowired
    private OrderItemApi orderItemApi;

    public OrderPojo add(OrderPojo orderPojo) {
        List<OrderItemPojo> orderItems = orderPojo.getOrderItems();

        if (orderItems == null || orderItems.isEmpty()) {
            throw new ApiException("Order must contain at least one item");
        }

        // Create the order with all items - OrderApi handles the complete creation
        return api.add(orderPojo);
    }

    public OrderPojo get(Integer id) {
        return api.get(id);
    }

    public List<OrderPojo> getAll() {
        return api.getAll();
    }

    public OrderPojo update(Integer id, OrderPojo form) {
        return api.update(id, form);
    }

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
        return api.generateInvoice(orderId);
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
            throw new ApiException("Failed to load invoice PDF: " + e.getMessage());
        }
    }
}
